package repository;

import model.shoppingStore.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID; // Добавлен для типа UUID

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    Page<Product> findByProductCategory(String productCategory, Pageable pageable);

    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);

    Page<Product> findByProductCategoryAndProductNameContainingIgnoreCase(String productCategory, String productName, Pageable pageable);

    @Query("SELECT DISTINCT p.productCategory FROM Product p WHERE p.productCategory IS NOT NULL")
    List<String> findAllDistinctProductCategory();

    @Query("SELECT p FROM Product p WHERE " +
            "(:productCategory IS NULL OR p.productCategory = :productCategory) AND " +
            "(:search IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findProductsWithFilters(@Param("productCategory") String productCategory,
                                          @Param("search") String search,
                                          Pageable pageable);

}