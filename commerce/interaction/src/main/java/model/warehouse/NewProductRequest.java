package model.warehouse;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class NewProductRequest {
    private UUID productId;

    private Boolean fragile;
    private Integer quantity;
    private Double weight;
    private Double dimensionWidth;
    private Double dimensionHeight;
    private Double dimensionDepth;

}