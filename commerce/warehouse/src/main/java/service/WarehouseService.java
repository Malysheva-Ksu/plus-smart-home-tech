package service;

import model.MovementType;
import model.warehouse.StockItem;
import model.warehouse.StockMovement;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WarehouseService {

    Optional<StockItem> getStockItem(Long productId);

    Map<Long, Integer> getAvailableQuantities(List<Long> productIds);

    void reserveStock(Map<Long, Integer> reservations);

    void releaseStock(Map<Long, Integer> reservations);

    void updateStock(Long productId, Integer quantity, MovementType movementType, String reference);

    List<StockMovement> getStockMovements(Long productId, int page, int size);

    List<StockItem> getLowStockItems(int threshold);

    StockItem addStock(Long productId, Integer quantity, String reference);

    StockItem removeStock(Long productId, Integer quantity, String reference);
}