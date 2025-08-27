package com.capstone.repository;

import com.capstone.model.User;
import com.capstone.model.EStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByEmailAndStatus(String email, EStatus status);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.id = :userId")
    Optional<User> findByIdWithRole(@Param("userId") UUID userId);
}
