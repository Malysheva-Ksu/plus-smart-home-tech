package client;

import model.StockItem;
import jakarta.ws.rs.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import service.ReserveRequest;

@Component
public class WarehouseServiceFallback implements WarehouseServiceClient {

    private static final Logger log = LoggerFactory.getLogger(WarehouseServiceFallback.class);

    @Override
    public StockItem getStock(Long productId) {
        log.warn("Fallback: Warehouse service unavailable for productId: {}", productId);
        throw new ServiceUnavailableException("Warehouse service temporarily unavailable");
    }

    @Override
    public void reserveStock(ReserveRequest request) {
        log.warn("Fallback: Cannot reserve stock, warehouse service unavailable");
        throw new ServiceUnavailableException("Cannot reserve stock - warehouse service down");
    }

    @Override
    public void releaseStock(ReserveRequest request) {
        log.warn("Fallback: Cannot release stock, warehouse service unavailable");
    }
}