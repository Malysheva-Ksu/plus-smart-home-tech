package model.order;

import java.util.Map;
import java.util.UUID;

public record AssemblyProductsForOrderRequest(
        Map<UUID, Integer> products,
        UUID orderId
) {
}