package repository;

import model.warehouse.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<ProductStock, UUID> {

    Optional<ProductStock> findByProductId(UUID productId);

    boolean existsByProductId(UUID productId);
}