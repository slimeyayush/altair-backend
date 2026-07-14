package com.example.demo.mapper;

import com.example.demo.DTO.response.OrderResponseDTO;
import com.example.demo.Model.Order;
import com.example.demo.Model.OrderItem;
import com.example.demo.Model.Product;
import com.example.demo.Model.ProductVariant;

import java.util.Collections;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {}

    public static OrderResponseDTO toDto(Order order) {
        List<OrderResponseDTO.OrderItemResponseDTO> items = order.getItems() == null
                ? Collections.emptyList()
                : order.getItems().stream()
                        .map(OrderMapper::toItemDto)
                        .toList();

        return new OrderResponseDTO(
                order.getId(),
                order.getOrderDate(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingAddress(),
                order.getCustomerEmail(),
                order.getRazorpayOrderId(),
                order.getRazorpayPaymentId(),
                items
        );
    }

    public static List<OrderResponseDTO> toDtoList(List<Order> orders) {
        return orders.stream().map(OrderMapper::toDto).toList();
    }

    private static OrderResponseDTO.OrderItemResponseDTO toItemDto(OrderItem item) {
        Product product = item.getProduct();
        ProductVariant variant = item.getProductVariant();

        return new OrderResponseDTO.OrderItemResponseDTO(
                item.getId(),
                product != null ? product.getId() : null,
                product != null ? product.getName() : null,
                product != null ? product.getImageUrl() : null,
                item.getQuantity(),
                item.getPriceAtPurchase(),
                variant != null ? variant.getId() : null,
                variant != null ? variant.getVariantLabel() : null
        );
    }
}
