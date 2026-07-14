package com.example.demo.DTO.response;

import com.example.demo.Model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order representation for the customer's "my orders" view.
 * No customer back-reference, no full product graph — just what's needed
 * to render an order history page.
 */
public record OrderResponseDTO(
        Long id,
        LocalDateTime orderDate,
        Order.OrderStatus status,
        BigDecimal totalAmount,
        String shippingAddress,
        String customerEmail,
        String razorpayOrderId,
        String razorpayPaymentId,
        List<OrderItemResponseDTO> items
) {
    public record OrderItemResponseDTO(
            Long id,
            Long productId,
            String productName,
            String productImageUrl,
            Integer quantity,
            BigDecimal priceAtPurchase,
            Long variantId,
            String variantLabel
    ) {}
}
