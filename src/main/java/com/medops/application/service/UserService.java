package com.medops.application.service;

import com.medops.adapter.in.web.request.UserLoginRequest;
import com.medops.adapter.in.web.request.UserRegisterRequest;
import com.medops.application.port.out.TokenPort;
import com.medops.domain.enums.TokenType;
import com.medops.application.port.in.usecase.UserUseCase;
import com.medops.application.port.out.LoadUserPort;
import com.medops.application.port.out.SaveUserPort;
import com.medops.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final SaveUserPort saveUserPort;
    private final LoadUserPort loadUserPort;
    private final TokenPort tokenPort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(UserRegisterRequest request) {
        if (loadUserPort.existsByEmail(request.email())){
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        var user = User.builder()
            .id(UUID.randomUUID().toString())
            .email(request.email())
            .name(request.name())
            .password(passwordEncoder.encode(request.password()))
            .build();
        return saveUserPort.saveUser(user);
    }

    @Override
    public String loginUser(UserLoginRequest request) {
        return loadUserPort.loadUserByEmail(request.email())
            .map(user -> {
                if (passwordEncoder.matches(user.getPassword(), request.password())){
                    throw new IllegalAccessError();
                }
                return tokenPort.generateToken(user.getId(), TokenType.USER);
            })
            .orElseThrow(IllegalAccessError::new);
    }
}
