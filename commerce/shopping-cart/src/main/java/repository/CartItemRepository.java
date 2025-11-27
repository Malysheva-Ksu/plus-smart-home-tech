package repository;

import model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndCartUserId(Long itemId, Long userId);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.cart WHERE ci.id = :itemId AND ci.cart.userId = :userId")
    Optional<CartItem> findByIdAndCartUserIdWithCart(@Param("itemId") Long itemId,
                                                     @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId AND ci.productId = :productId")
    Optional<CartItem> findByUserIdAndProductId(@Param("userId") Long userId,
                                                @Param("productId") Long productId);
}