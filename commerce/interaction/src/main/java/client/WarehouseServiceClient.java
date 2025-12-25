package client;

import exception.ProductInShoppingCartLowQuantityInWarehouseException;
import model.delivery.BookedProductsDto;
import model.delivery.ShippedToDeliveryRequest;
import model.order.AssemblyProductsForOrderRequest;
import model.shoppingCart.ShoppingCart;
import model.warehouse.AddQuantityRequest;
import model.warehouse.NewProductRequest;
import model.warehouse.StockItemResponse;
import model.warehouse.AddressDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "warehouse", path = "/api/v1/warehouse")
public interface WarehouseServiceClient {

    @GetMapping("/stock/{productId}")
    ResponseEntity<StockItemResponse> getStock(@PathVariable("productId") UUID productId);

    @GetMapping("/address")
    AddressDto getWarehouseAddress();

    @PutMapping
    ResponseEntity<Void> addNewProduct(@RequestBody NewProductRequest request);

    @PostMapping("/add")
    ResponseEntity<Void> addStock(@RequestBody AddQuantityRequest request);

    @PostMapping("/return")
    void returnBookedProducts(@RequestBody Map<UUID, Integer> products);

    @GetMapping("/assembly")
    BookedProductsDto assemblyProducts(@RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping("/shipped")
    void shipProducts(@RequestBody ShippedToDeliveryRequest request);

    @PostMapping("/check")
    BookedProductsDto checkProductsQuantity(@RequestBody ShoppingCart cart)
            throws ProductInShoppingCartLowQuantityInWarehouseException;
}