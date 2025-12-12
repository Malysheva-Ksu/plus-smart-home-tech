package repository;

import model.shoppingCart.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {

    Optional<ShoppingCart> findByUsernameAndIsActiveTrue(String username);

    Optional<ShoppingCart> findByUsername(String username);

    boolean existsByUsernameAndIsActiveTrue(String username);
}