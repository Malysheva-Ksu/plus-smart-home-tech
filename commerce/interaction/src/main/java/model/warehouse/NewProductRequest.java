package model.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewProductRequest {
    private UUID productId;
    private Boolean fragile;
    private Integer quantity;
    private Double weight;

    private Dimension dimension;

    public Double getDimensionWidth() {
        return dimension != null ? dimension.getWidth() : null;
    }

    public Double getDimensionHeight() {
        return dimension != null ? dimension.getHeight() : null;
    }

    public Double getDimensionDepth() {
        return dimension != null ? dimension.getDepth() : null;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dimension {
        private Double width;
        private Double height;
        private Double depth;
    }
}