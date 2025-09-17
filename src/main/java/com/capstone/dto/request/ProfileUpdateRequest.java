package com.capstone.dto.request;

import com.capstone.validation.ValidName;
import com.capstone.validation.ValidUsername;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to update user profile information")
public class ProfileUpdateRequest {

    @Schema(description = "User's first name", example = "John")
    @NotBlank(message = "First name is required")
    @Size(
            min = 2,
            max = 50,
            message = "First name must be between 2 and 50 characters"
    )
    @ValidName(message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    @NotBlank(message = "Last name is required")
    @Size(
            min = 2,
            max = 50,
            message = "Last name must be between 2 and 50 characters"
    )
    @ValidName(message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;

    @Schema(description = "User's username", example = "johndoe")
    @NotBlank(message = "Username is required")
    @Size(
            min = 3,
            max = 30,
            message = "Username must be between 3 and 30 characters"
    )
    @ValidUsername(message = "Username must contain only letters, numbers, underscores, and hyphens. Must start with a letter or number")
    private String username;

    @Schema(
            description = "User's phone number with country code (E.164 format)",
            example = "+1234567890"
    )
    @Pattern(
            regexp = "^\\+[1-9]\\d{6,14}$",
            message = "Phone number must be in international format (E.164) with 7-15 digits including country code"
    )
    private String phoneNumber;
}