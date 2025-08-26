package com.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationResponse {
    private UUID userId;
    private String email;
    private String registrationLink;
    private String message;
    private String tokenExpiresAt;
}
