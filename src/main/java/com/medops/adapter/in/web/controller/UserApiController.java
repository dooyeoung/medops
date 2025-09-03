package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.annotation.UserSession;
import com.medops.adapter.in.web.request.UserLoginRequest;
import com.medops.adapter.in.web.request.UserRegisterRequest;
import com.medops.application.port.in.usecase.UserUseCase;
import com.medops.common.response.Api;
import com.medops.domain.model.User;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController implements UserApiControllerSpec{
    private final UserUseCase userUseCase;

    @Override
    @PostMapping(path="")
    public Api<User> register(@RequestBody UserRegisterRequest request){
        return Api.OK(userUseCase.createUser(request));
    }

    @Override
    @PostMapping(path = "/login")
    public Api<String> login(@RequestBody UserLoginRequest request) {
        return Api.OK(userUseCase.loginUser(request));
    }

    @Override
    @GetMapping("/me")
    public Api<User> me(
        @Parameter(hidden = true)
        @UserSession User user
    ) {
        return Api.OK(user);
    }
}
