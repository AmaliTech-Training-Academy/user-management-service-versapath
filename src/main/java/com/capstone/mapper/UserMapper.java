package com.capstone.mapper;

import com.capstone.dto.response.UserInfoDto;
import com.capstone.dto.response.UserProfileDto;
import com.capstone.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "id", expression = "java(user.getId().toString())")
    @Mapping(target = "role", expression = "java(user.getRole().getRole().name().replace(\"ROLE_\", \"\"))")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    UserInfoDto toUserInfoDto(User user);
    
    @Mapping(target = "id", expression = "java(user.getId().toString())")
    @Mapping(target = "role", expression = "java(user.getRole().getRole().name().replace(\"ROLE_\", \"\"))")
    UserProfileDto toUserProfileDto(User user);

}