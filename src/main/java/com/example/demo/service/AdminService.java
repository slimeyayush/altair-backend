package com.example.demo.service;

import com.example.demo.DTO.request.AuthRequestDTO;
import com.example.demo.DTO.response.AdminDTO;
import com.example.demo.Model.AdminUser;
import com.example.demo.exception.BusinessRuleException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repo.AdminUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminService(AdminUserRepository adminUserRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AdminDTO register(AuthRequestDTO payload) {
        if (adminUserRepository.findByUsername(payload.getUsername()).isPresent()) {
            throw new BusinessRuleException("Username already exists");
        }
        AdminUser newUser = new AdminUser();
        newUser.setUsername(payload.getUsername());
        newUser.setPassword(passwordEncoder.encode(payload.getPassword()));
        AdminUser saved = adminUserRepository.save(newUser);
        return new AdminDTO(saved.getId(), saved.getUsername());
    }

    /**
     * Returns a JWT if credentials match, empty otherwise.
     * Controller decides the HTTP shape; service just answers "did this work".
     */
    public Optional<String> authenticate(String username, String password) {
        return adminUserRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> jwtService.generateToken(user.getUsername(), user.getRole()));
    }

    @Transactional(readOnly = true)
    public List<AdminDTO> findAll() {
        return adminUserRepository.findAll().stream()
                .map(a -> new AdminDTO(a.getId(), a.getUsername()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        if (!adminUserRepository.existsById(id)) {
            throw new ResourceNotFoundException("Admin", id);
        }
        adminUserRepository.deleteById(id);
    }
}
