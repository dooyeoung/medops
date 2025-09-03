package com.medops.application.port.in.usecase;

import com.medops.adapter.in.web.request.UserLoginRequest;
import com.medops.adapter.in.web.request.UserRegisterRequest;
import com.medops.domain.model.User;

public interface UserUseCase {
    User createUser(UserRegisterRequest request);
    String loginUser(UserLoginRequest request);
}
