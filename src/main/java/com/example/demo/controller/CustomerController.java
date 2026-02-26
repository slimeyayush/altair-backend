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

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public CustomerController(CustomerRepository customerRepository, ProductRepository productRepository) {
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @PostMapping("/sync-cart")
    @Transactional
    public ResponseEntity<?> syncCart(@RequestBody List<Map<String, Integer>> localCart) {
        // 1. Get identifier (Email or Phone) from the Firebase token via Security Context
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Find or Create the Customer
        Customer customer = customerRepository.findByEmail(identifier)
                .orElseGet(() -> customerRepository.findByPhoneNumber(identifier)
                        .orElseGet(() -> {
                            Customer newCustomer = new Customer();
                            if (identifier.contains("@")) {
                                newCustomer.setEmail(identifier);
                            } else {
                                newCustomer.setPhoneNumber(identifier);
                            }
                            return customerRepository.save(newCustomer);
                        }));

        // 3. Clear existing DB cart (simplified sync: local overrides/merges into DB)
        customer.getCartItems().clear();

        // 4. Rebuild the cart from the incoming React payload
        for (Map<String, Integer> item : localCart) {
            Long productId = Long.valueOf(item.get("productId"));
            Integer quantity = item.get("quantity");

            productRepository.findById(productId).ifPresent(product -> {
                CartItem cartItem = new CartItem();
                cartItem.setCustomer(customer);
                cartItem.setProduct(product);
                cartItem.setQuantity(quantity);
                customer.getCartItems().add(cartItem);
            });
        }

        customerRepository.save(customer);

        // Return the updated list of items to React
        return ResponseEntity.ok(customer.getCartItems());
    }
}
