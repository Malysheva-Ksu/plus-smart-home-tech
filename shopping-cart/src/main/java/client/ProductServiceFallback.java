package client;

import model.Product;
import jakarta.ws.rs.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductServiceFallback implements ProductServiceClient {

    private static final Logger log = LoggerFactory.getLogger(ProductServiceFallback.class);

    @Override
    public Product getProduct(Long productId) {
        log.warn("Fallback: Product service unavailable for productId: {}", productId);
        throw new ServiceUnavailableException("Product service temporarily unavailable");
    }


    @Override
    public Map<Long, Boolean> checkProductsAvailability(List<Long> productIds) {
        log.warn("Fallback: Product service unavailable for availability check");
        return productIds.stream()
                .collect(Collectors.toMap(id -> id, id -> false));
    }
}