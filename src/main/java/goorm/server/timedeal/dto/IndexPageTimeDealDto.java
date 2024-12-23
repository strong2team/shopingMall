package goorm.server.timedeal.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record IndexPageTimeDealDto(
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
