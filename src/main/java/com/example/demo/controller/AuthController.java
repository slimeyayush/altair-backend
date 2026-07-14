package com.example.demo.controller;

import com.example.demo.DTO.request.AuthRequestDTO;
import com.example.demo.DTO.response.LoginResponse;
import com.example.demo.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminService adminService;

    public AuthController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO credentials) {
        return adminService.authenticate(credentials.getUsername(), credentials.getPassword())
                .<ResponseEntity<?>>map(token -> ResponseEntity.ok(new LoginResponse(token)))
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid credentials"));
    }
}
