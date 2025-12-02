package controller;

import client.ProductServiceClient;
import client.WarehouseServiceClient;
import exception.InsufficientStockException;
import exception.ServiceUnavailableException;
import model.AddItemRequest;
import model.shoppingCart.CartItem;
import model.shoppingCart.ShoppingCart;
import model.shoppingCart.UpdateQuantityRequest;
import model.warehouse.ReserveRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.CartService;

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

    @GetMapping("/{userId}")
    public ResponseEntity<ShoppingCart> getCart(@PathVariable Long userId) {
        log.info("Getting cart for user: {}", userId);

        ShoppingCart cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartItem> addItem(@PathVariable Long userId,
                                            @RequestBody AddItemRequest request) {
        log.info("Adding item to cart: userId={}, productId={}, quantity={}",
                userId, request.getProductId(), request.getQuantity());

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("shoppingStore");

        CartItem cartItem = circuitBreaker.run(() -> {
            log.debug("Checking product existence: productId={}", request.getProductId());
            var product = productServiceClient.getProduct(request.getProductId());

            log.debug("Checking stock availability: productId={}", request.getProductId());
            var stock = warehouseServiceClient.getStock(request.getProductId());

            int availableQuantity = stock.getQuantity() - stock.getReserved();
            if (availableQuantity < request.getQuantity()) {
                log.warn("Insufficient stock: productId={}, requested={}, available={}",
                        request.getProductId(), request.getQuantity(), availableQuantity);
                throw new InsufficientStockException(
                        String.format("Not enough stock available. Requested: %d, Available: %d",
                                request.getQuantity(), availableQuantity)
                );
            }

            log.debug("Adding item to cart: userId={}, productId={}, price={}",
                    userId, request.getProductId(), product.getPrice());
            return cartService.addItem(userId, request.getProductId(),
                    request.getQuantity(), product.getPrice());

        }, throwable -> {
            log.error("Circuit breaker triggered for addItem - userId: {}, productId: {}, error: {}",
                    userId, request.getProductId(), throwable.getMessage());
            throw new ServiceUnavailableException("Unable to add item to cart - dependent services unavailable");
        });

        log.info("Item successfully added to cart: userId={}, cartItemId={}", userId, cartItem.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartItem);
    }

    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartItem> updateQuantity(@PathVariable Long userId,
                                                   @PathVariable Long itemId,
                                                   @RequestBody UpdateQuantityRequest request) {
        log.info("Updating item quantity: userId={}, itemId={}, newQuantity={}",
                userId, itemId, request.getQuantity());

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("warehouseService");

        CartItem updatedItem = circuitBreaker.run(() -> {
            CartItem existingItem = cartService.getCartItem(userId, itemId);

            if (request.getQuantity() > existingItem.getQuantity()) {
                int additionalQuantity = request.getQuantity() - existingItem.getQuantity();
                var stock = warehouseServiceClient.getStock(existingItem.getProductId());

                int availableQuantity = stock.getQuantity() - stock.getReserved();
                if (availableQuantity < additionalQuantity) {
                    log.warn("Insufficient stock for update: productId={}, additional={}, available={}",
                            existingItem.getProductId(), additionalQuantity, availableQuantity);
                    throw new InsufficientStockException(
                            String.format("Not enough stock available for update. Additional needed: %d, Available: %d",
                                    additionalQuantity, availableQuantity)
                    );
                }
            }

            return cartService.updateItemQuantity(userId, itemId, request.getQuantity());

        }, throwable -> {
            log.error("Circuit breaker triggered for updateQuantity - userId: {}, itemId: {}, error: {}",
                    userId, itemId, throwable.getMessage());
            throw new ServiceUnavailableException("Unable to update item quantity - warehouse service unavailable");
        });

        log.info("Item quantity updated: userId={}, itemId={}", userId, itemId);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long userId,
                                           @PathVariable Long itemId) {
        log.info("Removing item from cart: userId={}, itemId={}", userId, itemId);

        cartService.removeItem(userId, itemId);

        log.info("Item removed from cart: userId={}, itemId={}", userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/items")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        log.info("Clearing cart for user: {}", userId);

        cartService.clearCart(userId);

        log.info("Cart cleared for user: {}", userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<String> checkout(@PathVariable Long userId) {
        log.info("Starting checkout process for user: {}", userId);

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("warehouseService");

        String orderId = circuitBreaker.run(() -> {
            ShoppingCart cart = cartService.getCart(userId);

            if (cart.getItems().isEmpty()) {
                log.warn("Checkout failed: empty cart for user: {}", userId);
                throw new IllegalArgumentException("Cannot checkout empty cart");
            }

            log.debug("Cart contents for checkout - items: {}, total: {}",
                    cart.getItems().size(), cart.getTotalAmount());

            var reservations = cart.getItems().stream()
                    .collect(Collectors.toMap(
                            CartItem::getProductId,
                            CartItem::getQuantity
                    ));

            ReserveRequest reserveRequest = new ReserveRequest(reservations);
            log.debug("Reserving stock: {}", reservations);

            warehouseServiceClient.reserveStock(reserveRequest);
            log.info("Stock reserved successfully for user: {}", userId);

            String generatedOrderId = "ORDER-" + System.currentTimeMillis() + "-" + userId;
            log.info("Order created: {}", generatedOrderId);

            cartService.clearCart(userId);
            log.info("Cart cleared after successful checkout for user: {}", userId);

            return generatedOrderId;

        }, throwable -> {
            log.error("Circuit breaker triggered for checkout - userId: {}, error: {}",
                    userId, throwable.getMessage());

            try {
                ShoppingCart cart = cartService.getCart(userId);
                if (!cart.getItems().isEmpty()) {
                    var reservations = cart.getItems().stream()
                            .collect(Collectors.toMap(
                                    CartItem::getProductId,
                                    CartItem::getQuantity
                            ));
                    ReserveRequest releaseRequest = new ReserveRequest(reservations);
                    warehouseServiceClient.releaseStock(releaseRequest);
                    log.info("Stock released after checkout failure for user: {}", userId);
                }
            } catch (Exception e) {
                log.error("Failed to release stock after checkout failure: {}", e.getMessage());
            }

            throw new ServiceUnavailableException("Unable to complete checkout - service unavailable");
        });

        log.info("Checkout completed successfully for user: {}, orderId: {}", userId, orderId);
        return ResponseEntity.ok(orderId);
    }

    @GetMapping("/{userId}/total")
    public ResponseEntity<Double> getCartTotal(@PathVariable Long userId) {
        log.debug("Getting cart total for user: {}", userId);

        ShoppingCart cart = cartService.getCart(userId);
        double total = cart.getTotalAmount().doubleValue();

        return ResponseEntity.ok(total);
    }

    @GetMapping("/{userId}/count")
    public ResponseEntity<Integer> getCartItemCount(@PathVariable Long userId) {
        log.debug("Getting cart item count for user: {}", userId);

        ShoppingCart cart = cartService.getCart(userId);
        int itemCount = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();

        return ResponseEntity.ok(itemCount);
    }
}