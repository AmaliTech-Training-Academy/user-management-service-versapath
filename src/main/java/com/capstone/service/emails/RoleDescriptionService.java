package com.capstone.service.emails;

import com.capstone.model.ERole;
import org.springframework.stereotype.Service;

@Service
public class RoleDescriptionService {

    /**
     * Get user-friendly role name for emails
     */
    public String getRoleName(ERole role) {
        return switch (role) {
            case ADMIN -> "System Administrator";
            case MANAGER -> "Manager";
            case MENTOR -> "Mentor";
            case LEARNER -> "Learner";
        };
    }

    /**
     * Get detailed role description for invitation emails
     */
    public String getRoleDescription(ERole role) {
        return switch (role) {
            case ADMIN -> "<h1> System Administrator with full access to manage the entire platform </h1>";
            case MANAGER -> "Manager with administrative privileges to oversee teams and learning paths";
            case MENTOR -> "Mentor who guides and supports learners throughout their journey";
            case LEARNER -> "Student in the system with access to learning materials and assessments";
        };
    }

}
