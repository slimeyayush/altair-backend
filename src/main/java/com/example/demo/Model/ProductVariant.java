package com.example.demo.Model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    // 1. The main product the user is viewing (e.g., CPAP Machine)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_product_id", nullable = false)
    @JsonBackReference("product-variants")
    private Product parentProduct;

    // 2. The standalone product linked as an option (e.g., Mask)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "linked_product_id", nullable = false)
    @JsonIgnoreProperties({"variants", "additionalImages", "description", "category", "brand", "tag"})
    private Product linkedProduct;

    // 3. Display name for the dropdown (e.g., "Add Medium Full Face Mask")
    @Column(nullable = false)
    private String variantLabel;

    // 4. Bundle pricing. If null, your frontend should use linkedProduct.getPrice()
    private BigDecimal priceOverride;
}