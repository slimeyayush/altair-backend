package com.example.demo.service;

import com.example.demo.Model.Customer;
import com.example.demo.Model.Order;
import com.example.demo.repo.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public CustomerService(CustomerRepository customerRepository,
                           AuthenticatedUserService authenticatedUserService) {
        this.customerRepository = customerRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    /**
     * Idempotent: creates the customer row if missing.
     * Returns whether a new record was created (useful for the response message).
     */
    @Transactional
    public boolean syncCustomerFromAuth() {
        boolean existed = authenticatedUserService.findCurrentCustomer().isPresent();
        authenticatedUserService.getOrCreateCurrentCustomer();
        return !existed;
    }
    public List<Customer> findCustomersWithMoreThanN(int orders,int items) {
        List<Customer> getAll = customerRepository.findCustomersWithMoreThanNOrders(orders);
        List<Customer> moreThanX  = new ArrayList<>();
        for(Customer cust : getAll) {
            boolean flag = true;
            for(Order o:cust.getOrders()) {
                if(o.getItems().size() < items) {
                    flag = false;
                    break;
                }
            }
            if(flag) {
                moreThanX.add(cust);
            }
        }
        return moreThanX;
    }

    /**
     * Customers with more than 3 orders.
     *
     * Previously this loaded ALL customers into memory and filtered in Java.
     * Now uses a JPQL count query on the repository — orders of magnitude
     * cheaper as your customer base grows.
     */
    @Transactional(readOnly = true)
    public List<Customer> findCustomersWithMoreThanThreeOrders() {
        return customerRepository.findCustomersWithMoreThanNOrders(3);
    }


}
