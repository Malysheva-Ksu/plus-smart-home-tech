package model.shoppingStore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public class QuantityUpdateDto {

    @NotNull(message = "Product ID is required for update")
    private UUID productId;

    @NotBlank(message = "Quantity state is required")
    @Pattern(regexp = "ENDED|FEW|ENOUGH|MANY", message = "Invalid quantity state. Must be one of: ENDED, FEW, ENOUGH, MANY")
    private String quantityState;

    public QuantityUpdateDto() {
    }

    public QuantityUpdateDto(UUID productId, String quantityState) {
        this.productId = productId;
        this.quantityState = quantityState;
    }


    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getQuantityState() {
        return quantityState;
    }

    public void setQuantityState(String quantityState) {
        this.quantityState = quantityState;
    }
}