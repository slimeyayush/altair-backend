package com.example.demo.mapper;

import com.example.demo.DTO.response.ProductDetailDTO;
import com.example.demo.DTO.response.ProductSummaryDTO;
import com.example.demo.Model.Product;
import com.example.demo.Model.ProductImage;
import com.example.demo.Model.ProductVariant;

import java.util.Collections;
import java.util.List;

/**
 * Entity-to-DTO conversion for products. Kept as static methods because
 * mappers are pure functions with no dependencies — no need for a Spring bean.
 *
 * Important: callers must invoke these inside an active transaction so that
 * lazy collections (variants, additionalImages) can initialize. The service
 * layer already wraps reads in @Transactional, so this is handled.
 */
public final class ProductMapper {

    private ProductMapper() {}

    public static ProductSummaryDTO toSummary(Product product) {
        boolean hasVariants = product.getVariants() != null && !product.getVariants().isEmpty();
        return new ProductSummaryDTO(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                product.getPrice(),
                product.getOldPrice(),
                product.getStockQuantity(),
                product.getCategory(),
                product.getTag(),
                product.getImageUrl(),
                product.getIsActive(),
                hasVariants
        );
    }

    public static List<ProductSummaryDTO> toSummaryList(List<Product> products) {
        return products.stream().map(ProductMapper::toSummary).toList();
    }

    public static ProductDetailDTO toDetail(Product product) {
        List<String> imageUrls = product.getAdditionalImages() == null
                ? Collections.emptyList()
                : product.getAdditionalImages().stream()
                        .map(ProductImage::getImageUrl)
                        .toList();

        List<ProductDetailDTO.VariantDTO> variants = product.getVariants() == null
                ? Collections.emptyList()
                : product.getVariants().stream()
                        .map(ProductMapper::toVariantDto)
                        .toList();

        return new ProductDetailDTO(
                product.getId(),
                product.getName(),
                product.getBrand(),
                product.getDescription(),
                product.getPrice(),
                product.getOldPrice(),
                product.getStockQuantity(),
                product.getCategory(),
                product.getTag(),
                product.getImageUrl(),
                product.getIsActive(),
                imageUrls,
                variants
        );
    }

    private static ProductDetailDTO.VariantDTO toVariantDto(ProductVariant variant) {
        Product linked = variant.getLinkedProduct();
        return new ProductDetailDTO.VariantDTO(
                variant.getId(),
                variant.getVariantLabel(),
                variant.getPriceOverride(),
                linked != null ? linked.getId() : null,
                linked != null ? linked.getName() : null,
                linked != null ? linked.getPrice() : null,
                linked != null ? linked.getImageUrl() : null,
                linked != null ? linked.getStockQuantity() : null
        );
    }
}
