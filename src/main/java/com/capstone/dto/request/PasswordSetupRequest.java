package com.capstone.dto.request;

import com.capstone.validation.PasswordMatching;
import com.capstone.validation.StrongPassword;
import com.capstone.validation.ValidName;
import com.capstone.validation.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request payload for completing user registration")
@PasswordMatching(message = "Password and confirmation password do not match")
public class PasswordSetupRequest {

    @Schema(
            description = "Unique username for the user account",
            example = "johndoe123"
    )
    @NotBlank(message = "Username is required")
    @Size(
            min = 3,
            max = 30,
            message = "Username must be between 3 and 30 characters"
    )
    @ValidUsername(message = "Username must contain only letters, numbers, underscores, and hyphens. Must start with a letter or number")
    private String username;

    @Schema(
            description = "User's first name",
            example = "John"
    )
    @NotBlank(message = "First name is required")
    @Size(
            min = 2,
            max = 50,
            message = "First name must be between 2 and 50 characters"
    )
    @ValidName(message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Schema(
            description = "User's last name",
            example = "Doe"
    )
    @NotBlank(message = "Last name is required")
    @Size(
            min = 2,
            max = 50,
            message = "Last name must be between 2 and 50 characters"
    )
    @ValidName(message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Schema(
            description = "User's password",
            example = "SecurePass123!"
    )
    @NotBlank(message = "Password is required")
    @Size(
            min = 8,
            max = 128,
            message = "Password must be between 8 and 128 characters"
    )
    @StrongPassword(message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character, and be at least 8 characters long")
    private String password;

    @Schema(
            description = "Password confirmation",
            example = "SecurePass123!"
    )
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
}