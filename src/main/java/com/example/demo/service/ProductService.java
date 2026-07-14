package com.example.demo.service;

import com.example.demo.DTO.request.StockUpdateDTO;
import com.example.demo.DTO.response.ProductDetailDTO;
import com.example.demo.DTO.response.ProductSummaryDTO;
import com.example.demo.Model.Product;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.repo.ProductRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // --- Entity returns (admin endpoints still use these) ---

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    // --- DTO returns (customer-facing endpoints) ---
    // Mapping happens inside the @Transactional boundary so lazy collections
    // (variants, additionalImages, linkedProduct) initialize cleanly.

    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> findActiveAsDto() {
        return ProductMapper.toSummaryList(productRepository.findByIsActiveTrue());
    }

    @Transactional(readOnly = true)
    public ProductDetailDTO findDetailById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return ProductMapper.toDetail(product);
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> searchAsDto(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return ProductMapper.toSummaryList(productRepository.searchProducts(query));
    }

    @Transactional(readOnly = true)
    public List<ProductSummaryDTO> findByCategoryAsDto(String category) {
        return ProductMapper.toSummaryList(productRepository.findByCategoryAndIsActiveTrue(category));
    }

    // --- Mutations (admin) ---

    @CacheEvict(value = {"productsCache", "singleProductCache", "categoryCache"}, allEntries = true)
    @Transactional
    public Product create(Product product) {
        product.setId(null);
        if (product.getIsActive() == null) {
            product.setIsActive(true);
        }
        attachVariantsAndImages(product, product);
        return productRepository.save(product);
    }

    @CacheEvict(value = {"productsCache", "singleProductCache", "categoryCache"}, allEntries = true)
    @Transactional
    public Product update(Long id, Product details) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        existing.setName(details.getName());
        existing.setBrand(details.getBrand());
        existing.setDescription(details.getDescription());
        existing.setPrice(details.getPrice());
        existing.setOldPrice(details.getOldPrice());
        existing.setStockQuantity(details.getStockQuantity());
        existing.setCategory(details.getCategory());
        existing.setTag(details.getTag());
        existing.setImageUrl(details.getImageUrl());

        if (details.getIsActive() != null) {
            existing.setIsActive(details.getIsActive());
        }

        existing.getVariants().clear();
        existing.getAdditionalImages().clear();
        attachVariantsAndImages(details, existing);

        return productRepository.save(existing);
    }

    @CacheEvict(value = {"productsCache", "singleProductCache", "categoryCache"}, allEntries = true)
    @Transactional
    public Product updateStock(Long id, StockUpdateDTO payload) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        product.setStockQuantity(payload.getStockQuantity());
        return productRepository.save(product);
    }

    @CacheEvict(value = {"productsCache", "singleProductCache", "categoryCache"}, allEntries = true)
    @Transactional
    public Product toggleVisibility(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        Boolean current = product.getIsActive();
        product.setIsActive(current == null || !current);
        return productRepository.save(product);
    }

    @CacheEvict(value = {"productsCache", "singleProductCache", "categoryCache"}, allEntries = true)
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        try {
            productRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw e;
        }
    }

    private void attachVariantsAndImages(Product source, Product target) {
        if (source.getVariants() != null) {
            source.getVariants().forEach(variant -> {
                variant.setParentProduct(target);

                Long linkedId = variant.getLinkedProductId() != null
                        ? variant.getLinkedProductId()
                        : (variant.getLinkedProduct() != null ? variant.getLinkedProduct().getId() : null);

                if (linkedId != null) {
                    Product linked = productRepository.findById(linkedId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Linked product (variant target)", linkedId));
                    variant.setLinkedProduct(linked);
                }

                if (source != target) {
                    target.getVariants().add(variant);
                }
            });
        }
        if (source.getAdditionalImages() != null) {
            source.getAdditionalImages().forEach(img -> {
                img.setProduct(target);
                if (source != target) {
                    target.getAdditionalImages().add(img);
                }
            });
        }
    }
}
