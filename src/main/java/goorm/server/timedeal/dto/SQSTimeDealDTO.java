package goorm.server.timedeal.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import goorm.server.timedeal.model.TimeDeal;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class SQSTimeDealDTO {

	private Long timeDealId;
	private Long userId;
	private String username;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;

	private Integer discountPrice;
	private Double discountPercentage;
	private LocalDateTime deletedAt;
	private String status;
	private Integer stockQuantity;

	public SQSTimeDealDTO(TimeDeal timeDeal) {
		this.timeDealId = timeDeal.getTimeDealId();
		this.userId = timeDeal.getUser().getUserId();
		this.username = timeDeal.getUser().getUsername();
		this.startTime = timeDeal.getStartTime();
		this.endTime = timeDeal.getEndTime();
		this.discountPrice = timeDeal.getDiscountPrice();
		this.discountPercentage = timeDeal.getDiscountPercentage();
		this.deletedAt = timeDeal.getDeletedAt();
		this.status = timeDeal.getStatus().name();
		this.stockQuantity = timeDeal.getStockQuantity();
	}

	public SQSTimeDealDTO() {
	}
}
