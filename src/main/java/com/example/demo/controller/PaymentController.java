package com.example.demo.controller;

import com.example.demo.DTO.response.PaymentTransactionResponse;
import com.example.demo.Model.Order;
import com.example.demo.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create-transaction/{orderId}")
    public ResponseEntity<PaymentTransactionResponse> createTransaction(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.createTransaction(orderId));
    }

    @PostMapping("/verify")
    public ResponseEntity<Order> verifyPayment(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(paymentService.verifyPayment(payload));
    }
}
