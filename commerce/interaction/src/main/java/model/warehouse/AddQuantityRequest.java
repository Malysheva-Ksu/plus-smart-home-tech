package model.warehouse;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AddQuantityRequest {
    private UUID productId;
    private Integer quantity;
}