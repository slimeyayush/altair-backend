package com.example.demo.controller;

import com.example.demo.DTO.request.OrderRequestDTO;
import com.example.demo.DTO.response.OrderResponseDTO;
import com.example.demo.Model.Order;
import com.example.demo.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@Valid @RequestBody OrderRequestDTO request) {
        // Returns entity — frontend uses .id to hand off to /api/payment/create-transaction
        // Switch to DTO only if you also update the payment-flow frontend.
        return ResponseEntity.ok(orderService.createPendingOrder(request));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponseDTO>> getMyOrders() {
        return ResponseEntity.ok(orderService.getMyOrders());
    }
}
