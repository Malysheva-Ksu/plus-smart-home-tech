package controller;

import dto.ProductDto;
import dto.QuantityUpdateDto;
import exception.ProductNotFoundException;
import model.shoppingStore.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.ProductService;

import java.util.List;
import java.util.UUID; // Добавлен для типа UUID

@RestController
@RequestMapping("/api/v1/shopping-store")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PutMapping
    public ResponseEntity<Product> createOrUpdateProduct(@RequestBody ProductDto productDto) {
        Product product = productService.saveFromDto(productDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProduct(@PathVariable UUID productId) {
        Product product = productService.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/categories/{productCategory}")
    public ResponseEntity<List<Product>> getProductsByCategory(
            @PathVariable String productCategory,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("productName").ascending());
        Page<Product> productsPage = productService.findByCategory(productCategory, pageable);

        return ResponseEntity.ok(productsPage.getContent());
    }

    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("productName").ascending());
        Page<Product> productsPage;

        if (category != null && search != null) {
            productsPage = productService.findByCategoryAndNameContaining(category, search, pageable);
        } else if (category != null) {
            productsPage = productService.findByCategory(category, pageable);
        } else if (search != null) {
            productsPage = productService.findByNameContaining(search, pageable);
        } else {
            productsPage = productService.findAll(pageable);
        }

        return ResponseEntity.ok(productsPage.getContent());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Product>> getProductsPage(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("productName").ascending());
        Page<Product> productsPage;
        if (category != null && search != null) {
            productsPage = productService.findByCategoryAndNameContaining(category, search, pageable);
        } else if (category != null) {
            productsPage = productService.findByCategory(category, pageable);
        } else if (search != null) {
            productsPage = productService.findByNameContaining(search, pageable);
        } else {
            productsPage = productService.findAll(pageable);
        }

        return ResponseEntity.ok(productsPage);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.findAllCategories());
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID productId) {
        productService.deleteById(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/removeProductFromStore")
    public ResponseEntity<Void> removeProductFromStore(@RequestBody UUID productId) {
        productService.deactivateProduct(productId);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/setQuantityState")
    public ResponseEntity<Void> setProductQuantityState(@RequestBody QuantityUpdateDto updateDto) {
        productService.setQuantityState(updateDto.getProductId(), updateDto.getQuantityState());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/quantityState")
    public ResponseEntity<Void> setProductQuantityState(
            @RequestParam UUID productId,
            @RequestParam String quantityState
    ) {
        productService.setQuantityState(productId, quantityState);

        return ResponseEntity.ok().build();
    }
}