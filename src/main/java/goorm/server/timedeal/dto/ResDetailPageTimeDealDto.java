package goorm.server.timedeal.dto;

import java.time.LocalDateTime;

public record ResDetailPageTimeDealDto(
	Long productId,
	String productImageUrl,
	String productName,
	int originalPrice,
	int discountPrice,
	String discountRate,
	LocalDateTime startTime,
	LocalDateTime endTime,
	String status,
	Integer stockQuantity,
	String brand,
	String mallName){
}
