package com.example.demo.repo;

import com.example.demo.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Main storefront grid
    List<Product> findByIsActiveTrue();

    // 2. Custom methods to block archived products
    List<Product> findByCategoryAndIsActiveTrue(String category);

    List<Product> findByTagAndIsActiveTrue(String tag);

    // NEW: Added for when we build the Shop page sidebar filters
    List<Product> findByBrandAndIsActiveTrue(String brand);

    // 3. Updated search query to enforce the active flag AND search the brand field
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Product> searchProducts(@Param("query") String query);
}