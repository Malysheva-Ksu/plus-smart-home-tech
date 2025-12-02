package model.warehouse;


import model.MovementType;
import org.jetbrains.annotations.NotNull;

public class StockUpdateRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Integer quantity;

    @NotNull
    private MovementType movementType;

    private String reference;

    public StockUpdateRequest() {
    }

    public StockUpdateRequest(Long productId, Integer quantity, MovementType movementType, String reference) {
        this.productId = productId;
        this.quantity = quantity;
        this.movementType = movementType;
        this.reference = reference;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public model.MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}