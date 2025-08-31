package com.capstone.mapper;

import com.capstone.dto.response.RoleDto;
import com.capstone.model.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDto toRoleDto(Role role);
    
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "theUsers", ignore = true)
    Role toRole(RoleDto roleDto);
    List<RoleDto> toRoleDtoList(List<Role> roles);
    List<Role> toRoleList(List<RoleDto> roleDtos);
}
