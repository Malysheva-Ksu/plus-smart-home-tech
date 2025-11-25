package service;

import model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategory(String category, Pageable pageable);

    Page<Product> findByNameContaining(String search, Pageable pageable);

    Page<Product> findByCategoryAndNameContaining(String category, String search, Pageable pageable);

    Optional<Product> findById(Long id);

    List<String> findAllCategories();

    Product save(Product product);

    void deleteById(Long id);
}