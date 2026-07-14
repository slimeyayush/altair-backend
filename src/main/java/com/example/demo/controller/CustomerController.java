package com.example.demo.controller;

import com.example.demo.DTO.request.CartItemRequestDTO;
import com.example.demo.DTO.response.ApiMessageResponse;
import com.example.demo.DTO.response.CartItemResponseDTO;
import com.example.demo.Model.Customer;
import com.example.demo.service.CartService;
import com.example.demo.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final CartService cartService;

    public CustomerController(CustomerService customerService, CartService cartService) {
        this.customerService = customerService;
        this.cartService = cartService;
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiMessageResponse> syncCustomer() {
        boolean created = customerService.syncCustomerFromAuth();
        String message = created
                ? "New customer profile created and synced."
                : "Existing customer profile synced.";
        return ResponseEntity.ok(new ApiMessageResponse(message));
    }

    @PostMapping("/sync-cart")
    public ResponseEntity<List<CartItemResponseDTO>> syncCart(@Valid @RequestBody List<CartItemRequestDTO> localCart) {
        return ResponseEntity.ok(cartService.syncCart(localCart));
    }

    @GetMapping("/special-cust")
    public List<Customer> moreThanX(@PathVariable int orders,@PathVariable int items) {
        return customerService.findCustomersWithMoreThanN(orders,items);
    }

    @GetMapping("/morethanthree")
    public ResponseEntity<List<Customer>> moreThanThree() {
        // Admin-style internal endpoint — left as entity for now
        return ResponseEntity.ok(customerService.findCustomersWithMoreThanThreeOrders());
    }
}
