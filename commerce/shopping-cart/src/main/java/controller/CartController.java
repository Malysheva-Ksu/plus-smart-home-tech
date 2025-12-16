package controller;

import jakarta.transaction.Transactional;
import client.WarehouseServiceClient;
import client.ProductServiceClient;
import model.shoppingCart.CartItemRequest;
import model.shoppingCart.ChangeQuantityRequest;
import model.shoppingCart.ShoppingCartResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/v1/shopping-cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;
    private final ProductServiceClient productServiceClient;
    private final WarehouseServiceClient warehouseServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public CartController(CartService cartService,
                          ProductServiceClient productServiceClient,
                          WarehouseServiceClient warehouseServiceClient,
                          CircuitBreakerFactory circuitBreakerFactory) {
        this.cartService = cartService;
        this.productServiceClient = productServiceClient;
        this.warehouseServiceClient = warehouseServiceClient;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping
    public ResponseEntity<ShoppingCartResponseDto> getCart(@RequestParam String username) {
        log.info("Getting cart for user: {}", username);

        ShoppingCartResponseDto cartDto = cartService.getOrCreateActiveCart(username);
        return ResponseEntity.ok(cartDto);
    }

    @PutMapping
    @Transactional
    public ResponseEntity<ShoppingCartResponseDto> addProductsToCart(
            @RequestParam String username,
            @RequestBody Map<UUID, Integer> productsMap) {

        log.info("Starting update/add items for cart: username={}", username);

        List<CartItemRequest> itemsRequest = productsMap.entrySet().stream()
                .map(entry -> new CartItemRequest(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("shoppingServices");

        ShoppingCartResponseDto cartDto = circuitBreaker.run(() -> {

            return cartService.addOrUpdateItems(username, itemsRequest);

        }, throwable -> {
            log.error("Circuit breaker triggered for addProductsToCart - username: {}, error: {}",
                    username, throwable.getMessage());
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Service Unavailable: Unable to add items to cart due to backend service failure.",
                    throwable
            );
        });

        log.info("Items successfully added/updated in cart: username={}", username);
        return ResponseEntity.status(HttpStatus.OK).body(cartDto);
    }

@PostMapping("/change-quantity")
@Transactional
public ResponseEntity<ShoppingCartResponseDto> changeProductQuantity(
        @RequestParam String username,
        @RequestBody ChangeQuantityRequest request) {

    log.info("Changing product quantity: username={}, productId={}, newQuantity={}",
            username, request.getProductId(), request.getNewQuantity());

    ShoppingCartResponseDto response = cartService.changeSingleProductQuantity(
            username,
            request.getProductId(),
            request.getNewQuantity()
    );

    return ResponseEntity.ok(response);
}

    @PostMapping("/remove")
    @Transactional
    public ResponseEntity<ShoppingCartResponseDto> removeProductsFromCart(
            @RequestParam String username,
            @RequestBody List<UUID> productIds) {

        log.info("Removing products from cart: username={}, productIds={}", username, productIds);

        ShoppingCartResponseDto response = cartService.removeProductsFromCart(username, productIds);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @Transactional
    public ResponseEntity<Void> deactivateCart(
            @RequestParam String username) {

        log.info("Attempting to deactivate or clear cart for user: {}", username);

        cartService.deactivateCart(username);

        return ResponseEntity.noContent().build();
    }
}