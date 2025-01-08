package goorm.server.timedeal.dto;


import java.time.LocalDateTime;

public record ResPurchase(
	Long userId,
	int quantity,
	LocalDateTime purchaseTime
) {}