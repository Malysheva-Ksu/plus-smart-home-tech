package service;

import model.CartItem;
import model.ShoppingCart;

import java.math.BigDecimal;

public interface CartService {

    ShoppingCart getCart(Long userId);

    CartItem addItem(Long userId, Long productId, Integer quantity, BigDecimal price);

    CartItem updateItemQuantity(Long userId, Long itemId, Integer quantity);

    void removeItem(Long userId, Long itemId);

    void clearCart(Long userId);

    CartItem getCartItem(Long userId, Long itemId);
}