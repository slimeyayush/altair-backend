package com.example.demo.controller;

import com.example.demo.DTO.request.CartDeltaDTO;
import com.example.demo.DTO.request.CartItemRequestDTO;
import com.example.demo.DTO.response.CartItemResponseDTO;
import com.example.demo.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemResponseDTO>> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/add")
    public ResponseEntity<List<CartItemResponseDTO>> addToCart(@Valid @RequestBody CartItemRequestDTO payload) {
        return ResponseEntity.ok(cartService.addToCart(payload));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<List<CartItemResponseDTO>> updateQuantity(@PathVariable Long productId,
                                                                    @Valid @RequestBody CartDeltaDTO payload) {
        return ResponseEntity.ok(cartService.updateQuantity(productId, payload));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<List<CartItemResponseDTO>> removeFromCart(@PathVariable Long productId,
                                                                    @RequestParam(required = false) Long variantId) {
        return ResponseEntity.ok(cartService.removeFromCart(productId, variantId));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok().build();
    }
}
