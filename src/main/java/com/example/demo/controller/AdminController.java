package com.example.demo.controller;

import com.example.demo.DTO.request.AuthRequestDTO;
import com.example.demo.DTO.request.OrderStatusDTO;
import com.example.demo.DTO.request.StockUpdateDTO;
import com.example.demo.DTO.response.AdminDTO;
import com.example.demo.Model.Customer;
import com.example.demo.Model.Order;
import com.example.demo.Model.Product;
import com.example.demo.repo.CustomerRepository;
import com.example.demo.repo.OrderRepository;
import com.example.demo.service.AdminService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderService orderService;
    private final ProductService productService;
    private final AdminService adminService;

    public AdminController(OrderRepository orderRepository,
                           CustomerRepository customerRepository,
                           OrderService orderService,
                           ProductService productService,
                           AdminService adminService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.orderService = orderService;
        this.productService = productService;
        this.adminService = adminService;
    }

    // --- Admin accounts ---

    @PostMapping("/register-admin")
    public ResponseEntity<AdminDTO> registerNewAdmin(@Valid @RequestBody AuthRequestDTO payload) {
        return ResponseEntity.ok(adminService.register(payload));
    }

    @GetMapping("/admins")
    public ResponseEntity<List<AdminDTO>> getAllAdmins() {
        return ResponseEntity.ok(adminService.findAll());
    }

    @DeleteMapping("/admins/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.ok().build();
    }

    // --- Orders ---

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping("/orders/{id}/mark-paid")
    public ResponseEntity<Order> markOrderAsPaid(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmOrderPayment(id));
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id,
                                                   @Valid @RequestBody OrderStatusDTO payload) {
        return ResponseEntity.ok(orderService.updateStatus(id, payload.getStatus()));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }

    // --- Inventory ---

    @GetMapping("/inventory")
    public List<Product> getInventory() {
        return productService.findAll();
    }

    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.create(product));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product details) {
        return ResponseEntity.ok(productService.update(id, details));
    }

    @PutMapping("/inventory/{id}")
    public ResponseEntity<Product> updateStock(@PathVariable Long id,
                                               @Valid @RequestBody StockUpdateDTO payload) {
        return ResponseEntity.ok(productService.updateStock(id, payload));
    }

    @PutMapping("/inventory/{id}/toggle-visibility")
    public ResponseEntity<Product> toggleProductVisibility(@PathVariable Long id) {
        return ResponseEntity.ok(productService.toggleVisibility(id));
    }

    @DeleteMapping("/inventory/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.ok().build();
    }

    // --- Customers ---

    @GetMapping("/customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerRepository.findAll());
    }
}
