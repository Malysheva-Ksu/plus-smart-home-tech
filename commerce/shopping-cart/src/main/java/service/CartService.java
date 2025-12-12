package service;

import model.shoppingCart.CartItemRequest;
import model.shoppingCart.ShoppingCartResponseDto;

import java.util.List;
import java.util.UUID;

public interface CartService {

    ShoppingCartResponseDto getOrCreateActiveCart(String username);

    ShoppingCartResponseDto addOrUpdateItems(String username, List<CartItemRequest> itemsRequest);

    public ShoppingCartResponseDto changeSingleProductQuantity(String username, UUID productId, Integer newQuantity);

    public ShoppingCartResponseDto removeProductsFromCart(String username, List<UUID> productIds);

    void deactivateCart(String username);
}