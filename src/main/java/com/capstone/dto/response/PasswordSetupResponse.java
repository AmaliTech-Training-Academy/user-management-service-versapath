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
public class PasswordSetupResponse {
    private String message;
    private boolean success;
    private UUID userId;
    private String username;
}
