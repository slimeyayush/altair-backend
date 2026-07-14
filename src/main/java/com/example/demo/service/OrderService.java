package com.example.demo.service;

import com.example.demo.DTO.request.OrderItemRequestDTO;
import com.example.demo.DTO.request.OrderRequestDTO;
import com.example.demo.DTO.response.OrderResponseDTO;
import com.example.demo.Model.Order;
import com.example.demo.Model.OrderItem;
import com.example.demo.Model.Product;
import com.example.demo.Model.ProductVariant;
import com.example.demo.exception.BusinessRuleException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.repo.OrderRepository;
import com.example.demo.repo.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        AuthenticatedUserService authenticatedUserService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    @Transactional
    public Order createPendingOrder(OrderRequestDTO requestDTO) {
        Order order = new Order();
        order.setCustomerEmail(requestDTO.getCustomerEmail());
        order.setShippingAddress(requestDTO.getShippingAddress());

        authenticatedUserService.findCurrentCustomer().ifPresent(order::setCustomer);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequestDTO itemDTO : requestDTO.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", itemDTO.getProductId()));

            if (product.getStockQuantity() < itemDTO.getQuantity()) {
                throw new BusinessRuleException("Insufficient stock for base product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemDTO.getQuantity());

            BigDecimal itemPrice = product.getPrice();

            if (itemDTO.getVariantId() != null) {
                ProductVariant selectedVariant = product.getVariants().stream()
                        .filter(v -> v.getId().equals(itemDTO.getVariantId()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Variant", itemDTO.getVariantId()));

                Product linkedProduct = selectedVariant.getLinkedProduct();

                if (linkedProduct.getStockQuantity() < itemDTO.getQuantity()) {
                    throw new BusinessRuleException(
                            "Insufficient stock for bundled variant: " + selectedVariant.getVariantLabel());
                }

                BigDecimal variantPrice = selectedVariant.getPriceOverride() != null
                        ? selectedVariant.getPriceOverride()
                        : linkedProduct.getPrice();

                itemPrice = itemPrice.add(variantPrice);
                orderItem.setProductVariant(selectedVariant);
            }

            orderItem.setPriceAtPurchase(itemPrice);
            order.getItems().add(orderItem);

            BigDecimal lineTotal = itemPrice.multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            total = total.add(lineTotal);
        }

        order.setTotalAmount(total);
        order.setStatus(Order.OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    @Transactional
    public Order confirmOrderPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new BusinessRuleException("Order is not in PENDING state");
        }

        for (OrderItem item : order.getItems()) {
            Product baseProduct = item.getProduct();
            if (baseProduct.getStockQuantity() < item.getQuantity()) {
                throw new BusinessRuleException(
                        "Stock depleted before payment for: " + baseProduct.getName());
            }
            baseProduct.setStockQuantity(baseProduct.getStockQuantity() - item.getQuantity());
            productRepository.save(baseProduct);

            if (item.getProductVariant() != null) {
                Product linkedProduct = item.getProductVariant().getLinkedProduct();
                if (linkedProduct.getStockQuantity() < item.getQuantity()) {
                    throw new BusinessRuleException(
                            "Stock depleted before payment for variant: "
                                    + item.getProductVariant().getVariantLabel());
                }
                linkedProduct.setStockQuantity(linkedProduct.getStockQuantity() - item.getQuantity());
                productRepository.save(linkedProduct);
            }
        }

        order.setStatus(Order.OrderStatus.PAID);
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Order is already cancelled.");
        }

        boolean stockWasDeducted = order.getStatus() == Order.OrderStatus.PAID
                || order.getStatus() == Order.OrderStatus.SHIPPED
                || order.getStatus() == Order.OrderStatus.DELIVERED;

        if (stockWasDeducted) {
            for (OrderItem item : order.getItems()) {
                if (item.getProductVariant() != null) {
                    Product linkedProduct = item.getProductVariant().getLinkedProduct();
                    linkedProduct.setStockQuantity(linkedProduct.getStockQuantity() + item.getQuantity());
                    productRepository.save(linkedProduct);
                }
                Product baseProduct = item.getProduct();
                baseProduct.setStockQuantity(baseProduct.getStockQuantity() + item.getQuantity());
                productRepository.save(baseProduct);
            }
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        try {
            order.setStatus(Order.OrderStatus.valueOf(newStatus.toUpperCase()));
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("Invalid order status: " + newStatus);
        }
        return orderRepository.save(order);
    }

    /**
     * Returns the current customer's orders as DTOs. Resolves the user via
     * AuthenticatedUserService so the controller doesn't touch SecurityContext.
     */
    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getMyOrders() {
        String userEmail = authenticatedUserService.getCurrentIdentifier();
        List<Order> orders = orderRepository.findByCustomerEmailOrderByIdDesc(userEmail);
        return OrderMapper.toDtoList(orders);
    }
}
