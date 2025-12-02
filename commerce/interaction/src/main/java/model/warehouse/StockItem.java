package model.warehouse;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_items")
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false)
    private Integer reserved = 0;

    @Column(name = "last_stock_update")
    private LocalDateTime lastStockUpdate = LocalDateTime.now();

    @Column(name = "min_stock_level")
    private Integer minStockLevel = 0;

    @Column(name = "max_stock_level")
    private Integer maxStockLevel;

    public StockItem() {
    }

    public StockItem(Long productId, Integer quantity, Integer reserved) {
        this.productId = productId;
        this.quantity = quantity;
        this.reserved = reserved;
        this.lastStockUpdate = LocalDateTime.now();
    }

    public Integer getAvailableQuantity() {
        return quantity - reserved;
    }

    public boolean isLowStock() {
        return minStockLevel != null && getAvailableQuantity() <= minStockLevel;
    }

    public boolean isOutOfStock() {
        return getAvailableQuantity() <= 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.lastStockUpdate = LocalDateTime.now();
    }
}