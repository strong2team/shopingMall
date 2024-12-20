package goorm.server.timedeal.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;
import goorm.server.timedeal.dto.ReqTimeDeal;
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
	private final S3Service s3Service;

	/**
	 * 타임딜을 생성하는 메서드.
	 * 요청받은 `ReqTimeDeal` 객체를 기반으로 새로운 타임딜을 생성하고 데이터베이스에 저장.
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
		// 상품 등록
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
		timeDeal.setUser(user); // 유저 정보 추가 (현재 임시 '1')
		timeDeal.setStatus(TimeDealStatus.SCHEDULED);  // 초기 상태는 예약됨
		timeDeal.setStockQuantity(timeDealRequest.stockQuantity());
		timeDeal = timeDealRepository.save(timeDeal);

		return timeDeal;
	}


	public List<TimeDeal> getActiveAndScheduledDeals() {
		return timeDealRepository.findActiveAndScheduledDeals();
	}

	/**
	 * 타임딜의 상태나 속성을 수정하는 메서드.
	 * 타임딜 ID와 수정할 정보를 담고 있는 `UpdateReqTimeDeal` 객체를 받아 기존 타임딜을 업데이트.
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

		// 상품 이미지 수정 (타임딜의 상품 정보에서 수정)

		// 할인율 수정 (discountRate는 discountPercentage로 적용)
		if (timeDealUpdateRequest.discountRate() != null) {
			timeDeal.setDiscountPercentage(Double.valueOf(timeDealUpdateRequest.discountRate()));  // 할인율 적용
		}

		// 시작 시간, 종료 시간 수정
		if (timeDealUpdateRequest.startTime() != null) {
			timeDeal.setStartTime(timeDealUpdateRequest.startTime());
		}
		if (timeDealUpdateRequest.endTime() != null) {
			timeDeal.setEndTime(timeDealUpdateRequest.endTime());
		}

		// 상태 수정 (TimeDealStatus로 설정)
		if (timeDealUpdateRequest.status() != null) {
			timeDeal.setStatus(TimeDealStatus.valueOf(timeDealUpdateRequest.status()));  // 상태 변경
		}

		// 재고 수량 수정
		if (timeDealUpdateRequest.stockQuantity() != null) {
			timeDeal.setStockQuantity(timeDealUpdateRequest.stockQuantity());
		}

		// 변경 사항 저장
		return timeDeal;
	}
}
