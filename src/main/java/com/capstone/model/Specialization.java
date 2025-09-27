package com.capstone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "specializations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Specialization {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "spec_id", nullable = false, unique = true, updatable = false)
    private UUID specId;
    @Column(name = "spec_name", nullable = false, unique = true)
    private String specName;
}
