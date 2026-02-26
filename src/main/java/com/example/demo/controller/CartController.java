package com.example.demo.controller;



import com.example.demo.Model.CartItem;
import com.example.demo.Model.Customer;
import com.example.demo.Model.Product;
import com.example.demo.repo.CustomerRepository;
import com.example.demo.repo.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CartController(CustomerRepository customerRepository, ProductRepository productRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    private Customer getAuthenticatedCustomer() {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();
        return customerRepository.findByEmail(identifier)
                .orElseGet(() -> customerRepository.findByPhoneNumber(identifier)
                        .orElseGet(() -> {
                            Customer newCustomer = new Customer();
                            if (identifier.contains("@")) newCustomer.setEmail(identifier);
                            else newCustomer.setPhoneNumber(identifier);
                            return customerRepository.save(newCustomer);
                        }));
    }

    // 1. GET CART: Pulls the cart from the database
    // 1. GET CART: Pulls the cart from the database
    @GetMapping
    @Transactional
    public ResponseEntity<List<CartItem>> getCart() {
        Customer customer = getAuthenticatedCustomer();
        return ResponseEntity.ok(customer.getCartItems());
    }

    // 2. SYNC/ADD ITEM
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Integer> payload) {
        Customer customer = getAuthenticatedCustomer();
        Long productId = Long.valueOf(payload.get("productId"));
        Integer quantity = payload.get("quantity");

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = customer.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCustomer(customer);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            customer.getCartItems().add(newItem);
        }

        customerRepository.save(customer);
        return ResponseEntity.ok(customer.getCartItems());
    }

    // 3. UPDATE QUANTITY
    @PutMapping("/update/{productId}")
    @Transactional
    public ResponseEntity<?> updateQuantity(@PathVariable Long productId, @RequestBody Map<String, Integer> payload) {
        Customer customer = getAuthenticatedCustomer();
        Integer delta = payload.get("delta"); // expecting 1 or -1

        customer.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(item -> {
                    int newQuantity = item.getQuantity() + delta;
                    if (newQuantity <= 0) {
                        customer.getCartItems().remove(item);
                    } else {
                        item.setQuantity(newQuantity);
                    }
                });

        customerRepository.save(customer);
        return ResponseEntity.ok(customer.getCartItems());
    }

    // 4. REMOVE ITEM
    @DeleteMapping("/remove/{productId}")
    @Transactional
    public ResponseEntity<?> removeFromCart(@PathVariable Long productId) {
        Customer customer = getAuthenticatedCustomer();
        customer.getCartItems().removeIf(item -> item.getProduct().getId().equals(productId));
        customerRepository.save(customer);
        return ResponseEntity.ok(customer.getCartItems());
    }

    // 5. CLEAR CART (Call this after successful checkout)
    @DeleteMapping("/clear")
    @Transactional
    public ResponseEntity<?> clearCart() {
        Customer customer = getAuthenticatedCustomer();
        customer.getCartItems().clear();
        customerRepository.save(customer);
        return ResponseEntity.ok().build();
    }
}
