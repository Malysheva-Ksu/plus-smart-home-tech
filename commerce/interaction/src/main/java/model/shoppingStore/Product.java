package model.shoppingStore;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.util.UUID; // Для типа UUID

@Entity
@Table(name = "store_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "productId", columnDefinition = "UUID")
    private UUID productId;

    @Column(name = "productName", nullable = false)
    private String productName;

    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @Column(name = "imageSrc", length = 500)
    private String imageSrc;

    @Column(name = "quantityState", nullable = false, length = 50)
    private String quantityState;

    @Column(name = "productState", nullable = false, length = 50)
    private String productState;

    @Column(name = "productCategory", length = 50)
    private String productCategory;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
}