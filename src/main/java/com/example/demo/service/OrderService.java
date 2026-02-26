package com.example.demo.service;




import com.example.demo.DTO.OrderItemRequestDTO;
import com.example.demo.DTO.OrderRequestDTO;
import com.example.demo.Model.Order;
import com.example.demo.Model.OrderItem;
import com.example.demo.Model.Product;
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
        order.setShippingAddress(requestDTO.getShippingAddress()); // NEW: Map the address

        // 2. Link the database Customer if the user is authenticated via Firebase
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

            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setPriceAtPurchase(product.getPrice());

            order.getItems().add(orderItem);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            total = total.add(lineTotal);
        }

        // 4. Add flat 500 shipping fee if cart isn't empty
//        if (total.compareTo(BigDecimal.ZERO) > 0) {
//            total = total.add(BigDecimal.valueOf(500));
//        }

        // 5. Finalize and save
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
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new RuntimeException("Stock depleted before payment for: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.PAID);
        return orderRepository.save(order);
    }
}
