package service;

import model.shoppingStore.Product;
import model.shoppingStore.ProductDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductService {

    public Page<Product> findByCategory(String category, Pageable pageable);

    public Page<Product> findByNameContaining(String search, Pageable pageable);

    public Page<Product> findByCategoryAndNameContaining(String category, String search, Pageable pageable);

    public Optional<Product> findById(UUID id);

    public List<String> findAllCategories();

    public Product save(Product product);

    public Product saveFromDto(ProductDto dto);

    public void deactivateProduct(UUID productId);

    public void deleteById(UUID id);

    public Page<Product> findAll(Pageable pageable);

    public void setQuantityState(UUID productId, String quantityState);
}