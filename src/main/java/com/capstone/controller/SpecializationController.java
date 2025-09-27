package com.capstone.controller;

import com.capstone.dto.response.ApiResponseDto;
import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SpecializationResponseDto;
import com.capstone.service.SpecializationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/specializations")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Specialization Management", description = "APIs for managing specializations")
@SecurityRequirement(name = "Bearer Authentication")
public class SpecializationController {

    private final SpecializationService specializationService;

    @GetMapping
    @Operation(
            summary = "Get all specializations",
            description = "Retrieve paginated list of all specializations"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specializations retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<SpecializationResponseDto>>> getAllSpecializations(
            @PageableDefault() Pageable pageable) {
        log.info("Retrieving all specializations, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        PaginatedResponseDto<SpecializationResponseDto> result =
                specializationService.getAllSpecializations(pageable);

        return ResponseEntity.ok(ApiResponseDto.success(result, "Specializations retrieved successfully"));
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search specialization by name",
            description = "Search for a specific specialization by name"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Specialization found successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Specialization not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponseDto<SpecializationResponseDto>> getSpecializationByName(
            @Parameter(description = "Specialization name to search for", required = true)
            @RequestParam String name) {
        log.info("Searching for specialization by name: '{}'", name);

        SpecializationResponseDto result = specializationService.getSpecializationByName(name);

        return ResponseEntity.ok(ApiResponseDto.success(result, "Specialization found successfully"));
    }
}
