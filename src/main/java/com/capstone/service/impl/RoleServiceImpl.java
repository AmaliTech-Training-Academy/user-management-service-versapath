package com.capstone.service.impl;

import com.capstone.dto.response.RoleDto;
import com.capstone.exception.RoleNotFoundException;
import com.capstone.mapper.RoleMapper;
import com.capstone.model.Role;
import com.capstone.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roleMapper.toRoleDtoList(roles);
    }
    public RoleDto getRoleById(UUID id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + id));
        return roleMapper.toRoleDto(role);
    }

}
