package goorm.server.timedeal.dto;

import java.time.LocalDateTime;

import lombok.Data;

import java.time.LocalDateTime;

public record UpdateReqTimeDeal(
	Long userId,          // 유저 ID (권한 검증용)
	String discountRate,
	LocalDateTime startTime,
	LocalDateTime endTime,
	String status,
	Integer stockQuantity
) {}
