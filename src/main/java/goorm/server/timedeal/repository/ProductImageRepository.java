package goorm.server.timedeal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import goorm.server.timedeal.model.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>,
	RevisionRepository<ProductImage, Long, Integer> {
	List<ProductImage> findByProduct_ProductId(Long productId); // 명시적으로 참조 경로 수정
}