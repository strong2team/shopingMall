package goorm.server.timedeal.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.dto.ResDetailPageTimeDealDto;
import goorm.server.timedeal.dto.ResPurchaseDto;
import goorm.server.timedeal.dto.ResTimeDealListDto;
import goorm.server.timedeal.dto.ReqUpdateTimeDeal;
import goorm.server.timedeal.model.Product;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;

import goorm.server.timedeal.model.enums.TimeDealStatus;
import goorm.server.timedeal.repository.TimeDealRepository;
import goorm.server.timedeal.service.aws.EventBridgeRuleService;
import goorm.server.timedeal.service.aws.S3Service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealService {

	private final UserService userService;
	private final ProductService productService;
	private final ProductImageService productImageService;
	private final PurchaseService purchaseService;

	private final TimeDealRepository timeDealRepository;

	private final S3Service s3Service;
	private final EventBridgeRuleService eventBridgeRuleService;

	@Value("${cloud.aws.lambda.timedeal-update-arn}")
	private String timeDealUpdateLambdaArn;


	private final RedissonClient redissonClient;  // RedissonClient 주입
	private final StringRedisTemplate redisTemplate;  // Redis 캐시 주입


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
		User user = userService.findById(timeDealRequest.userId());

		// 2. 상품 등록
		Product product = productService.createProduct(timeDealRequest);

		// 3. 이미지 업로드 (S3에 저장하고 URL 반환)
		String imageUrl = s3Service.uploadImageFromUrlWithCloudFront(timeDealRequest.imageUrl());

		// 4. 상품 이미지 저장
		productImageService.saveProductImage(product, imageUrl, "thumbnail");


		// 5. 타임딜 예약 생성
		TimeDeal timeDeal = saveTimeDeal(timeDealRequest, product, user);

		// 6. EventBridge Rule 생성
		createEventBridgeRulesForTimeDeal(timeDeal);

		return timeDeal;
	}

	private TimeDeal saveTimeDeal(ReqTimeDeal timeDealRequest, Product product, User user) {
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
	public TimeDeal updateTimeDeal(Long dealId, ReqUpdateTimeDeal timeDealUpdateRequest) {
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

		return timeDeal;
	}


	private void createEventBridgeRulesForTimeDeal(TimeDeal timeDeal) {
		// KST to UTC conversion
		ZonedDateTime startKST = timeDeal.getStartTime().atZone(ZoneId.of("Asia/Seoul"));
		ZonedDateTime endKST = timeDeal.getEndTime().atZone(ZoneId.of("Asia/Seoul"));

		// System.out.println("startKST"+startKST);
		// System.out.println("endKST"+startKST);


		// 1. UTC 로 변환
		ZonedDateTime startUTC = startKST.withZoneSameInstant(ZoneId.of("UTC"));
		ZonedDateTime endUTC = endKST.withZoneSameInstant(ZoneId.of("UTC"));

		// 2. cron expression 포맷팅
		String startCron = eventBridgeRuleService.convertToCronExpression(startUTC.toLocalDateTime());
		String endCron = eventBridgeRuleService.convertToCronExpression(endUTC.toLocalDateTime());

		String startRuleName = "TimeDealStart-" + timeDeal.getTimeDealId();
		String startPayload = String.format("{\"time_deal_id\": %d, \"new_status\": \"%s\"}",
			timeDeal.getTimeDealId(), TimeDealStatus.ACTIVE.name());

		String endRuleName = "TimeDealEnd-" + timeDeal.getTimeDealId();
		String endPayload = String.format("{\"time_deal_id\": %d, \"new_status\": \"%s\"}",
			timeDeal.getTimeDealId(), TimeDealStatus.ENDED.name());

		// 3. Create EventBridge Rules using UTC times
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
		List<String> productImages = productImageService.findImageUrlsByProductId(productId);


		// 3. DTO 생성 및 반환
		return new ResDetailPageTimeDealDto(
			timeDeal.getTimeDealId(),
			timeDeal.getProduct().getProductId(),
			//String.join(",", productImages),
			String.join("", productImages.get(0)), // 단일 이미지로 설정. 나중에 여러 이미지 저장할때는 수정 필요
			removeHtmlTags(timeDeal.getProduct().getTitle()), // HTML 태그 제거
			timeDeal.getProduct().getPrice(),
			timeDeal.getDiscountPrice(),
			String.format("%d%%", timeDeal.getDiscountPercentage().intValue()),
			timeDeal.getStartTime(),
			timeDeal.getEndTime(),
			timeDeal.getStatus().name(),
			timeDeal.getStockQuantity(),
			timeDeal.getProduct().getBrand(),
			timeDeal.getProduct().getMallName()
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

	/**
	 * 타임딜 구매 메서드.
	 * 비관적 락을 사용하여 다중 서버 환경에서 재고 감소를 처리.
	 *
	 * @param timeDealId 구매할 타임딜 ID.
	 * @param quantity   구매 수량.
	 * @return 구매 성공 여부 메시지.
	 */
	@Transactional
	public String purchaseTimeDeal(Long timeDealId, int quantity) {
		// 타임딜 조회 시 비관적 락 사용
		TimeDeal timeDeal = timeDealRepository.findByIdWithLock(timeDealId)
			.orElseThrow(() -> new RuntimeException("타임딜 정보를 찾을 수 없습니다."));

		// 재고 확인
		if (timeDeal.getStockQuantity() < quantity) {
			throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + timeDeal.getStockQuantity() + "개");
		}

		// 재고 감소
		timeDeal.setStockQuantity(timeDeal.getStockQuantity() - quantity);

		// 구매 완료 메시지 반환
		return "구매가 완료되었습니다. 남은 재고: " + timeDeal.getStockQuantity() + "개";
	}

	@Transactional
	public void updateTimeDealStatus(Long timeDealId, TimeDealStatus newStatus) {
		int updatedRows = timeDealRepository.updateStatus(timeDealId, newStatus);
		if (updatedRows > 0) {
			System.out.println("TimeDeal ID: " + timeDealId + " updated to status: " + newStatus);
		} else {
			System.out.println("TimeDeal ID: " + timeDealId + " not found or already updated.");
		}
	}

	public Optional<TimeDeal> findById(Long timeDealId) {
		return timeDealRepository.findById(timeDealId);

	}

	/**
	 * 테스트용 타임딜 구매 메서드
	 *
	 * @param timeDealId 구매할 타임딜 ID
	 * @param userId     구매할 유저 ID
	 * @param quantity   구매 수량
	 * @return 구매 완료 메시지
	 */
	@Transactional
	public ResPurchaseDto testPurchaseTimeDeal(Long timeDealId, Long userId, int quantity) {
		// 유저 존재 여부 확인
		User user = userService.findById(userId);


		// 타임딜 조회 시 비관적 락 사용
		TimeDeal timeDeal = timeDealRepository.findByIdWithLock(timeDealId)
			.orElseThrow(() -> new RuntimeException("타임딜 정보를 찾을 수 없습니다."));

		// 재고 확인
		if (timeDeal.getStockQuantity() < quantity) {
			throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + timeDeal.getStockQuantity() + "개");
		}

		// 재고 감소
		timeDeal.setStockQuantity(timeDeal.getStockQuantity() - quantity);
		timeDealRepository.save(timeDeal);

		// 구매 기록 생성 및 저장
		return purchaseService.createPurchaseRecord(timeDeal, user, quantity);
	}

	/**
	 * 특정 타임딜의 남은 재고 수량을 반환하는 메서드.
	 *
	 * @param timeDealId 조회할 타임딜 ID.
	 * @return 남은 재고 수량 (정수값).
	 * @throws IllegalArgumentException 유효하지 않은 타임딜 ID일 경우 예외 발생.
	 */
	public int getRemainingStock(Long timeDealId) {
		// 타임딜 ID로 해당 타임딜 조회
		TimeDeal timeDeal = timeDealRepository.findById(timeDealId)
			.orElseThrow(() -> new IllegalArgumentException("유효하지 않은 타임딜 ID입니다: " + timeDealId));

		// 남은 재고 수량 반환
		int remainingStock = timeDeal.getStockQuantity();
		log.info("TimeDeal ID: {}, Remaining Stock: {}", timeDealId, remainingStock);
		return remainingStock;
	}

	@Transactional
	public TimeDeal updateTimeDealStock(Long dealId, int stockQuantity) {
		// 타임딜 조회
		TimeDeal timeDeal = timeDealRepository.findById(dealId)
			.orElseThrow(() -> new IllegalArgumentException("해당 타임딜을 찾을 수 없습니다. ID: " + dealId));

		// 수량 업데이트
		if (stockQuantity < 0) {
			throw new IllegalArgumentException("수량은 0 이상이어야 합니다.");
		}
		timeDeal.setStockQuantity(stockQuantity);

		// 저장 및 반환
		return timeDealRepository.save(timeDeal);
	}







	//
	@Transactional
	public ResPurchaseDto testPurchaseTimeDealByRedis(Long timeDealId, Long userId, int quantity) {
		// 유저 확인
		User user = userService.findById(userId);

		// Redis 락을 통한 동시성 제어
		RLock lock = redissonClient.getLock("deal-lock:" + timeDealId); // 고유한 락 키 설정
		lock.lock(); // 락을 얻음

		try {
			// Redis에서 재고 수량 조회
			String stockKey = "time_deal:stock:" + timeDealId;
			String stockQuantityStr = redisTemplate.opsForValue().get(stockKey);

			int currentStockQuantity = (stockQuantityStr != null) ? Integer.parseInt(stockQuantityStr) : 0;

			// 캐시된 재고 수량이 DB와 다를 수 있으므로 DB를 한번 조회해 갱신
			if (currentStockQuantity == 0) {
				TimeDeal timeDeal = timeDealRepository.findById(timeDealId)
					.orElseThrow(() -> new RuntimeException("타임딜 정보를 찾을 수 없습니다."));
				currentStockQuantity = timeDeal.getStockQuantity();
				// Redis 캐시 갱신
				redisTemplate.opsForValue().set(stockKey, String.valueOf(currentStockQuantity));
			}

			// 재고 확인
			if (currentStockQuantity < quantity) {
				throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + currentStockQuantity + "개");
			}

			// 재고 감소 및 Redis 업데이트
			currentStockQuantity -= quantity;
			redisTemplate.opsForValue().set(stockKey, String.valueOf(currentStockQuantity));

			// DB 업데이트
			TimeDeal timeDeal = timeDealRepository.findById(timeDealId)
				.orElseThrow(() -> new RuntimeException("타임딜 정보를 찾을 수 없습니다."));
			timeDeal.setStockQuantity(currentStockQuantity);
			timeDealRepository.save(timeDeal);

			// 구매 기록 생성 및 반환
			return purchaseService.createPurchaseRecord(timeDeal, user, quantity);
		} finally {
			lock.unlock(); // 락 해제
		}
	}



}
