package model.shoppingCart;

import lombok.Data;
import java.util.UUID;

@Data
public class ChangeQuantityRequest {
    private Integer newQuantity;
    private UUID productId;
}