package goorm.server.timedeal.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import goorm.server.timedeal.config.aws.SqsMessageSender;
import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.dto.ResDetailPageTimeDealDto;
import goorm.server.timedeal.dto.ResTimeDealListDto;
import goorm.server.timedeal.dto.SQSTimeDealDTO;
import goorm.server.timedeal.dto.UpdateReqTimeDeal;
import goorm.server.timedeal.model.Product;
import goorm.server.timedeal.model.ProductImage;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;
import goorm.server.timedeal.model.enums.MessageType;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
		//String imageUrl = s3Service.uploadImageFromUrl(timeDealRequest.imageUrl());
		String imageUrl = s3Service.uploadImageFromUrlWithCloudFront(timeDealRequest.imageUrl());

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
		// KST to UTC conversion
		ZonedDateTime startKST = timeDeal.getStartTime().atZone(ZoneId.of("Asia/Seoul"));
		ZonedDateTime endKST = timeDeal.getEndTime().atZone(ZoneId.of("Asia/Seoul"));

		// Convert to UTC
		ZonedDateTime startUTC = startKST.withZoneSameInstant(ZoneId.of("UTC"));
		ZonedDateTime endUTC = endKST.withZoneSameInstant(ZoneId.of("UTC"));

		// Format the time as a cron expression
		String startCron = eventBridgeRuleService.convertToCronExpression(startUTC.toLocalDateTime());
		String endCron = eventBridgeRuleService.convertToCronExpression(endUTC.toLocalDateTime());

		// Prepare payload for EventBridge Rule using UTC times
		String startRuleName = "TimeDealStart-" + timeDeal.getTimeDealId();
		String startPayload = String.format("{\"time_deal_id\": %d, \"new_status\": \"%s\", \"message_type\": \"%s\"}",
			timeDeal.getTimeDealId(), TimeDealStatus.ACTIVE.name(), "AUTO_TIME_DEAL_CHANGE");

		String endRuleName = "TimeDealEnd-" + timeDeal.getTimeDealId();
		String endPayload = String.format("{\"time_deal_id\": %d, \"new_status\": \"%s\", \"message_type\": \"%s\"}",
			timeDeal.getTimeDealId(), TimeDealStatus.ENDED.name(), "AUTO_TIME_DEAL_CHANGE");

		// Create EventBridge Rules using UTC times
		eventBridgeRuleService.createEventBridgeRule(
			startRuleName,
			startCron,
			startPayload,
			timeDealUpdateLambdaArn
		);

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

		// 메시지 타입 설정
		timeDealDTO.setMessageType(MessageType.USER_TIME_DEAL_CHANGE);

		sqsMessageSender.sendJsonMessage(timeDealDTO);
		log.info("SQS 메시지를 전송했습니다: {}", timeDeal);
	}

	/**
	 * 상품 상세 정보를 조회하는 메서드.
	 *
	 * @param productId 조회할 상품의 ID.
	 * @return 상품 상세 정보를 담은 DTO.
	 */
	public ResDetailPageTimeDealDto getTimeDealDetails(Long productId) {
		// 1. 상품에 대한 타임딜 정보 조회
		TimeDeal timeDeal = timeDealRepository.findByProduct_ProductId(productId)
			.orElseThrow(() -> new RuntimeException("해당 상품에 대한 타임딜 정보를 찾을 수 없습니다."));

		// 2. 상품 이미지 조회
		List<String> productImages = productImageRepository.findByProduct_ProductId(productId)
			.stream()
			.map(ProductImage::getImageUrl)
			.toList();

		// 3. DTO 생성 및 반환
		return new ResDetailPageTimeDealDto(
			timeDeal.getProduct().getProductId(),
			//String.join(",", productImages),
			String.join("", productImages.get(0)), // 단일 이미지로 설정. 나중에 여러 이미지 저장할때는 수정 필요
			//timeDeal.getProduct().getTitle(),
			removeHtmlTags(timeDeal.getProduct().getTitle()), // HTML 태그 제거
			timeDeal.getProduct().getPrice(),
			timeDeal.getDiscountPrice(),
			String.format("%d%%", timeDeal.getDiscountPercentage().intValue()),
			timeDeal.getStartTime(),
			timeDeal.getEndTime(),
			timeDeal.getStatus().name(),
			timeDeal.getStockQuantity(),
			timeDeal.getProduct().getBrand(),         // 추가
			timeDeal.getProduct().getMallName()       // 추가
		);
	}

	// HTML 태그 제거 유틸리티 메서드
	private String removeHtmlTags(String input) {
		if (input == null) {
			return null;
		}
		return input.replaceAll("<[^>]+>", ""); // 정규식으로 HTML 태그 제거
	}

	/**
	 * 타임딜 리스트를 가져오는 메서드
	 */
	public List<ResTimeDealListDto> getTimeDealList() {
		List<TimeDeal> timeDeals = timeDealRepository.findAll(); // 예시: 전체 타임딜 조회
		return timeDeals.stream()
			.map(deal -> new ResTimeDealListDto(
				deal.getTimeDealId(),
				deal.getProduct().getTitle(),
				deal.getStartTime(),
				deal.getEndTime(),
				deal.getStockQuantity(),
				deal.getDiscountPercentage(),
				deal.getDiscountPrice(),
				mapTimeDealStatus(deal.getStartTime(), deal.getEndTime()) // 상태 변환
			))
			.collect(Collectors.toList());
	}

	private String mapTimeDealStatus(LocalDateTime startTime, LocalDateTime endTime) {
		LocalDateTime now = LocalDateTime.now();

		if (now.isBefore(startTime)) {
			return "진행전"; // SCHEDULED
		} else if (now.isAfter(endTime)) {
			return "종료"; // ENDED
		} else {
			return "진행중"; // ACTIVE
		}
	}
}
