package goorm.server.timedeal.dto;


import java.time.LocalDateTime;

public record ResPurchaseDto(
	Long userId,
	int quantity,
	LocalDateTime purchaseTime
) {}