package client;

import model.warehouse.AddQuantityRequest;
import model.warehouse.NewProductRequest;
import model.warehouse.StockItemResponse;
import model.warehouse.WarehouseAddressDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseServiceClient {

    @GetMapping("/stock/{productId}")
    ResponseEntity<StockItemResponse> getStock(@PathVariable("productId") UUID productId);

    @GetMapping("/address")
    ResponseEntity<WarehouseAddressDto> getWarehouseAddress();

    @PutMapping
    ResponseEntity<Void> addNewProduct(@RequestBody NewProductRequest request);

    @PostMapping("/add")
    ResponseEntity<Void> addStock(@RequestBody AddQuantityRequest request);

}