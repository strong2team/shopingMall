package goorm.server.timedeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import goorm.server.timedeal.model.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long>,
	RevisionRepository<ProductImage, Long, Integer> {
}