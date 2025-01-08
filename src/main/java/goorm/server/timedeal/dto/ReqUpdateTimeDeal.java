package goorm.server.timedeal.dto;

import java.time.LocalDateTime;

public record ReqUpdateTimeDeal(
	Long userId,          // 유저 ID (권한 검증용)
	String discountRate,
	LocalDateTime startTime,
	LocalDateTime endTime,
	String status,
	Integer stockQuantity
) {}
