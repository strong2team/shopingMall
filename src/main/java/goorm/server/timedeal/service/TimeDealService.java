package goorm.server.timedeal.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import goorm.server.timedeal.config.aws.SqsMessageSender;
import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.dto.SQSTimeDealDTO;
import goorm.server.timedeal.dto.UpdateReqTimeDeal;
import goorm.server.timedeal.model.Product;
import goorm.server.timedeal.model.ProductImage;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;
import goorm.server.timedeal.model.enums.TimeDealStatus;
import goorm.server.timedeal.repository.ProductImageRepository;
import goorm.server.timedeal.repository.ProductRepository;
import goorm.server.timedeal.repository.TimeDealRepository;
import goorm.server.timedeal.repository.UserRepository;
import goorm.server.timedeal.service.aws.EventBridgeRuleService;
import goorm.server.timedeal.service.aws.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealService {

	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final TimeDealRepository timeDealRepository;
	private final UserRepository userRepository;
	private final SqsMessageSender sqsMessageSender;

	private final S3Service s3Service;
	private final EventBridgeRuleService eventBridgeRuleService;

	@Value("${cloud.aws.lambda.timedeal-update-arn}") // Lambda ARN (application.yml에 설정)
	private String timeDealUpdateLambdaArn;

	/**
	 * 타임딜을 생성하는 메서드.
	 *
	 * @param timeDealRequest 생성할 타임딜의 세부 정보를 담고 있는 `ReqTimeDeal` 객체.
	 * @return 생성된 타임딜 객체를 반환.
	 * @throws IOException 타임딜 생성 중 외부 리소스와의 연동 시 IO 예외 발생 시 던져짐.
	 */
	@Transactional
	public TimeDeal createTimeDeal(ReqTimeDeal timeDealRequest) throws IOException {
		log.info("createTimeDeal 서비스 레이어가 정상적으로 실행되었습니다.");

		// 1. 유저 확인
		User user = userRepository.findById(timeDealRequest.userId())
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저입니다."));

		// 2. 상품 등록
		Product product = new Product();
		product.setTitle(timeDealRequest.title());
		product.setPrice(timeDealRequest.price());
		product.setMallName(timeDealRequest.mallName());
		product.setBrand(timeDealRequest.brand());
		product.setCategory1(timeDealRequest.category1());
		product = productRepository.save(product);

		// 3. 이미지 업로드 (S3에 저장하고 URL 반환)
		String imageUrl = s3Service.uploadImageFromUrl(timeDealRequest.imageUrl());

		// 4. 상품 이미지 저장
		ProductImage productImage = new ProductImage();
		productImage.setProduct(product);
		productImage.setImageUrl(imageUrl);
		productImage.setImageType("thumbnail");
		productImageRepository.save(productImage);

		// 5. 타임딜 예약 생성
		TimeDeal timeDeal = new TimeDeal();
		timeDeal.setProduct(product);
		timeDeal.setStartTime(timeDealRequest.startTime());
		timeDeal.setEndTime(timeDealRequest.endTime());
		timeDeal.setDiscountPrice(timeDealRequest.discountPrice());
		timeDeal.setDiscountPercentage(timeDealRequest.discountPercentage());
		timeDeal.setUser(user);
		timeDeal.setStatus(TimeDealStatus.SCHEDULED); // 초기 상태는 예약됨
		timeDeal.setStockQuantity(timeDealRequest.stockQuantity());
		timeDeal = timeDealRepository.save(timeDeal);

		// 6. EventBridge Rule 생성
		createEventBridgeRulesForTimeDeal(timeDeal);

		return timeDeal;
	}

	public List<TimeDeal> getActiveAndScheduledDeals() {
		return timeDealRepository.findActiveAndScheduledDeals();
	}

	/**
	 * 타임딜의 상태나 속성을 수정하는 메서드.
	 *
	 * @param dealId 타임딜을 식별하는 고유 ID.
	 * @param timeDealUpdateRequest 수정할 타임딜 정보를 담고 있는 `UpdateReqTimeDeal`.
	 * @return 업데이트된 타임딜 객체를 반환.
	 */
	@Transactional
	public TimeDeal updateTimeDeal(Long dealId, UpdateReqTimeDeal timeDealUpdateRequest) {
		// 타임딜 ID로 기존 타임딜 조회
		TimeDeal timeDeal = timeDealRepository.findById(dealId)
			.orElseThrow(() -> new RuntimeException("타임딜을 찾을 수 없습니다."));

		// 할인율 수정
		if (timeDealUpdateRequest.discountRate() != null) {
			timeDeal.setDiscountPercentage(Double.valueOf(timeDealUpdateRequest.discountRate()));
		}

		// 시작 시간, 종료 시간 수정
		if (timeDealUpdateRequest.startTime() != null) {
			timeDeal.setStartTime(timeDealUpdateRequest.startTime());
		}
		if (timeDealUpdateRequest.endTime() != null) {
			timeDeal.setEndTime(timeDealUpdateRequest.endTime());
		}

		// 상태 수정
		if (timeDealUpdateRequest.status() != null) {
			timeDeal.setStatus(TimeDealStatus.valueOf(timeDealUpdateRequest.status()));
		}

		// 재고 수량 수정
		if (timeDealUpdateRequest.stockQuantity() != null) {
			timeDeal.setStockQuantity(timeDealUpdateRequest.stockQuantity());
		}

		sendTimeDealUpdateMessage(timeDeal);

		return timeDeal;
	}

	private void createEventBridgeRulesForTimeDeal(TimeDeal timeDeal) {
		// 시작 시간 Rule 생성
		String startRuleName = "TimeDealStart-" + timeDeal.getTimeDealId();
		String startCron = eventBridgeRuleService.convertToCronExpression(timeDeal.getStartTime());
		String startPayload = String.format("{\"time_deal_id\": %d, \"new_status\": \"%s\"}",
			timeDeal.getTimeDealId(), TimeDealStatus.ACTIVE.name());
		eventBridgeRuleService.createEventBridgeRule(
			startRuleName,
			startCron,
			startPayload,
			timeDealUpdateLambdaArn
		);

		// 종료 시간 Rule 생성
		String endRuleName = "TimeDealEnd-" + timeDeal.getTimeDealId();
		String endCron = eventBridgeRuleService.convertToCronExpression(timeDeal.getEndTime());
		String endPayload = String.format("{\"time_deal_id\": %d, \"new_status\": \"%s\"}",
			timeDeal.getTimeDealId(), TimeDealStatus.ENDED.name());
		eventBridgeRuleService.createEventBridgeRule(
			endRuleName,
			endCron,
			endPayload,
			timeDealUpdateLambdaArn
		);
	}

	private void sendTimeDealUpdateMessage(TimeDeal timeDeal) {
		// SQS 메시지 전송
		log.info("SQS 메시지를 전송 시작합니다.");
		SQSTimeDealDTO timeDealDTO = new SQSTimeDealDTO(timeDeal);
		sqsMessageSender.sendJsonMessage(timeDealDTO);
		log.info("SQS 메시지를 전송했습니다: {}", timeDeal);
	}
}
