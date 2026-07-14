package com.example.demo.controller;

import com.example.demo.DTO.response.ProductDetailDTO;
import com.example.demo.DTO.response.ProductSummaryDTO;
import com.example.demo.Model.Category;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        // Category is flat — no relationships, safe to return as-is
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping
    public List<ProductSummaryDTO> getAllProducts() {
        return productService.findActiveAsDto();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductSummaryDTO>> searchProducts(@RequestParam("q") String query) {
        return ResponseEntity.ok(productService.searchAsDto(query));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDetailDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findDetailById(id));
    }

    @GetMapping("/category/{category}")
    public List<ProductSummaryDTO> getProductsByCategory(@PathVariable String category) {
        return productService.findByCategoryAsDto(category);
    }
}
