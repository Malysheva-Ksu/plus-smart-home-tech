package model.shoppingStore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private UUID productId;

    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    private String productName;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    @Size(max = 500, message = "Image source path must not exceed 500 characters")
    private String imageSrc;

    @NotBlank(message = "Quantity state is required")
    @Pattern(regexp = "ENDED|FEW|ENOUGH|MANY", message = "Invalid quantity state. Must be one of: ENDED, FEW, ENOUGH, MANY")
    private String quantityState;

    @NotBlank(message = "Product state is required")
    @Pattern(regexp = "ACTIVE|DEACTIVATE", message = "Invalid product state. Must be one of: ACTIVE, DEACTIVATE")
    private String productState;

    @Pattern(regexp = "LIGHTING|CONTROL|SENSORS", message = "Invalid category. Must be one of: LIGHTING, CONTROL, SENSORS")
    private String productCategory;

    private BigDecimal price;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public String getQuantityState() {
        return quantityState;
    }

    public void setQuantityState(String quantityState) {
        this.quantityState = quantityState;
    }

    public String getProductState() {
        return productState;
    }

    public void setProductState(String productState) {
        this.productState = productState;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}