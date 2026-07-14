package com.example.demo.repo;



import com.example.demo.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerEmail(String email);
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByCustomerEmailOrderByIdDesc(String customerEmail);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    List<Order> findByOrderDate(LocalDateTime date);
}
//Find all the customers those have order on 01-06-2026