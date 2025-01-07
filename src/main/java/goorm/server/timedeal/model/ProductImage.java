package goorm.server.timedeal.model;


import org.hibernate.envers.AuditOverride;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
//@Audited
@Table(name = "product_image")
public class ProductImage extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long imageId;

	@ManyToOne
	@JoinColumn(name = "product_id", foreignKey = @ForeignKey(name = "fk_product_id"))
	private Product product;

	private String imageUrl;
	private String imageType;
}
