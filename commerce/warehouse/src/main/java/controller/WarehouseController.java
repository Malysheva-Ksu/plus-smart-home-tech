package controller;

import exception.StockNotFoundException;
import exception.StockReservationException;
import model.warehouse.ReserveRequest;
import model.warehouse.StockItem;
import model.warehouse.StockMovement;
import model.warehouse.StockUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.WarehouseService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    private static final Logger log = LoggerFactory.getLogger(WarehouseController.class);

    private final WarehouseService warehouseService;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public WarehouseController(WarehouseService warehouseService,
                               CircuitBreakerFactory circuitBreakerFactory) {
        this.warehouseService = warehouseService;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping("/stock/{productId}")
    public ResponseEntity<StockItem> getStock(@PathVariable Long productId) {
        log.debug("Getting stock for product: {}", productId);

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("stockService");

        StockItem stockItem = circuitBreaker.run(() -> {
            return warehouseService.getStockItem(productId)
                    .orElseThrow(() -> new StockNotFoundException("Stock not found for product: " + productId));
        }, throwable -> {
            log.warn("Fallback: Returning default stock for product: {}", productId);
            return new StockItem(productId, 0, 0);
        });

        return ResponseEntity.ok(stockItem);
    }

    @GetMapping("/availability")
    public ResponseEntity<Map<Long, Integer>> checkAvailability(@RequestParam List<Long> productIds) {
        log.debug("Checking availability for products: {}", productIds);

        Map<Long, Integer> availability = warehouseService.getAvailableQuantities(productIds);
        return ResponseEntity.ok(availability);
    }

    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveStock(@RequestBody ReserveRequest request) {
        log.debug("Reserving stock: {}", request.getReservations());

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("reserveStock");

        circuitBreaker.run(() -> {
            warehouseService.reserveStock(request.getReservations());
            return null;
        }, throwable -> {
            log.error("Failed to reserve stock: {}", throwable.getMessage());
            throw new StockReservationException("Unable to reserve stock");
        });

        log.info("Stock reserved successfully: {}", request.getReservations());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseStock(@RequestBody ReserveRequest request) {
        log.debug("Releasing stock reservation: {}", request.getReservations());

        warehouseService.releaseStock(request.getReservations());

        log.info("Stock reservation released: {}", request.getReservations());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    public ResponseEntity<Void> updateStock(@RequestBody StockUpdateRequest request) {
        log.debug("Updating stock: productId={}, quantity={}",
                request.getProductId(), request.getQuantity());

        warehouseService.updateStock(request.getProductId(), request.getQuantity(),
                request.getMovementType(), request.getReference());

        log.info("Stock updated: productId={}, newQuantity={}",
                request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/movements/{productId}")
    public ResponseEntity<List<StockMovement>> getStockMovements(@PathVariable Long productId,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "50") int size) {
        log.debug("Getting stock movements for product: {}", productId);

        List<StockMovement> movements = warehouseService.getStockMovements(productId, page, size);
        return ResponseEntity.ok(movements);
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<StockItem>> getLowStockItems(@RequestParam(defaultValue = "10") int threshold) {
        log.debug("Getting low stock items with threshold: {}", threshold);

        List<StockItem> lowStockItems = warehouseService.getLowStockItems(threshold);
        return ResponseEntity.ok(lowStockItems);
    }
}