package model.warehouse;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class StockItemResponse {

    private UUID productId;

    private Integer quantity;

    private BigDecimal price;

}