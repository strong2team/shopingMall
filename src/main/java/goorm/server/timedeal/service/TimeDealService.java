package goorm.server.timedeal.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.model.Product;
import goorm.server.timedeal.model.ProductImage;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;
import goorm.server.timedeal.model.enums.TimeDealStatus;
import goorm.server.timedeal.repository.ProductImageRepository;
import goorm.server.timedeal.repository.ProductRepository;
import goorm.server.timedeal.repository.TimeDealRepository;
import goorm.server.timedeal.repository.UserRepository;
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
		timeDeal = timeDealRepository.save(timeDeal);

		return timeDeal;
	}


	public List<TimeDeal> getActiveAndScheduledDeals() {
		return timeDealRepository.findActiveAndScheduledDeals();
	}
}
