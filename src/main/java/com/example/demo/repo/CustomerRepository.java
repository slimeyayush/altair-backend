package com.example.demo.repo;

import com.example.demo.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    List<Customer> findAll();

    /**
     * Returns customers with strictly more than {@code minOrders} orders.
     * Replaces CustomerService.findByOrderSize() which loaded every customer
     * into memory and filtered in Java.
     */
    @Query("SELECT c FROM Customer c WHERE SIZE(c.orders) > :minOrders")
    List<Customer> findCustomersWithMoreThanNOrders(@Param("minOrders") int minOrders);
}
