package goorm.server.timedeal.dto;

import java.time.LocalDateTime;

public record ReqTimeDeal(
	Long userId, // 요청한 유저의 ID
	String title,
	int price,
	String mallName,
	String brand,
	String category1,
	String imageUrl,
	LocalDateTime startTime,
	LocalDateTime endTime,
	Integer discountPrice,
	Double discountPercentage
) {}
