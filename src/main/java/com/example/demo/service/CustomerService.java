package com.example.demo.service;



import com.example.demo.Model.Customer;
import com.example.demo.repo.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public void syncCustomerFromToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Abort if no valid authentication is found
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return;
        }

        String identifier = auth.getName(); // The email or phone injected by your Firebase filter

        Optional<Customer> existingCustomer = customerRepository.findByEmail(identifier)
                .or(() -> customerRepository.findByPhoneNumber(identifier));

        // Create new customer if they do not exist
        if (existingCustomer.isEmpty()) {
            Customer newCustomer = new Customer();

            // Simple check to map identifier to the correct field
            if (identifier.contains("@")) {
                newCustomer.setEmail(identifier);
            } else {
                newCustomer.setPhoneNumber(identifier);
            }

            customerRepository.save(newCustomer);
        }
    }
}
