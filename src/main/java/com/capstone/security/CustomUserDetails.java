package com.capstone.security;

import com.capstone.model.EStatus;
import com.capstone.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails{

    private UUID id;
    private String email;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String roleName;
    private EStatus status;

    public static CustomUserDetails fromUser(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().getRole().name(),
                user.getStatus()
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Using email as username for login
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != EStatus.INACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == EStatus.ACTIVE;
    }

    // Helper methods for easier access to user data
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getRoleWithoutPrefix() {
        return roleName;
    }

    public String getActualUsername() {
        return username;
    }
}
