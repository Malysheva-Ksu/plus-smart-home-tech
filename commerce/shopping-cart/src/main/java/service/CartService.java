package service;

import model.shoppingCart.ShoppingCartResponseDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface CartService {

    public ShoppingCartResponseDto addProductsToCart(String username, Map<UUID, Integer> productList);

    public ShoppingCartResponseDto getCartByUsername(String username);

    public ShoppingCartResponseDto changeSingleProductQuantity(String username, UUID productId, Integer newQuantity);

    public ShoppingCartResponseDto removeProductsFromCart(String username, List<UUID> productIds);

    public void deactivateCart(String username);
}