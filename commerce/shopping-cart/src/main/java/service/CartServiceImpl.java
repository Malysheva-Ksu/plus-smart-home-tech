package service;

import client.WarehouseServiceClient;
import exception.CartNotFoundException;
import exception.InsufficientStockException;
import exception.ProductNotFoundException;
import model.shoppingCart.CartItem;
import model.shoppingCart.CartItemRequest;
import model.shoppingCart.ShoppingCart;
import model.shoppingCart.ShoppingCartResponseDto;
import model.warehouse.StockItemResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.CartItemRepository;
import repository.ShoppingCartRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final ShoppingCartRepository cartRepository;
    private final CartItemRepository itemRepository;
    private final WarehouseServiceClient warehouseClient;

    public CartServiceImpl(ShoppingCartRepository cartRepository,
                           CartItemRepository itemRepository,
                           WarehouseServiceClient warehouseClient) {
        this.cartRepository = cartRepository;
        this.itemRepository = itemRepository;
        this.warehouseClient = warehouseClient;
    }

    @Override
    @Transactional
    public ShoppingCartResponseDto getOrCreateActiveCart(String username) {
        log.debug("Attempting to get or create active cart for user: {}", username);

        ShoppingCart cart = cartRepository.findByUsernameAndIsActiveTrue(username)
                .orElseGet(() -> {
                    log.info("Active cart not found, creating new cart for user: {}", username);
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUsername(username);
                    newCart.setTotalAmount(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public ShoppingCartResponseDto addOrUpdateItems(String username, List<CartItemRequest> itemsRequest) {
        ShoppingCart cart = cartRepository.findByUsernameAndIsActiveTrue(username)
                .orElseGet(() -> {
                    log.info("Active cart not found for addOrUpdateItems, creating new cart for user: {}", username);
                    ShoppingCart newCart = new ShoppingCart();
                    newCart.setUsername(username);
                    newCart.setTotalAmount(BigDecimal.ZERO);
                    return cartRepository.save(newCart);
                });

        for (CartItemRequest itemRequest : itemsRequest) {
            UUID productId = itemRequest.getProductId();
            Integer requestedQuantity = itemRequest.getQuantity();

            StockItemResponse stock = checkWarehouseStock(productId, requestedQuantity);

            Optional<CartItem> existingItem = itemRepository.findByCartAndProductId(cart, productId);

            CartItem item;
            if (existingItem.isPresent()) {
                item = existingItem.get();
                item.setQuantity(requestedQuantity);
                item.setPrice(stock.getPrice());
            } else {
                item = CartItem.builder()
                        .cart(cart)
                        .productId(productId)
                        .quantity(requestedQuantity)
                        .price(stock.getPrice())
                        .build();
                cart.getItems().add(item);
            }
            itemRepository.save(item);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public ShoppingCartResponseDto changeSingleProductQuantity(String username, UUID productId, Integer newQuantity) {
        ShoppingCart cart = cartRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + username));

        StockItemResponse stock = checkWarehouseStock(productId, newQuantity);

        CartItem item = itemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found in cart: " + productId));

        if (newQuantity <= 0) {
            itemRepository.delete(item);
            cart.getItems().remove(item);
            log.info("Item removed from cart: productId={}", productId);
        } else {
            item.setQuantity(newQuantity);
            item.setPrice(stock.getPrice());
            itemRepository.save(item);
            log.info("Item quantity changed: productId={}, newQuantity={}", productId, newQuantity);
        }

        updateCartTotals(cart);
        cartRepository.save(cart);

        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public ShoppingCartResponseDto removeProductsFromCart(String username, List<UUID> productIds) {
        ShoppingCart cart = cartRepository.findByUsernameAndIsActiveTrue(username)
                .orElseThrow(() -> new CartNotFoundException("Active cart not found for user: " + username));

        productIds.forEach(productId -> {
            itemRepository.findByCartAndProductId(cart, productId)
                    .ifPresent(item -> {
                        itemRepository.delete(item);
                        cart.getItems().remove(item);
                        log.debug("Removed item {} from cart.", productId);
                    });
        });

        updateCartTotals(cart);
        cartRepository.save(cart);
        return mapToResponse(cart);
    }

    @Override
    @Transactional
    public void deactivateCart(String username) {
        Optional<ShoppingCart> cartOptional = cartRepository.findByUsername(username);

        if (cartOptional.isPresent()) {
            ShoppingCart cart = cartOptional.get();
            if (cart.getIsActive()) {
                cart.setIsActive(false);
                cartRepository.save(cart);
                log.info("Cart deactivated for user: {}", username);
            } else {
                log.warn("Cart for user {} is already inactive.", username);
            }
        } else {
            log.info("No cart found for user {} to deactivate. Considering it already deactivated or non-existent.", username);
        }
    }


    private StockItemResponse checkWarehouseStock(UUID productId, Integer requestedQuantity) {
        ResponseEntity<StockItemResponse> response = warehouseClient.getStock(productId);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            log.warn("Warehouse returned status {} for product {}", response.getStatusCode(), productId);
            throw new ProductNotFoundException("Product not found in stock: " + productId);
        }

        StockItemResponse stock = response.getBody();

        if (stock.getQuantity() < requestedQuantity) {
            log.warn("Insufficient stock for product {}. Requested: {}, Available: {}",
                    productId, requestedQuantity, stock.getQuantity());
            throw new InsufficientStockException("Insufficient stock for product " + productId
                    + ". Available: " + stock.getQuantity());
        }
        return stock;
    }

    @Transactional
    private void updateCartTotals(ShoppingCart cart) {
        BigDecimal newTotal = cart.getItems().stream()
                .map(item -> {
                    BigDecimal price = (item.getPrice() != null) ? item.getPrice() : new BigDecimal("10.00");
                    return price.multiply(new BigDecimal(item.getQuantity()));
                })
                .reduce(new BigDecimal("10.00"), BigDecimal::add);
        cart.setTotalAmount(newTotal);
    }

    private ShoppingCartResponseDto mapToResponse(ShoppingCart cart) {
        List<ShoppingCartResponseDto.Item> responseItems = cart.getItems().stream()
                .map(item -> new ShoppingCartResponseDto.Item(
                        item.getProductId(),
                        item.getQuantity(),
                        item.getPrice()
                ))
                .collect(Collectors.toList());

        return ShoppingCartResponseDto.builder()
                .username(cart.getUsername())
                .totalAmount(cart.getTotalAmount())
                .items(responseItems)
                .build();
    }
}