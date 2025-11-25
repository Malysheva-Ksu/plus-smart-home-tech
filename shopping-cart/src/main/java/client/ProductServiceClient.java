package client;

import model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "shopping-store", path = "/api/products")
public interface ProductServiceClient {

    @GetMapping("/{productId}")
    Product getProduct(@PathVariable("productId") Long productId);

    @GetMapping("/availability")
    Map<Long, Boolean> checkProductsAvailability(@RequestParam("productIds") List<Long> productIds);
}