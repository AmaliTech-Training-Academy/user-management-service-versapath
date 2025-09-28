package com.capstone.service.impl;

import com.capstone.dto.response.PaginatedResponseDto;
import com.capstone.dto.response.SpecializationResponseDto;
import com.capstone.exception.SpecializationAlreadyExistsException;
import com.capstone.exception.SpecializationNotFoundException;
import com.capstone.exception.SpecializationProcessingException;
import com.capstone.mapper.SpecializationMapper;
import com.capstone.model.Specialization;
import com.capstone.repository.SpecializationRepository;
import com.capstone.service.SpecializationService;
import com.capstone.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.common.event.TalentRouteEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecializationServiceImpl implements SpecializationService {

    private final SpecializationRepository specializationRepository;
    private final SpecializationMapper specializationMapper;

    @Override
    @Transactional
    public void createSpecialization(TalentRouteEvent event) {
        log.info("Creating specialization with ID: {} and name: {}",
                event.getId(), event.getName());

        try {
            if (specializationRepository.existsBySpecId(event.getId())) {
                log.warn("Specialization with ID {} already exists, skipping creation",
                        event.getId());
                throw new SpecializationAlreadyExistsException(
                        String.format("Specialization with ID %s already exists", event.getId()));
            }

            if (specializationRepository.existsBySpecName(event.getName())) {
                log.warn("Specialization with name '{}' already exists", event.getName());
                throw new SpecializationAlreadyExistsException(
                        String.format("Specialization with name '%s' already exists", event.getName()));
            }

            Specialization specialization = specializationMapper.toEntity(event);
            specializationRepository.save(specialization);

            log.info("Successfully created specialization with ID: {}", event.getId());
        } catch (SpecializationAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to create specialization with ID: {}", event.getId(), e);
            throw new SpecializationProcessingException(
                    String.format("Failed to create specialization with ID %s", event.getId()), e);
        }
    }

    @Override
    @Transactional
    public void updateSpecialization(TalentRouteEvent event) {
        log.info("Updating specialization with ID: {} and name: {}",
                event.getId(), event.getName());

        try {
            Specialization existingSpec = specializationRepository.findBySpecId(event.getId())
                    .orElseThrow(() -> new SpecializationNotFoundException(
                            String.format("Specialization with ID %s not found for update", event.getId())));

            // Check if another specialization already has this name
            specializationRepository.findBySpecName(event.getName())
                    .ifPresent(spec -> {
                        if (!spec.getSpecId().equals(event.getId())) {
                            throw new SpecializationAlreadyExistsException(
                                    String.format("Another specialization with name '%s' already exists",
                                            event.getName()));
                        }
                    });

            existingSpec.setSpecName(event.getName());
            specializationRepository.save(existingSpec);

            log.info("Successfully updated specialization with ID: {}", event.getId());
        } catch (SpecializationNotFoundException | SpecializationAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update specialization with ID: {}", event.getId(), e);
            throw new SpecializationProcessingException(
                    String.format("Failed to update specialization with ID %s", event.getId()), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDto<SpecializationResponseDto> getAllSpecializations(Pageable pageable) {
        log.debug("Retrieving specializations with pagination: {}", pageable);

        try {
            Page<Specialization> specializationPage = specializationRepository.findAll(pageable);
            Page<SpecializationResponseDto> dtoPage = specializationPage.map(specializationMapper::toResponseDto);

            return PaginationUtil.toPaginatedResponse(dtoPage);
        } catch (Exception e) {
            log.error("Failed to retrieve specializations with pagination", e);
            throw new SpecializationProcessingException("Failed to retrieve specializations", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecializationResponseDto> searchSpecializations(String searchTerm) {
        log.debug("Searching specializations with term: '{}'", searchTerm);

        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.warn("Empty search term provided, returning empty list");
                return List.of();
            }

            List<Specialization> specializations = specializationRepository
                    .findBySpecNameContainingIgnoreCase(searchTerm.trim());

            List<SpecializationResponseDto> result = specializations.stream()
                    .map(specializationMapper::toResponseDto)
                    .toList();

            log.info("Found {} specializations matching search term: '{}'", result.size(), searchTerm);
            return result;

        } catch (Exception e) {
            log.error("Failed to search specializations with term: '{}'", searchTerm, e);
            throw new SpecializationProcessingException(
                    String.format("Failed to search specializations with term '%s'", searchTerm), e);
        }
    }

}
