package goorm.server.timedeal.model;

import java.util.List;

import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter  @Setter
@Entity
//@Audited  // 변경 이력 추적
//@AuditTable(schema = "audit", value = "product_audit")  // 변경 이력을 저장할 테이블 지정
@Table(name = "product")
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long productId;

	@Column(name = "title", nullable = false)
	@NotNull(message = "Title cannot be null")
	private String title;

	@Column(name = "price", nullable = false)
	private int price;

	@Column(name = "mall_name")
	private String mallName;

	private String brand;

	private String category1;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ProductImage> productImages;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<TimeDeal> timeDeals;
}