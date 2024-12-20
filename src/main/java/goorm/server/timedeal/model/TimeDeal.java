package goorm.server.timedeal.model;

import java.time.LocalDateTime;

import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

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
	private Product product;

	@ManyToOne
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_time_deal_user_id"))
	private User user;

	private LocalDateTime startTime;
	private LocalDateTime endTime;

	private Integer discountPrice;
	private Double discountPercentage;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	private TimeDealStatus status;

	@Override
	public String toString() {
		return "TimeDeal{" +
			"timeDealId=" + timeDealId +
			", product=" + (product != null ? product.getProductId() : "null") +
			", discountPrice=" + discountPrice +
			", status=" + status +
			", startTime=" + startTime +
			", endTime=" + endTime +
			'}';
	}


}
