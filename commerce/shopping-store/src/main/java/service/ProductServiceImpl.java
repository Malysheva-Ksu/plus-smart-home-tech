package service;

import dto.ProductDto;
import exception.ProductNotFoundException;
import model.shoppingStore.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID; // Добавлен для типа UUID

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findByCategory(String category, Pageable pageable) {
        return productRepository.findByProductCategory(category, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findByNameContaining(String search, Pageable pageable) {
        return productRepository.findByProductNameContainingIgnoreCase(search, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> findByCategoryAndNameContaining(String category, String search, Pageable pageable) {
        return productRepository.findByProductCategoryAndProductNameContainingIgnoreCase(category, search, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllCategories() {
        return productRepository.findAllDistinctProductCategory();
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product saveFromDto(ProductDto dto) {
        Product product;

        if (dto.getProductId() != null) {
            product = productRepository.findById(dto.getProductId()).orElse(new Product());
            product.setProductId(dto.getProductId());
        } else {
            product = new Product();
        }

        product.setProductName(dto.getProductName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setProductCategory(dto.getProductCategory());
        product.setImageSrc(dto.getImageSrc());

        product.setQuantityState(dto.getQuantityState());
        product.setProductState(dto.getProductState());

        return productRepository.save(product);
    }

    @Override
    public void deactivateProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));

        product.setProductState("DEACTIVATE");

        productRepository.save(product);
    }

    @Override
    public void deleteById(UUID id) {
        productRepository.deleteById(id);
    }
    @Override
    @Transactional(readOnly = true)
    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public void setQuantityState(UUID productId, String quantityState) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId.toString()));

        product.setQuantityState(quantityState);

        productRepository.save(product);
    }
}