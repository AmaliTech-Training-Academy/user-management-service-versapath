package com.capstone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Logout confirmation response")
public class LogoutResponseDto {

    @Schema(description = "Confirmation message")
    private String message;

    @Schema(description = "Timestamp of logout")
    private Instant logoutTime;
}
