package client;

import model.shoppingStore.Product;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

@FeignClient(name = "shopping-store", path = "/api/v1/shopping-store")
public interface ProductServiceClient {

    @GetMapping("/{id}")
    Product getProduct(@PathVariable("id") Long id);

    @GetMapping
    List<Product> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    );

    @GetMapping("/categories")
    List<String> getCategories();
}