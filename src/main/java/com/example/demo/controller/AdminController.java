package com.example.demo.controller;



import com.example.demo.Model.AdminUser;
import com.example.demo.Model.Order;
import com.example.demo.Model.Product;
import com.example.demo.repo.AdminUserRepository;
import com.example.demo.repo.OrderRepository;
import com.example.demo.repo.ProductRepository;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")

public class AdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;
    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerNewAdmin(@RequestBody Map<String, String> payload) {
        String newUsername = payload.get("username");
        String rawPassword = payload.get("password");

        if (adminUserRepository.findByUsername(newUsername).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        AdminUser newUser = new AdminUser();
        newUser.setUsername(newUsername);
        // Hash the password before saving to the database
        newUser.setPassword(passwordEncoder.encode(rawPassword));

        adminUserRepository.save(newUser);
        return ResponseEntity.ok("New admin created successfully");
    }

    // --- Orders ---
    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    @DeleteMapping("/inventory/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        try {
            productRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Cannot delete product: It is linked to existing order records.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error deleting product.");
        }
    }

    @PostMapping("/orders/{id}/mark-paid")
    public ResponseEntity<Order> markOrderAsPaid(@PathVariable Long id) {
        try {
            Order paidOrder = orderService.confirmOrderPayment(id);
            return ResponseEntity.ok(paidOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        return orderRepository.findById(id).map(order -> {
            order.setStatus(Order.OrderStatus.valueOf(payload.get("status").toUpperCase()));
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }

    // --- Inventory ---
    @GetMapping("/inventory")
    public List<Product> getInventory() {
        return productRepository.findAll();
    }

    @PutMapping("/inventory/{id}")
    public ResponseEntity<Product> updateStock(@PathVariable Long id, @RequestBody Map<String, Integer> payload) {
        return productRepository.findById(id).map(product -> {
            product.setStockQuantity(payload.get("stockQuantity"));
            return ResponseEntity.ok(productRepository.save(product));
        }).orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        // Ensure ID is null so Hibernate knows to INSERT rather than UPDATE
        product.setId(null);
        Product savedProduct = productRepository.save(product);
        return ResponseEntity.ok(savedProduct);
    }
    @PutMapping("/inventory/{id}/toggle-visibility")
    public ResponseEntity<Product> toggleProductVisibility(@PathVariable Long id) {
        return productRepository.findById(id).map(product -> {
            product.setActive(!product.isActive());
            return ResponseEntity.ok(productRepository.save(product));
        }).orElse(ResponseEntity.notFound().build());
    }
}