package com.example.demo.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String brand;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal oldPrice;

    // Master stock. If this is a standalone mask, its stock lives here.
    @Column(nullable = false)
    private Integer stockQuantity;

    private String category;

    private String tag;

    @Column(name = "image_url")
    private String imageUrl;

    // Explicitly lock the JSON mapping so Spring doesn't expect "active" instead
    @JsonProperty("isActive")
    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

    // Mapped to the new 'parentProduct' field in ProductVariant
    @OneToMany(mappedBy = "parentProduct", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("product-variants")
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("product-images")
    private List<ProductImage> additionalImages = new ArrayList<>();
}