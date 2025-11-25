package model;

import lombok.Data;

@Data
public class StockItem {
    private Long productId;
    private Integer quantity;
    private Integer reserved;

    public StockItem() {
    }

    public StockItem(Long productId, Integer quantity, Integer reserved) {
        this.productId = productId;
        this.quantity = quantity;
        this.reserved = reserved;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getReserved() {
        return reserved;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setReserved(Integer reserved) {
        this.reserved = reserved;
    }

    public Integer getAvailableQuantity() {
        return quantity - reserved;
    }
}