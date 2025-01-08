package goorm.server.timedeal.service;

import org.springframework.stereotype.Service;

import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.model.Product;
import goorm.server.timedeal.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

	@Transactional
	public Product createProduct(ReqTimeDeal timeDealRequest) {
		Product product = new Product();
		product.setTitle(timeDealRequest.title());
		product.setPrice(timeDealRequest.price());
		product.setMallName(timeDealRequest.mallName());
		product.setBrand(timeDealRequest.brand());
		product.setCategory1(timeDealRequest.category1());
		return productRepository.save(product);
	}
}
