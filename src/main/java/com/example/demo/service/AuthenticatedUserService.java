package com.example.demo.service;

import com.example.demo.Model.Customer;
import com.example.demo.exception.BusinessRuleException;
import com.example.demo.repo.CustomerRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Single source of truth for resolving the currently authenticated customer.
 *
 * Previously this logic was duplicated across CartController, CustomerController,
 * OrderService, and CustomerService — each with slightly different null-handling.
 * Centralizing it means: one bug to fix, one place to add logging, one mocked
 * dependency in tests.
 */
@Service
public class AuthenticatedUserService {

    private final CustomerRepository customerRepository;

    public AuthenticatedUserService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /**
     * Returns the identifier (email or phone) of the current authenticated user.
     * Throws if no authenticated user is present.
     */
    public String getCurrentIdentifier() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())
                || auth.getName() == null
                || auth.getName().isBlank()) {
            throw new BusinessRuleException("No authenticated user found");
        }
        return auth.getName();
    }

    /**
     * Looks up the current customer by email or phone. Does NOT create a row.
     */
    @Transactional(readOnly = true)
    public Optional<Customer> findCurrentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        String identifier = auth.getName();
        return customerRepository.findByEmail(identifier)
                .or(() -> customerRepository.findByPhoneNumber(identifier));
    }

    /**
     * Returns the current customer, creating a new row if one doesn't exist.
     * Determines email vs phone by presence of '@' in the identifier
     * (matching the existing Firebase filter behavior).
     */
    @Transactional
    public Customer getOrCreateCurrentCustomer() {
        String identifier = getCurrentIdentifier();
        return customerRepository.findByEmail(identifier)
                .or(() -> customerRepository.findByPhoneNumber(identifier))
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    if (identifier.contains("@")) {
                        newCustomer.setEmail(identifier);
                    } else {
                        newCustomer.setPhoneNumber(identifier);
                    }
                    return customerRepository.save(newCustomer);
                });
    }
}
