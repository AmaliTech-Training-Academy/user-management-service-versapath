package com.capstone.repository;

import com.capstone.model.Specialization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {

    Optional<Specialization> findBySpecId(UUID specId);

    @Query("SELECT s FROM Specialization s WHERE s.specId IN :specIds")
    List<Specialization> findAllBySpecIds(@Param("specIds") List<UUID> specIds);

    boolean existsBySpecId(UUID specId);

    boolean existsBySpecName(String specName);

    Optional<Specialization> findBySpecName(String specName);

    @Query("SELECT s FROM Specialization s WHERE LOWER(s.specName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY s.specName")
    List<Specialization> findBySpecNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    @Query("SELECT s FROM Specialization s WHERE LOWER(s.specName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Specialization> findBySpecNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

}
