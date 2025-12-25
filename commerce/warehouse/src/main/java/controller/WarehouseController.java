package controller;

import model.warehouse.StockItemResponse;
import org.springframework.web.bind.annotation.RequestBody;
import model.warehouse.AddQuantityRequest;
import model.warehouse.NewProductRequest;
import model.warehouse.AddressDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.WarehouseService;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/address")
    public ResponseEntity<AddressDto> getWarehouseAddress() {
        AddressDto address = warehouseService.getAddress();
        return ResponseEntity.ok(address);
    }

    @GetMapping("/stock/{productId}")
    public ResponseEntity<StockItemResponse> getStock(@PathVariable UUID productId) {
        StockItemResponse stock = warehouseService.getStock(productId);
        return ResponseEntity.ok(stock);
    }

    @PutMapping
    public ResponseEntity<Void> addNewProduct(@RequestBody NewProductRequest request) {

        if (request.getQuantity() == null) {
            request.setQuantity(0);
        }

        warehouseService.saveProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addProductQuantity(@RequestBody AddQuantityRequest request) {

        Integer qtyToAdd = request.getQuantity() != null ? request.getQuantity() : 0;

        warehouseService.addStock(request.getProductId(), qtyToAdd);

        return ResponseEntity.ok().build();
        }

}