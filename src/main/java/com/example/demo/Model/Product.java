package com.example.demo.Model;



import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Entity
@Table(name = "products")
@Data // Lombok generates getters, setters, and constructors
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal oldPrice;

    @Column(nullable = false)
    private Integer stockQuantity;

    private String category;

    private String tag; // e.g., "Hot", "New", "Sale"

    @Column(name = "image_url")
    private String imageUrl;

    // Update this line in Product.java
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive = true;

    // Add this inside your Product.java class
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductVariant> variants = new ArrayList<>();

    // Add this inside your Product.java class
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ProductImage> additionalImages = new ArrayList<>();
}
