package repository;

import model.shoppingCart.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID; // Добавлен для типа UUID

@Repository
public interface CartRepository extends JpaRepository<ShoppingCart, UUID> {

    Optional<ShoppingCart> findByUsername(String username);

    @Query("SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.items WHERE c.username = :username")
    Optional<ShoppingCart> findByUsernameWithItems(@Param("username") String username);

    boolean existsByUsername(String username);
}