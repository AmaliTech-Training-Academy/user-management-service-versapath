package com.capstone.mapper;

import com.capstone.dto.response.SpecializationResponseDto;
import com.capstone.model.Specialization;
import org.common.event.TalentRouteEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpecializationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "specId", source = "id")
    @Mapping(target = "specName", source = "name")
    Specialization toEntity(TalentRouteEvent event);

    @Mapping(target = "specId", expression = "java(specialization.getSpecId().toString())")
    SpecializationResponseDto toResponseDto(Specialization specialization);
}
