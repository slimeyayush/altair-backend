package com.example.demo.DTO.response;

import java.math.BigDecimal;

/**
 * Lightweight product representation for grid, search, and category list views.
 * Excludes variants and additionalImages — fetch the detail endpoint for those.
 */
public record ProductSummaryDTO(
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
        boolean hasVariants
) {}
