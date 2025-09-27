package com.capstone.service;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SpecializationResponseDto;
import org.common.event.TalentRouteEvent;
import org.springframework.data.domain.Pageable;

public interface SpecializationService {
    void createSpecialization(TalentRouteEvent event);
    void updateSpecialization(TalentRouteEvent event);
    SpecializationResponseDto getSpecializationByName(String specName);
    PaginatedResponseDto<SpecializationResponseDto> getAllSpecializations(Pageable pageable);
}
