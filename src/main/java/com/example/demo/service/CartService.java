package com.example.demo.service;

import com.example.demo.DTO.request.CartDeltaDTO;
import com.example.demo.DTO.request.CartItemRequestDTO;
import com.example.demo.DTO.response.CartItemResponseDTO;
import com.example.demo.Model.CartItem;
import com.example.demo.Model.Customer;
import com.example.demo.Model.Product;
import com.example.demo.Model.ProductVariant;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.CartMapper;
import com.example.demo.repo.CustomerRepository;
import com.example.demo.repo.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public CartService(CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       AuthenticatedUserService authenticatedUserService) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional(readOnly = true)
    public List<CartItemResponseDTO> getCart() {
        Customer customer = authenticatedUserService.getOrCreateCurrentCustomer();
        return CartMapper.toDtoList(customer.getCartItems());
    }

    @Transactional
    public List<CartItemResponseDTO> addToCart(CartItemRequestDTO payload) {
        Customer customer = authenticatedUserService.getOrCreateCurrentCustomer();
        Long productId = payload.getProductId();
        Integer quantity = payload.getQuantity();
        Long variantId = payload.getVariantId();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Optional<CartItem> existingItem = findMatchingItem(customer, productId, variantId);

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCustomer(customer);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);

            if (variantId != null) {
                ProductVariant selectedVariant = product.getVariants().stream()
                        .filter(v -> v.getId().equals(variantId))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Variant", variantId));
                newItem.setProductVariant(selectedVariant);
            }
            customer.getCartItems().add(newItem);
        }

        customerRepository.save(customer);
        return CartMapper.toDtoList(customer.getCartItems());
    }

    @Transactional
    public List<CartItemResponseDTO> updateQuantity(Long productId, CartDeltaDTO payload) {
        Customer customer = authenticatedUserService.getOrCreateCurrentCustomer();
        Integer delta = payload.getDelta();
        Long variantId = payload.getVariantId();

        findMatchingItem(customer, productId, variantId).ifPresent(item -> {
            int newQuantity = item.getQuantity() + delta;
            if (newQuantity <= 0) {
                customer.getCartItems().remove(item);
            } else {
                item.setQuantity(newQuantity);
            }
        });

        customerRepository.save(customer);
        return CartMapper.toDtoList(customer.getCartItems());
    }

    @Transactional
    public List<CartItemResponseDTO> removeFromCart(Long productId, Long variantId) {
        Customer customer = authenticatedUserService.getOrCreateCurrentCustomer();
        customer.getCartItems().removeIf(item -> matches(item, productId, variantId));
        customerRepository.save(customer);
        return CartMapper.toDtoList(customer.getCartItems());
    }

    @Transactional
    public void clearCart() {
        Customer customer = authenticatedUserService.getOrCreateCurrentCustomer();
        customer.getCartItems().clear();
        customerRepository.save(customer);
    }

    @Transactional
    public List<CartItemResponseDTO> syncCart(List<CartItemRequestDTO> localCart) {
        Customer customer = authenticatedUserService.getOrCreateCurrentCustomer();
        customer.getCartItems().clear();

        for (CartItemRequestDTO item : localCart) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                CartItem cartItem = new CartItem();
                cartItem.setCustomer(customer);
                cartItem.setProduct(product);
                cartItem.setQuantity(item.getQuantity());
                customer.getCartItems().add(cartItem);
            });
        }

        customerRepository.save(customer);
        return CartMapper.toDtoList(customer.getCartItems());
    }

    private Optional<CartItem> findMatchingItem(Customer customer, Long productId, Long variantId) {
        return customer.getCartItems().stream()
                .filter(item -> matches(item, productId, variantId))
                .findFirst();
    }

    private boolean matches(CartItem item, Long productId, Long variantId) {
        boolean productMatch = item.getProduct().getId().equals(productId);
        boolean variantMatch = (variantId == null)
                ? item.getProductVariant() == null
                : (item.getProductVariant() != null
                        && item.getProductVariant().getId().equals(variantId));
        return productMatch && variantMatch;
    }
}
