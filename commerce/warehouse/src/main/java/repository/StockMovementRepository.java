package repository;

import model.MovementType;
import model.StockMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);

    List<StockMovement> findByMovementTypeAndCreatedAtBetween(MovementType movementType,
                                                              LocalDateTime startDate,
                                                              LocalDateTime endDate);

    List<StockMovement> findByProductIdAndMovementTypeOrderByCreatedAtDesc(Long productId,
                                                                           MovementType movementType,
                                                                           Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.productId = :productId AND sm.createdAt BETWEEN :startDate AND :endDate ORDER BY sm.createdAt DESC")
    List<StockMovement> findByProductIdAndDateRange(@Param("productId") Long productId,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    Pageable pageable);

    @Query("SELECT COALESCE(SUM(sm.quantity), 0) FROM StockMovement sm WHERE sm.productId = :productId AND sm.movementType = :movementType AND sm.createdAt BETWEEN :startDate AND :endDate")
    Integer getMovementQuantitySum(@Param("productId") Long productId,
                                   @Param("movementType") MovementType movementType,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);
}