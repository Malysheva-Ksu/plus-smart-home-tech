package client;

import model.shoppingCart.ShoppingCartResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cloud.openfeign.FeignClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart")
public interface ShoppingCartClient {

    @GetMapping
    ResponseEntity<ShoppingCartResponseDto> getCart(@RequestParam("username") String username);

    @PutMapping
    ResponseEntity<ShoppingCartResponseDto> addProductsToCart(
            @RequestParam("username") String username,
            @RequestBody Map<UUID, Integer> productList // Map<UUID, Integer> как тело запроса
    );

    @DeleteMapping("/item")
    ResponseEntity<Void> removeItem(
            @RequestParam("username") String username,
            @RequestParam("productId") UUID productId
    );

    @DeleteMapping
    ResponseEntity<Void> clearCart(@RequestParam("username") String username);

    @PostMapping("/checkout")
    ResponseEntity<String> checkout(@RequestParam("username") String username);

    @GetMapping("/total")
    ResponseEntity<BigDecimal> getCartTotal(@RequestParam("username") String username);

    @GetMapping("/count")
    ResponseEntity<Integer> getCartItemCount(@RequestParam("username") String username);

    @PostMapping("/remove")
    ResponseEntity<ShoppingCartResponseDto> removeProductsFromCart(
            @RequestParam("username") String username,
            @RequestBody List<UUID> productIds
    );
}