package client;

import model.shoppingStore.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping; // Добавлен для метода PUT
import org.springframework.web.bind.annotation.RequestBody; // Добавлен для тела запроса
import org.springframework.cloud.openfeign.FeignClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID; // Добавлен для типа UUID

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ProductServiceClient {

    @PutMapping
    Product addProduct(@RequestBody Product product);

    @GetMapping("/{productId}")
    Product getProduct(@PathVariable("productId") UUID productId);

    @GetMapping("/categories/{productCategory}")
    List<Product> getProductsByCategory(@PathVariable("productCategory") String productCategory);

    @GetMapping("/categories")
    List<String> getCategories();

    @GetMapping("/price")
    Map<UUID, BigDecimal> getProductsPrice(@RequestBody List<UUID> productsIds);
}