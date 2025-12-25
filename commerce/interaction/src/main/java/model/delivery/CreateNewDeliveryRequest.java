package model.delivery;

import model.order.OrderDto;
import model.warehouse.AddressDto;

public record CreateNewDeliveryRequest(
        OrderDto orderDto,
        AddressDto fromAddress,
        AddressDto toAddress
) {
}