package com.capstone.service;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SpecializationResponseDto;
import org.common.event.TalentRouteEvent;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SpecializationService {
    void createSpecialization(TalentRouteEvent event);
    void updateSpecialization(TalentRouteEvent event);
    PaginatedResponseDto<SpecializationResponseDto> getAllSpecializations(Pageable pageable);
    List<SpecializationResponseDto> searchSpecializations(String searchTerm);
}
