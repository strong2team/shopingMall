package goorm.server.timedeal.dto;

import java.time.LocalDateTime;

public record ResTimeDealListDto(
	Long timeDealId,
	String productName,
	LocalDateTime startTime,
	LocalDateTime endTime,
	Integer stockQuantity,
	Double discountRate,
	Integer discountPrice,
	String status
) {}