package com.capstone.mapper;

import com.capstone.dto.response.RoleDto;
import com.capstone.model.Role;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleDto toRoleDto(Role role);
    Role toRole(RoleDto roleDto);
    List<RoleDto> toRoleDtoList(List<Role> roles);
    List<Role> toRoleList(List<RoleDto> roleDtos);
}
