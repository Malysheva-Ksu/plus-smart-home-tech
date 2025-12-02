package service;

import exception.CartItemNotFoundException;
import model.shoppingCart.CartItem;
import model.shoppingCart.ShoppingCart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.CartItemRepository;
import repository.CartRepository;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public CartItem updateItemQuantity(Long userId, Long itemId, Integer quantity) {
        log.debug("Updating item quantity: userId={}, itemId={}, quantity={}", userId, itemId, quantity);

        CartItem cartItem = cartItemRepository.findByIdAndCartUserIdWithCart(itemId, userId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId, userId));

        if (quantity <= 0) {
            removeItem(userId, itemId);
            throw new IllegalArgumentException("Item removed due to zero quantity");
        }

        cartItem.setQuantity(quantity);
        CartItem updatedItem = cartItemRepository.save(cartItem);

        ShoppingCart cart = cartItem.getCart();
        updateCartTotal(cart);
        cartRepository.save(cart);

        log.info("Item quantity updated: userId={}, itemId={}, newQuantity={}", userId, itemId, quantity);

        return updatedItem;
    }

    @Override
    public void removeItem(Long userId, Long itemId) {
        log.debug("Removing item from cart: userId={}, itemId={}", userId, itemId);

        CartItem cartItem = cartItemRepository.findByIdAndCartUserIdWithCart(itemId, userId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId, userId));

        ShoppingCart cart = cartItem.getCart();

        cart.getItems().remove(cartItem);

        cartItemRepository.delete(cartItem);

        updateCartTotal(cart);
        cartRepository.save(cart);

        log.info("Item removed from cart: userId={}, itemId={}", userId, itemId);
    }

    @Override
    @Transactional(readOnly = true)
    public ShoppingCart getCart(Long userId) {
        log.debug("Getting cart for user: {}", userId);
        return cartRepository.findByUserIdWithItems(userId)
                .orElseGet(() -> createNewCart(userId));
    }

    @Override
    public CartItem addItem(Long userId, Long productId, Integer quantity, BigDecimal price) {
        log.debug("Adding item to cart: userId={}, productId={}, quantity={}, price={}",
                userId, productId, quantity, price);

        ShoppingCart cart = getCart(userId);

        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

        CartItem savedItem;

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            savedItem = cartItemRepository.save(item);
            log.debug("Updated existing item quantity: {}", item.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProductId(productId);
            newItem.setQuantity(quantity);
            newItem.setPrice(price);

            savedItem = cartItemRepository.save(newItem);
            cart.getItems().add(savedItem);
            log.debug("Created new cart item");
        }

        updateCartTotal(cart);
        cartRepository.save(cart);

        log.info("Item added to cart successfully: userId={}, productId={}", userId, productId);

        return savedItem;
    }

    @Override
    public void clearCart(Long userId) {
        log.debug("Clearing cart for user: {}", userId);

        ShoppingCart cart = getCart(userId);

        cartItemRepository.deleteAllByUserId(userId);
        cart.getItems().clear();

        updateCartTotal(cart);
        cartRepository.save(cart);

        log.info("Cart cleared for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public CartItem getCartItem(Long userId, Long itemId) {
        log.debug("Getting cart item: userId={}, itemId={}", userId, itemId);
        return cartItemRepository.findByIdAndCartUserId(itemId, userId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId, userId));
    }

    private ShoppingCart createNewCart(Long userId) {
        log.debug("Creating new cart for user: {}", userId);
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setTotalAmount(BigDecimal.ZERO);
        return cartRepository.save(cart);
    }

    private void updateCartTotal(ShoppingCart cart) {
        BigDecimal total = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(total);
        log.debug("Updated cart total: {}", total);
    }
}