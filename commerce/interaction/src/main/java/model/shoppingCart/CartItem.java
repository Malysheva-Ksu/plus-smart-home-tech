package model.shoppingCart;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID; // Добавлен для типа UUID

@Data
@Entity
@Builder
@Table(name = "shopping_cart_product")
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private ShoppingCart cart;

    @Column(name = "product_id", columnDefinition = "UUID", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 10, scale = 2, nullable = true)
    private BigDecimal price;

}