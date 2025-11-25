package model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 100)
    private String reference;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 500)
    private String description;

    public StockMovement() {
    }

    public StockMovement(Long productId, MovementType movementType, Integer quantity, String reference) {
        this.productId = productId;
        this.movementType = movementType;
        this.quantity = quantity;
        this.reference = reference;
        this.createdAt = LocalDateTime.now();
    }

    public StockMovement(Long productId, MovementType movementType, Integer quantity, String reference, String description) {
        this(productId, movementType, quantity, reference);
        this.description = description;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}