package com.medops.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class User implements UserDetails {
    private Instant createdAt;

    private String id;
    private String email;
    private String name;
    private String password;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return this.email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        // 계정 잠금 정책을 구현할 때 사용됩니다.
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        // 계정 유효 기간 정책을 구현할 때 사용됩니다
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        // 비밀번호 만료 정책을 구현할 때 사용됩니다.
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
