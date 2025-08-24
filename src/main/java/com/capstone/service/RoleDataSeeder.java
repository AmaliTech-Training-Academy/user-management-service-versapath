package com.capstone.service;

import com.capstone.model.ERole;
import com.capstone.model.Role;
import com.capstone.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class RoleDataSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
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
}
