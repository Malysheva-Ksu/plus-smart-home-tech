package model.warehouse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStock {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "fragile", nullable = false)
    private Boolean fragile;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "dimension_width", nullable = false)
    private Double dimensionWidth;

    @Column(name = "dimension_height", nullable = false)
    private Double dimensionHeight;

    @Column(name = "dimension_depth", nullable = false)
    private Double dimensionDepth;

}