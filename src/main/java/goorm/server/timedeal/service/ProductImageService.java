package goorm.server.timedeal.service;

import java.util.List;

import org.springframework.stereotype.Service;

import goorm.server.timedeal.model.Product;
import goorm.server.timedeal.model.ProductImage;
import goorm.server.timedeal.repository.ProductImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductImageService {

	private final ProductImageRepository productImageRepository;

	@Transactional
	public void saveProductImage(Product product, String imageUrl, String imageType) {
		ProductImage productImage = new ProductImage();
		productImage.setProduct(product);
		productImage.setImageUrl(imageUrl);
		productImage.setImageType(imageType);
		productImageRepository.save(productImage);
	}
	public List<String> findImageUrlsByProductId(Long productId) {
		return productImageRepository.findByProduct_ProductId(productId)
			.stream()
			.map(ProductImage::getImageUrl)
			.toList();
	}
}
