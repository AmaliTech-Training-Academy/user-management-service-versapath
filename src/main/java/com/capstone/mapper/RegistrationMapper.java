package com.capstone.mapper;

import com.capstone.dto.request.PasswordSetupRequest;
import com.capstone.dto.request.UserRegistrationRequest;
import com.capstone.dto.response.PasswordSetupResponse;
import com.capstone.dto.response.UserRegistrationResponse;
import com.capstone.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RegistrationMapper {
    /**
     * Map registration request to User entity for initial invitation
     * Only maps email, other fields will be null initially
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "mentor", ignore = true)
    @Mapping(target = "learners", ignore = true)
    @Mapping(target = "moodleUserId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserRegistrationRequest request);

    /*
     * Map User entity to registration response
     */
    @Mapping(target = "registrationLink", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "tokenExpiresAt", ignore = true)
    @Mapping(source = "id", target = "userId")
    UserRegistrationResponse toRegistrationResponse(User user);

    /*
     * Update existing user entity with registration completion data
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "mentor", ignore = true)
    @Mapping(target = "learners", ignore = true)
    @Mapping(target = "moodleUserId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUserFromRegistrationCompletion(@MappingTarget User user, PasswordSetupRequest request);

    /*
     * Map User entity to password setup response
     */
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "success", ignore = true)
    @Mapping(source = "id", target = "userId")
    PasswordSetupResponse toPasswordSetupResponse(User user);
}
