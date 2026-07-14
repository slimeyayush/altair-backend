package com.example.demo.mapper;

import com.example.demo.DTO.response.CartItemResponseDTO;
import com.example.demo.Model.CartItem;
import com.example.demo.Model.Product;
import com.example.demo.Model.ProductVariant;

import java.util.List;

public final class CartMapper {

    private CartMapper() {}

    public static CartItemResponseDTO toDto(CartItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getProductVariant();

        return new CartItemResponseDTO(
                item.getId(),
                product != null ? product.getId() : null,
                product != null ? product.getName() : null,
                product != null ? product.getImageUrl() : null,
                product != null ? product.getPrice() : null,
                item.getQuantity(),
                variant != null ? variant.getId() : null,
                variant != null ? variant.getVariantLabel() : null,
                variant != null ? variant.getPriceOverride() : null,
                (variant != null && variant.getLinkedProduct() != null)
                        ? variant.getLinkedProduct().getPrice()
                        : null,
                product != null ? product.getStockQuantity() : null
        );
    }

    public static List<CartItemResponseDTO> toDtoList(List<CartItem> items) {
        return items.stream().map(CartMapper::toDto).toList();
    }
}
