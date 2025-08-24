package com.capstone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Validation error details")
public class ValidationError {

    @Schema(description = "Field name that failed validation", example = "email")
    private String field;

    @Schema(description = "Rejected value", example = "invalid-email")
    private String rejectedValue;

    @Schema(description = "Validation error message", example = "Invalid email format")
    private String message;
}
