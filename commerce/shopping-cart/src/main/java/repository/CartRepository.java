package repository;

import model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<ShoppingCart, Long> {

    Optional<ShoppingCart> findByUserId(Long userId);

    @Query("SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.items WHERE c.userId = :userId")
    Optional<ShoppingCart> findByUserIdWithItems(@Param("userId") Long userId);

    boolean existsByUserId(Long userId);
}