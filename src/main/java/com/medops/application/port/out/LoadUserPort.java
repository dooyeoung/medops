package com.medops.application.port.out;

import com.medops.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface LoadUserPort {
    Optional<User> loadUserByEmail(String email);
    Optional<User> loadUserById(String id);
    List<User> loadUsersByIds(List<String> ids);
    boolean existsByEmail(String email);
}
