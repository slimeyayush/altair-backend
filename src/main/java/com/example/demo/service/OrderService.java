package com.example.demo.service;

import com.example.demo.DTO.OrderItemRequestDTO;
import com.example.demo.DTO.OrderRequestDTO;
import com.example.demo.Model.Order;
import com.example.demo.Model.OrderItem;
import com.example.demo.Model.Product;
import com.example.demo.Model.ProductVariant;
import com.example.demo.repo.CustomerRepository;
import com.example.demo.repo.OrderRepository;
import com.example.demo.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public Order createPendingOrder(OrderRequestDTO requestDTO) {
        Order order = new Order();

        // 1. Set the email and address for guest checkouts / WhatsApp reference
        order.setCustomerEmail(requestDTO.getCustomerEmail());
        order.setShippingAddress(requestDTO.getShippingAddress());

        // 2. Link the database Customer if the user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String identifier = auth.getName();
            customerRepository.findByEmail(identifier)
                    .or(() -> customerRepository.findByPhoneNumber(identifier))
                    .ifPresent(order::setCustomer);
        }

        BigDecimal total = BigDecimal.ZERO;

        // 3. Process items securely
        for (OrderItemRequestDTO itemDTO : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product ID " + itemDTO.getProductId() + " not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());

            BigDecimal itemPrice = product.getPrice();

            // Handle proper database-driven Variant Logic
            if (itemDTO.getVariantId() != null) {
                ProductVariant selectedVariant = product.getVariants().stream()
                        .filter(v -> v.getId().equals(itemDTO.getVariantId()))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Variant ID " + itemDTO.getVariantId() + " not found"));

                if (selectedVariant.getStockQuantity() < itemDTO.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for variant: " + selectedVariant.getVariantType() + " " + selectedVariant.getVariantSize());
                }

                // Use variant price if an override exists
                if (selectedVariant.getPriceOverride() != null) {
                    itemPrice = selectedVariant.getPriceOverride();
                }

                orderItem.setProductVariant(selectedVariant);
            } else {
                // Fallback to base product logic if no variant is selected
                if (product.getStockQuantity() < itemDTO.getQuantity()) {
                    throw new RuntimeException("Insufficient stock for product: " + product.getName());
                }
            }

            orderItem.setPriceAtPurchase(itemPrice);
            order.getItems().add(orderItem);

            BigDecimal lineTotal = itemPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            total = total.add(lineTotal);
        }

        // 4. Finalize and save
        order.setTotalAmount(total);
        order.setStatus(Order.OrderStatus.PENDING);

        return orderRepository.save(order);
    }

    @Transactional
    public Order confirmOrderPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Order is not in PENDING state");
        }

        // Deduct inventory only upon payment confirmation
        for (OrderItem item : order.getItems()) {
            if (item.getProductVariant() != null) {
                // Deduct from specific variant stock
                ProductVariant variant = item.getProductVariant();
                if (variant.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Stock depleted before payment for variant: " + variant.getVariantType() + " " + variant.getVariantSize());
                }
                variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());

                // Saving the parent product cascades the update to the variant
                productRepository.save(item.getProduct());
            } else {
                // Deduct from base product stock
                Product product = item.getProduct();
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Stock depleted before payment for: " + product.getName());
                }
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(Order.OrderStatus.PAID);
        return orderRepository.save(order);
    }
}