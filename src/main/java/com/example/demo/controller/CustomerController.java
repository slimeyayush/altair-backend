package com.example.demo.controller;

import com.example.demo.Model.CartItem;
import com.example.demo.Model.Customer;
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

    // NEW: Dedicated sync endpoint triggered strictly on login
    @PostMapping("/sync")
    @Transactional
    public ResponseEntity<?> syncCustomer() {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();

        if (identifier == null || identifier.equals("anonymousUser")) {
            return ResponseEntity.badRequest().body("No authenticated user found");
        }

        // Check if customer exists, create if they do not
        Customer customer = customerRepository.findByEmail(identifier)
                .orElseGet(() -> customerRepository.findByPhoneNumber(identifier).orElse(null));

        if (customer == null) {
            Customer newCustomer = new Customer();
            if (identifier.contains("@")) {
                newCustomer.setEmail(identifier);
            } else {
                newCustomer.setPhoneNumber(identifier);
            }
            customerRepository.save(newCustomer);
        }

        return ResponseEntity.ok("Customer profile synced");
    }

    // EXISTING: Untouched cart sync logic
    @PostMapping("/sync-cart")
    @Transactional
    public ResponseEntity<?> syncCart(@RequestBody List<Map<String, Integer>> localCart) {
        String identifier = SecurityContextHolder.getContext().getAuthentication().getName();

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

        customer.getCartItems().clear();

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
        return ResponseEntity.ok(customer.getCartItems());
    }
}