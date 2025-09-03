package com.medops.application.port.out;

import com.medops.domain.model.User;

public interface SaveUserPort {
    User saveUser(User user);
}
