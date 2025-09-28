package com.capstone.repository;

import com.capstone.model.MentorSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface MentorSpecializationRepository extends JpaRepository<MentorSpecialization, Long> {

    List<MentorSpecialization> findByUserId(UUID userId);

    @Query("SELECT ms.specialization.specId FROM MentorSpecialization ms WHERE ms.user.id = :userId")
    Set<UUID> findSpecializationSpecIdsByUserId(@Param("userId") UUID userId);

    // Batch save method (JPA will handle this efficiently)
    default void saveAllMentorSpecializations(List<MentorSpecialization> mentorSpecializations) {
        saveAll(mentorSpecializations);
    }

}
