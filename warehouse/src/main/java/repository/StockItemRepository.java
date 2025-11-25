package repository;

import model.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    Optional<StockItem> findByProductId(Long productId);

    List<StockItem> findByProductIdIn(List<Long> productIds);

    @Query("SELECT si FROM StockItem si WHERE (si.quantity - si.reserved) <= :threshold")
    List<StockItem> findLowStockItems(@Param("threshold") int threshold);

    @Query("SELECT si FROM StockItem si WHERE si.quantity > 0 AND (si.quantity - si.reserved) > 0")
    List<StockItem> findAvailableStockItems();

    @Modifying
    @Query("UPDATE StockItem si SET si.quantity = si.quantity + :quantity WHERE si.productId = :productId")
    void increaseQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE StockItem si SET si.quantity = si.quantity - :quantity WHERE si.productId = :productId AND si.quantity >= :quantity")
    int decreaseQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE StockItem si SET si.reserved = si.reserved + :quantity WHERE si.productId = :productId AND (si.quantity - si.reserved) >= :quantity")
    int reserveQuantity(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE StockItem si SET si.reserved = GREATEST(0, si.reserved - :quantity) WHERE si.productId = :productId")
    void releaseReservation(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    boolean existsByProductId(Long productId);
}