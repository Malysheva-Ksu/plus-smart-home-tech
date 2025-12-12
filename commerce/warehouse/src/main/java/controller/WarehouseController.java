package controller;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import model.warehouse.AddQuantityRequest;
import model.warehouse.NewProductRequest;
import model.warehouse.WarehouseAddressDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.WarehouseService;

@RestController
@RequestMapping(path = "/api/v1/warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    @GetMapping("/address")
    public ResponseEntity<WarehouseAddressDto> getWarehouseAddress() {
        WarehouseAddressDto address = warehouseService.getAddress();
        return ResponseEntity.ok(address);
    }

    @PutMapping
    public ResponseEntity<Void> addNewProduct(@RequestBody NewProductRequest request) {

        warehouseService.saveProduct(request);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/add")
    public ResponseEntity<Void> addProductQuantity(@RequestBody AddQuantityRequest request) {

        warehouseService.addStock(request.getProductId(), request.getQuantity());

        return ResponseEntity.noContent().build();
        }

}