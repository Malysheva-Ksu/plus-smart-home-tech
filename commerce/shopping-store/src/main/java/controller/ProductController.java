package controller;

import exception.ProductNotFoundException;
import model.shoppingStore.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import service.ProductService;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getProducts(
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

        return productsPage.getContent();
    }

    @GetMapping("/page")
    public Page<Product> getProductsPage(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

        if (category != null && search != null) {
            return productService.findByCategoryAndNameContaining(category, search, pageable);
        } else if (category != null) {
            return productService.findByCategory(category, pageable);
        } else if (search != null) {
            return productService.findByNameContaining(search, pageable);
        } else {
            return productService.findAll(pageable);
        }
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @GetMapping("/categories")
    public List<String> getCategories() {
        return productService.findAllCategories();
    }
}