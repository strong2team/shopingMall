package goorm.server.timedeal.model;

import java.time.LocalDateTime;

import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import goorm.server.timedeal.model.enums.PurchaseStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Audited
@AuditTable(schema = "audit", value = "purchase_audit")
@Table(name = "purchase")
public class Purchase extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long purchaseId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "time_deal_id", foreignKey = @ForeignKey(name = "fk_purchase_time_deal_id"))
	private TimeDeal timeDeal;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_purchase_user_id"))
	private User user;

	@Column(nullable = false)
	private LocalDateTime purchaseTime;

	@Column(nullable = false, columnDefinition = "INT DEFAULT 1")
	private int quantity;

	@Enumerated(EnumType.STRING)
	private PurchaseStatus status;
}