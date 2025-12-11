package service;

import client.ProductServiceClient;
import exception.CartNotFoundException;
import exception.ItemNotInCartException;
import jakarta.transaction.Transactional;
import model.shoppingCart.CartItem;
import model.shoppingCart.ShoppingCart;
import model.shoppingCart.ShoppingCartResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import repository.CartItemRepository;
import repository.CartRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;

    public CartServiceImpl(CartRepository cartRepository,
                           CartItemRepository cartItemRepository,
                           ProductServiceClient productServiceClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productServiceClient = productServiceClient;
    }

    @Override
    public ShoppingCartResponseDto addProductsToCart(String username, Map<UUID, Integer> productList) {
        log.debug("Adding/updating products for user: {}", username);

        ShoppingCart cart = getOrCreateCart(username);
        boolean isUpdated = false;

        for (Map.Entry<UUID, Integer> entry : productList.entrySet()) {
            UUID productId = entry.getKey();
            Integer quantity = entry.getValue();

            if (quantity <= 0) {
                removeItemByProductId(cart, productId);
                isUpdated = true;
                continue;
            }

            BigDecimal price = getProductPriceFromService(productId);

            Optional<CartItem> existingItemOpt = cartItemRepository.findByCartUsernameAndProductId(username, productId);

            if (existingItemOpt.isPresent()) {
                CartItem item = existingItemOpt.get();
                item.setQuantity(quantity);
                item.setPrice(price);
                cartItemRepository.save(item);
                log.debug("Updated existing item quantity: {}", quantity);
            } else {
                CartItem newItem = new CartItem();
                newItem.setCart(cart);
                newItem.setProductId(productId);
                newItem.setQuantity(quantity);
                newItem.setPrice(price);

                CartItem savedItem = cartItemRepository.save(newItem);
                cart.getItems().add(savedItem);
                log.debug("Created new cart item");
            }
            isUpdated = true;
        }

        if (isUpdated) {
            updateCartTotal(cart);
            cartRepository.save(cart);
        }

        log.info("Products processed successfully for user: {}", username);
        return convertToDto(cart);
    }

    @Override
    public ShoppingCartResponseDto getCartByUsername(String username) {
        log.debug("Getting cart for user: {}", username);

        ShoppingCart cart = cartRepository.findByUsername(username)
                .orElseGet(() -> createNewCart(username));

        return convertToDto(cart);
    }

    private ShoppingCart createNewCart(String username) {
        log.debug("Creating new cart for user: {}", username);
        ShoppingCart cart = new ShoppingCart();
        cart.setUsername(username);
        cart.setTotalAmount(BigDecimal.ZERO);
        return cartRepository.save(cart);
    }

    private ShoppingCart getOrCreateCart(String username) {
        return cartRepository.findByUsername(username)
                .orElseGet(() -> createNewCart(username));
    }

    private ShoppingCartResponseDto convertToDto(ShoppingCart cart) {
        Map<UUID, Integer> productsMap = cart.getItems().stream()
                .collect(Collectors.toMap(
                        CartItem::getProductId,
                        CartItem::getQuantity
                ));

        return ShoppingCartResponseDto.builder()
                .username(cart.getUsername())
                .totalAmount(cart.getTotalAmount())
                .products(productsMap)
                .build();
    }

    private BigDecimal getProductPriceFromService(UUID productId) {
        return BigDecimal.valueOf(100.00);
    }

    private void removeItemByProductId(ShoppingCart cart, UUID productId) {
        Optional<CartItem> itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();

        if (itemToRemove.isPresent()) {
            cart.getItems().remove(itemToRemove.get());
            cartItemRepository.delete(itemToRemove.get());
            log.info("Item removed from cart: username={}, productId={}", cart.getUsername(), productId);
        }
    }

    private void updateCartTotal(ShoppingCart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(total);
        log.debug("Updated cart total: {}", total);
    }

    @Override
    public ShoppingCartResponseDto changeSingleProductQuantity(String username, UUID productId, Integer newQuantity) {

        ShoppingCart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + username));

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCartUsernameAndProductId(username, productId);

        if (existingItemOpt.isEmpty()) {
            throw new ItemNotInCartException(productId);
        }

        CartItem item = existingItemOpt.get();

        if (newQuantity <= 0) {
            removeItemByProductId(cart, productId);
        } else {
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        }

        updateCartTotal(cart);
        cartRepository.save(cart);

        return convertToDto(cart);
    }

    @Override
    public ShoppingCartResponseDto removeProductsFromCart(String username, List<UUID> productIds) {

        ShoppingCart cart = cartRepository.findByUsernameWithItems(username)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + username));

        boolean removed = false;

        for (UUID productId : productIds) {
            Optional<CartItem> itemToRemove = cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst();

            if (itemToRemove.isPresent()) {
                CartItem item = itemToRemove.get();
                cart.getItems().remove(item);
                cartItemRepository.delete(item);
                removed = true;
                log.debug("Product removed from cart: {}", productId);
            } else {
                log.warn("Attempted to remove product not found in cart: {}", productId);
            }
        }

        if (removed) {
            updateCartTotal(cart);
            cartRepository.save(cart);
        }

        return convertToDto(cart);
    }

    @Override
    public void deactivateCart(String username) {
        log.debug("Deactivating cart for user: {}", username);

        ShoppingCart cart = cartRepository.findByUsername(username)
                .orElseThrow(() -> new CartNotFoundException("Cart not found for user: " + username));

        cart.setIsActive(false);

        if (!cart.getItems().isEmpty()) {
            cartItemRepository.deleteAll(cart.getItems());
            cart.getItems().clear();
        }

        cart.setTotalAmount(BigDecimal.ZERO);
        cartRepository.save(cart);

        log.info("Cart successfully deactivated for user: {}", username);
    }

}