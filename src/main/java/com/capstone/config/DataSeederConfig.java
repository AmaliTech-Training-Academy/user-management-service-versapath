package com.capstone.config;

import com.capstone.model.ERole;
import com.capstone.model.EStatus;
import com.capstone.model.Role;
import com.capstone.model.User;
import com.capstone.repository.RoleRepository;
import com.capstone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeederConfig {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@versapath.com}")
    private String adminEmail;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:Admin@123}")
    private String adminPassword;

    @Value("${app.admin.firstName:System}")
    private String adminFirstName;

    @Value("${app.admin.lastName:Administrator}")
    private String adminLastName;

    @Bean
    public CommandLineRunner seedDatabase() {
        return args -> {
            log.info("Starting database seeding...");

            try {
                seedRoles();
                seedAdminUser();
                log.info("Database seeding completed successfully!");
            } catch (Exception e) {
                log.error("Error during database seeding: {}", e.getMessage(), e);
            }
        };
    }

    private void seedRoles() {
        log.info("Starting role data seeding...");
        seedRole(ERole.ADMIN, "System Administrator with full access");
        seedRole(ERole.MANAGER, "Manager with administrative privileges");
        seedRole(ERole.MENTOR, "Mentor who guides and supports learners");
        seedRole(ERole.LEARNER, "Student in the system");
        log.info("Role data seeding completed.");
    }

    private void seedRole(ERole role, String description) {
        if(!roleRepository.existsByRole(role)){
            Role roles = Role.builder()
                    .role(role)
                    .description(description)
                    .build();
            roleRepository.save(roles);
            log.info("Seeded role: {}", role);
        }else{
            log.info("Role {} already exists.", role);
        }

    }

    private void seedAdminUser() {
        log.info("Seeding admin user...");

        // Check if admin user already exists
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists with email: {}", adminEmail);
            return;
        }

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user already exists with username: {}", adminUsername);
            return;
        }

        // Get ADMIN role
        Role adminRole = roleRepository.findByRole(ERole.ADMIN)
                .orElseThrow(() -> new RuntimeException("ADMIN role not found. Please run role seeding first."));

        // Create admin user
        User adminUser = User.builder()
                .firstName(adminFirstName)
                .lastName(adminLastName)
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .role(adminRole)
                .status(EStatus.ACTIVE)
                .build();

        userRepository.save(adminUser);
    }

}
