package com.medops.domain.model;

import com.medops.domain.enums.AdminRole;
import com.medops.domain.enums.AdminStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class Admin implements UserDetails {
    private String id;
    private String name;
    private String email;
    @Setter
    private String password;
    private AdminRole role;
    private AdminStatus status;
    private Hospital hospital;
    private Instant createdAt;
    private Instant deletedAt;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public void activateAccount(String name, String password){
        this.name = name;
        this.password = password;
        this.status = AdminStatus.ACTIVATED;
    }
}
