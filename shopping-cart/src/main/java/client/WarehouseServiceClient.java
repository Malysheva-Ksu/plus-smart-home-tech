package client;

import model.StockItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import service.ReserveRequest;

@FeignClient(
        name = "warehouse",
        url = "${feign.client.config.warehouse.url:}",
        fallback = WarehouseServiceFallback.class
)
public interface WarehouseServiceClient {

    @GetMapping("/api/warehouse/stock/{productId}")
    StockItem getStock(@PathVariable("productId") Long productId);

    @PostMapping("/api/warehouse/reserve")
    void reserveStock(@RequestBody ReserveRequest request);

    @PostMapping("/api/warehouse/release")
    void releaseStock(@RequestBody ReserveRequest request);
}