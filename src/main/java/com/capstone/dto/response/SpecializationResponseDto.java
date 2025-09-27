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
@Schema(description = "Specialization information response")
public class SpecializationResponseDto {

    @Schema(description = "Database record ID")
    private Long id;

    @Schema(description = "Specialization's unique identifier")
    private String specId;

    @Schema(description = "Specialization name")
    private String specName;
}
