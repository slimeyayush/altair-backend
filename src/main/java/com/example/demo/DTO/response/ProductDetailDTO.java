package com.example.demo.DTO.response;

import java.math.BigDecimal;
import java.util.List;

/**
 * Full product representation for the detail page.
 * Includes variants (with their linked-product info flattened) and additional image URLs.
 */
public record ProductDetailDTO(
        Long id,
        String name,
        String brand,
        String description,
        BigDecimal price,
        BigDecimal oldPrice,
        Integer stockQuantity,
        String category,
        String tag,
        String imageUrl,
        Boolean isActive,
        List<String> additionalImages,
        List<VariantDTO> variants
) {
    /**
     * Flattened variant — replaces the nested ProductVariant -> linkedProduct graph
     * with the handful of fields the variant dropdown actually needs.
     */
    public record VariantDTO(
            Long id,
            String variantLabel,
            BigDecimal priceOverride,
            Long linkedProductId,
            String linkedProductName,
            BigDecimal linkedProductPrice,
            String linkedProductImageUrl,
            Integer linkedProductStock
    ) {}
}
