package com.example.demo;

import com.example.demo.Model.AdminUser;
import com.example.demo.repo.AdminUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class EcommerceBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceBackendApplication.class, args);
	}
    @Bean
    public CommandLineRunner initMasterAdmin(AdminUserRepository repository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Only create if no admins exist
            if (repository.count() == 0) {
                AdminUser admin = new AdminUser();
                admin.setUsername("master_admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // Spring encrypts this safely
                admin.setRole("ADMIN");
                repository.save(admin);
                System.out.println("=== MASTER ADMIN CREATED SUCCESSFULLY ===");
            }
        };
    }
}
