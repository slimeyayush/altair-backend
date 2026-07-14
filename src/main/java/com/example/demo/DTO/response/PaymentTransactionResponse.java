package com.example.demo.DTO.response;

/** Sent to the frontend after a Razorpay order is registered. */
public record PaymentTransactionResponse(String razorpayOrderId, int amount, String currency) {}
