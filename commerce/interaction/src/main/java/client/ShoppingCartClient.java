package client;

import model.AddItemRequest;
import model.shoppingCart.CartItem;
import model.shoppingCart.ShoppingCart;
import model.shoppingCart.UpdateQuantityRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "shopping-cart-service", path = "/api/v1/shopping-cart")
public interface ShoppingCartClient {

    @GetMapping("/{userId}")
    ResponseEntity<ShoppingCart> getCart(@PathVariable("userId") Long userId);

    @PostMapping("/{userId}/items")
    ResponseEntity<CartItem> addItem(@PathVariable("userId") Long userId,
                                     @RequestBody AddItemRequest request);

    @PutMapping("/{userId}/items/{itemId}")
    ResponseEntity<CartItem> updateQuantity(@PathVariable("userId") Long userId,
                                            @PathVariable("itemId") Long itemId,
                                            @RequestBody UpdateQuantityRequest request);

    @DeleteMapping("/{userId}/items/{itemId}")
    ResponseEntity<Void> removeItem(@PathVariable("userId") Long userId,
                                    @PathVariable("itemId") Long itemId);

    @DeleteMapping("/{userId}/items")
    ResponseEntity<Void> clearCart(@PathVariable("userId") Long userId);

    @PostMapping("/{userId}/checkout")
    ResponseEntity<String> checkout(@PathVariable("userId") Long userId);

    @GetMapping("/{userId}/total")
    ResponseEntity<Double> getCartTotal(@PathVariable("userId") Long userId);

    @GetMapping("/{userId}/count")
    ResponseEntity<Integer> getCartItemCount(@PathVariable("userId") Long userId);
}