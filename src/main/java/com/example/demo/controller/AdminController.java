package com.example.demo.controller;

import com.example.demo.DTO.AdminDTO;
import com.example.demo.Model.AdminUser;
import com.example.demo.Model.Customer;
import com.example.demo.Model.Order;
import com.example.demo.Model.Product;
import com.example.demo.repo.AdminUserRepository;
import com.example.demo.repo.CustomerRepository;
import com.example.demo.repo.OrderRepository;
import com.example.demo.repo.ProductRepository;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

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

    // NEW: Cancel Order Endpoint
    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            Order order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            String currentStatus = order.getStatus().toString();

            if ("CANCELLED".equals(currentStatus)) {
                return ResponseEntity.badRequest().body("Order is already cancelled.");
            }

            // Restock items ONLY if the order had already deducted them (Paid, Shipped, Delivered)
            if ("PAID".equals(currentStatus) || "SHIPPED".equals(currentStatus) || "DELIVERED".equals(currentStatus)) {
                order.getItems().forEach(item -> {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                });
            }

            // Update status safely using the Enum
            order.setStatus(Order.OrderStatus.valueOf("CANCELLED"));
            orderRepository.save(order);

            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to cancel order: " + e.getMessage());
        }
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
        product.setId(null);

        // Link the parent product to each child variant
        if (product.getVariants() != null) {
            product.getVariants().forEach(variant -> variant.setProduct(product));
        }
        if (product.getAdditionalImages() != null) {
            product.getAdditionalImages().forEach(img -> img.setProduct(product));
        }

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

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        return productRepository.findById(id).map(existingProduct -> {
            // 1. Update standard fields
            existingProduct.setName(productDetails.getName());
            existingProduct.setBrand(productDetails.getBrand());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setOldPrice(productDetails.getOldPrice());
            existingProduct.setStockQuantity(productDetails.getStockQuantity()); // Keep as a total/fallback stock
            existingProduct.setCategory(productDetails.getCategory());
            existingProduct.setTag(productDetails.getTag());
            existingProduct.setImageUrl(productDetails.getImageUrl());

            // 2. Update Variants safely
            existingProduct.getVariants().clear();
            if (productDetails.getVariants() != null) {
                productDetails.getVariants().forEach(variant -> {
                    variant.setProduct(existingProduct); // Link to parent
                    existingProduct.getVariants().add(variant);
                });
            }
            existingProduct.getAdditionalImages().clear();
            if (productDetails.getAdditionalImages() != null) {
                productDetails.getAdditionalImages().forEach(img -> {
                    img.setProduct(existingProduct);
                    existingProduct.getAdditionalImages().add(img);
                });
            }

            Product savedProduct = productRepository.save(existingProduct);
            return ResponseEntity.ok(savedProduct);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 1. Update the GET mapping to include the ID
    @GetMapping("/admins")
    public ResponseEntity<List<AdminDTO>> getAllAdmins() {
        List<AdminDTO> adminList = adminUserRepository.findAll().stream()
                .map(admin -> new AdminDTO(admin.getId(), admin.getUsername()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(adminList);
    }

    // 2. Add the DELETE endpoint
    @DeleteMapping("/admins/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable Long id) {
        if (!adminUserRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        adminUserRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return ResponseEntity.ok(customers);
    }
}