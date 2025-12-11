package repository;

import model.shoppingCart.CartItem; // Используем скорректированный класс CartItem
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID; // Добавлен для типа UUID

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByIdAndCartUsername(UUID itemId, String username);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.username = :username AND ci.productId = :productId")
    Optional<CartItem> findByCartUsernameAndProductId(@Param("username") String username,
                                                      @Param("productId") UUID productId);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.cart c WHERE ci.id = :itemId AND c.username = :username")
    Optional<CartItem> findByIdAndCartUsernameWithCart(@Param("itemId") UUID itemId,
                                                       @Param("username") String username);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.username = :username")
    void deleteAllByUsername(@Param("username") String username);
}