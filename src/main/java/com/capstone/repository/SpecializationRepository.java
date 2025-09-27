package com.capstone.repository;

import com.capstone.model.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    Optional<Specialization> findBySpecId(UUID specId);

    boolean existsBySpecId(UUID specId);

    boolean existsBySpecName(String specName);

    Optional<Specialization> findBySpecName(String specName);
}
