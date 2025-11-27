package client;

import model.ReserveRequest;
import model.StockItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "warehouse-service", path = "/api/v1/warehouse")
public interface WarehouseServiceClient {

    @GetMapping("/stock/{productId}")
    StockItem getStock(@PathVariable("productId") Long productId);

    @PostMapping("/reserve")
    void reserveStock(@RequestBody ReserveRequest request);

    @PostMapping("/release")
    void releaseStock(@RequestBody ReserveRequest request);
}