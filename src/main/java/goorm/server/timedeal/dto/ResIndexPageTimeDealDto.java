package goorm.server.timedeal.dto;

import java.time.LocalDateTime;

public record ResIndexPageTimeDealDto(
	Long productId,
	String ProductImages,
	String productName,
	int originalPrice,
	int discountPrice,
	String discountRate,
	LocalDateTime startTime,
	LocalDateTime endTime,
	String status,
	Integer stockQuantity
) {
}
