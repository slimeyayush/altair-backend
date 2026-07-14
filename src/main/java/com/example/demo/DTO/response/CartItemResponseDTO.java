package com.example.demo.DTO.response;

import java.math.BigDecimal;

/**
 * Cart row for the customer-facing cart view. Flattens product and variant info
 * into one object so the frontend doesn't traverse a nested entity graph.
 *
 * Line total = (productPrice + (variantPriceOverride ?? linkedProductPrice ?? 0)) * quantity
 */
public record CartItemResponseDTO(
        Long id,
        Long productId,
        String productName,
        String productImageUrl,
        BigDecimal productPrice,
        Integer quantity,
        Long variantId,
        String variantLabel,
        BigDecimal variantPriceOverride,
        BigDecimal linkedProductPrice,
        Integer productStockQuantity
) {}
