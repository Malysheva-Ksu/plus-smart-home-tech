package model.delivery;

import java.util.UUID;

public record ShippedToDeliveryRequest(
        UUID orderId,
        UUID deliveryId
) {
}