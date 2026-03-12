package com.example.demo.Model;



import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Data
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    // Variant attributes
    private String variantType; // e.g., "Full Face Mask", "Color"
    private String variantSize; // e.g., "Medium", "Large"

    @Column(nullable = false)
    private Integer stockQuantity;

    // Optional: If a Large costs more than a Small.
    // If null, the frontend/backend will default to the base Product price.
    private BigDecimal priceOverride;
}
