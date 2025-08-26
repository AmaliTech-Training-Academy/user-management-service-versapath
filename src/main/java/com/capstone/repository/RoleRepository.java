package com.capstone.repository;

import com.capstone.model.ERole;
import com.capstone.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByRole(ERole role);
    boolean existsByRole(ERole role);
}
