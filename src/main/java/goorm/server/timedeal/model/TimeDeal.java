package goorm.server.timedeal.model;

import java.time.LocalDateTime;

import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

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


@Entity
@Audited
@AuditTable(schema = "audit", value = "time_deal_audit")
@Table(name = "time_deal")
public class TimeDeal extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long timeDealId;

	@ManyToOne
	@JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_time_deal_product_id"))
	private Product product;

	private LocalDateTime startTime;
	private LocalDateTime endTime;

	private Integer discountPrice;
	private Double discountPercentage;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

	@Enumerated(EnumType.STRING)
	private TimeDealStatus status;

}
