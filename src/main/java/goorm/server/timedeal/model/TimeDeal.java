package goorm.server.timedeal.model;

import java.time.LocalDateTime;

import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import goorm.server.timedeal.model.enums.TimeDealStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Audited
@AuditTable(schema = "audit", value = "time_deal_audit")
@Table(name = "time_deal")
@Getter @Setter
public class TimeDeal extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long timeDealId;


	@ManyToOne
	@JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_time_deal_product_id"))
	@JsonIgnore
	private Product product;

	@ManyToOne
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_time_deal_user_id"))
	private User user;


	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime startTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime endTime;

	private Integer discountPrice;
	private Double discountPercentage;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	private TimeDealStatus status;

	// 추가된 재고 수량 변수
	@Column(name = "stock_quantity", nullable = false)
	private Integer stockQuantity;

	public TimeDeal() {
	}

	// @Override
	// public String toString() {
	// 	return "TimeDeal{" +
	// 		"timeDealId=" + timeDealId +
	// 		", product=" + (product != null ? product.getProductId() : "null") +
	// 		", discountPrice=" + discountPrice +
	// 		", stockQuantity=" + stockQuantity +
	// 		", status=" + status +
	// 		", startTime=" + startTime +
	// 		", endTime=" + endTime +
	// 		'}';
	// }
}
