package controller;

import dto.ProductDto;
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

    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
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
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
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

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return ResponseEntity.ok(product);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productService.findAllCategories());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}