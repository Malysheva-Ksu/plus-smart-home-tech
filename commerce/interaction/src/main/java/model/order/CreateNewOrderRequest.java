package model.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import model.shoppingCart.ShoppingCart;
import model.warehouse.AddressDto;

public record CreateNewOrderRequest(
        @NotNull
        @Valid
        ShoppingCart shoppingCart,

        @NotNull
        AddressDto deliveryAddress
) {
}