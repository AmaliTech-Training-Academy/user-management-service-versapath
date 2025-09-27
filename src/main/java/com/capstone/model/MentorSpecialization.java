package com.capstone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mentor_specializations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_specialization",
                columnNames = {"user_id", "specialization_id"}
        ),
        indexes = {
                @Index(name = "idx_user_specializations_user_id", columnList = "user_id"),
                @Index(name = "idx_user_specializations_specialization_id", columnList = "specialization_id"),
                @Index(name = "idx_user_specializations_assigned_at", columnList = "assigned_at")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorSpecialization {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        assignedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
