package com.capstone.repository;

import com.capstone.model.MentorSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MentorSpecializationRepository extends JpaRepository<MentorSpecialization, Long> {

    List<MentorSpecialization> findByUserId(UUID userId);

    boolean existsByUserIdAndSpecializationSpecId(UUID userId, UUID specializationId);

}
