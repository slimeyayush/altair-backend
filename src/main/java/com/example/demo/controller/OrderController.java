package com.example.demo.controller;

import com.example.demo.DTO.OrderRequestDTO;
import com.example.demo.Model.Order;
import com.example.demo.service.OrderService;
import com.example.demo.repo.OrderRepository; // <-- 1. Add this import
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository; // <-- 2. Inject the repository here

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@Valid @RequestBody OrderRequestDTO request) {
        try {
            Order savedOrder = orderService.createPendingOrder(request);
            return ResponseEntity.ok(savedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders() {
        // Get the email of the currently logged-in Firebase user from the Security Context
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Fetch orders matching that email
        List<Order> myOrders = orderRepository.findByCustomerEmailOrderByIdDesc(userEmail);
        return ResponseEntity.ok(myOrders);
    }
}