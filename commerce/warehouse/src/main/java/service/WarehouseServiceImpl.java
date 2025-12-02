package service;

import exception.InsufficientStockException;
import exception.StockNotFoundException;
import model.MovementType;
import model.warehouse.StockItem;
import model.warehouse.StockMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.StockItemRepository;
import repository.StockMovementRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WarehouseServiceImpl implements WarehouseService {

    private static final Logger log = LoggerFactory.getLogger(WarehouseServiceImpl.class);

    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;

    public WarehouseServiceImpl(StockItemRepository stockItemRepository,
                                StockMovementRepository stockMovementRepository) {
        this.stockItemRepository = stockItemRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StockItem> getStockItem(Long productId) {
        log.debug("Getting stock item for product: {}", productId);
        return stockItemRepository.findByProductId(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Integer> getAvailableQuantities(List<Long> productIds) {
        log.debug("Getting available quantities for products: {}", productIds);

        List<StockItem> stockItems = stockItemRepository.findByProductIdIn(productIds);

        return stockItems.stream()
                .collect(Collectors.toMap(
                        StockItem::getProductId,
                        item -> item.getQuantity() - item.getReserved()
                ));
    }

    @Override
    public void reserveStock(Map<Long, Integer> reservations) {
        log.debug("Reserving stock: {}", reservations);

        for (Map.Entry<Long, Integer> entry : reservations.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            StockItem stockItem = stockItemRepository.findByProductId(productId)
                    .orElseThrow(() -> new StockNotFoundException("Stock not found for product: " + productId));

            int availableQuantity = stockItem.getQuantity() - stockItem.getReserved();
            if (availableQuantity < quantity) {
                throw new InsufficientStockException(
                        productId, quantity, availableQuantity
                );
            }

            stockItem.setReserved(stockItem.getReserved() + quantity);
            stockItemRepository.save(stockItem);

            StockMovement movement = new StockMovement(
                    productId,
                    MovementType.RESERVE,
                    quantity,
                    "RESERVATION",
                    String.format("Reserved %d units for order", quantity)
            );
            stockMovementRepository.save(movement);

            log.info("Stock reserved: productId={}, quantity={}", productId, quantity);
        }
    }

    @Override
    public void releaseStock(Map<Long, Integer> reservations) {
        log.debug("Releasing stock reservations: {}", reservations);

        for (Map.Entry<Long, Integer> entry : reservations.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();

            StockItem stockItem = stockItemRepository.findByProductId(productId)
                    .orElseThrow(() -> new StockNotFoundException("Stock not found for product: " + productId));

            int newReserved = Math.max(0, stockItem.getReserved() - quantity);
            stockItem.setReserved(newReserved);
            stockItemRepository.save(stockItem);

            StockMovement movement = new StockMovement(
                    productId,
                    MovementType.RELEASE,
                    quantity,
                    "RESERVATION_RELEASE",
                    String.format("Released reservation for %d units", quantity)
            );
            stockMovementRepository.save(movement);

            log.info("Stock reservation released: productId={}, quantity={}", productId, quantity);
        }
    }

    @Override
    public void updateStock(Long productId, Integer quantity, MovementType movementType, String reference) {
        log.debug("Updating stock: productId={}, quantity={}, type={}, reference={}",
                productId, quantity, movementType, reference);

        StockItem stockItem = stockItemRepository.findByProductId(productId)
                .orElseGet(() -> createNewStockItem(productId));

        switch (movementType) {
            case IN:
                stockItem.setQuantity(stockItem.getQuantity() + quantity);
                break;
            case OUT:
                int available = stockItem.getQuantity() - stockItem.getReserved();
                if (available < quantity) {
                    throw new InsufficientStockException(productId, quantity, available);
                }
                stockItem.setQuantity(stockItem.getQuantity() - quantity);
                break;
            case ADJUSTMENT:
                stockItem.setQuantity(quantity);
                break;
            default:
                throw new IllegalArgumentException("Unsupported movement type for stock update: " + movementType);
        }

        stockItemRepository.save(stockItem);

        StockMovement movement = new StockMovement(
                productId,
                movementType,
                quantity,
                reference,
                String.format("Stock %s: %d units", movementType.toString().toLowerCase(), quantity)
        );
        stockMovementRepository.save(movement);

        log.info("Stock updated: productId={}, newQuantity={}, type={}",
                productId, stockItem.getQuantity(), movementType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovement> getStockMovements(Long productId, int page, int size) {
        log.debug("Getting stock movements for product: {}, page: {}, size: {}", productId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockItem> getLowStockItems(int threshold) {
        log.debug("Getting low stock items with threshold: {}", threshold);

        return stockItemRepository.findLowStockItems(threshold);
    }

    @Override
    public StockItem addStock(Long productId, Integer quantity, String reference) {
        log.debug("Adding stock: productId={}, quantity={}", productId, quantity);
        return updateStockWithMovement(productId, quantity, MovementType.IN, reference);
    }

    @Override
    public StockItem removeStock(Long productId, Integer quantity, String reference) {
        log.debug("Removing stock: productId={}, quantity={}", productId, quantity);
        return updateStockWithMovement(productId, quantity, MovementType.OUT, reference);
    }

    private StockItem updateStockWithMovement(Long productId, Integer quantity, MovementType movementType, String reference) {
        updateStock(productId, quantity, movementType, reference);
        return getStockItem(productId)
                .orElseThrow(() -> new StockNotFoundException("Stock not found after update for product: " + productId));
    }

    private StockItem createNewStockItem(Long productId) {
        log.debug("Creating new stock item for product: {}", productId);
        StockItem stockItem = new StockItem();
        stockItem.setProductId(productId);
        stockItem.setQuantity(0);
        stockItem.setReserved(0);
        return stockItemRepository.save(stockItem);
    }
}