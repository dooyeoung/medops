package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.converter.UserConverter;
import com.medops.adapter.out.persistence.mongodb.repository.UserDocumentRepository;
import com.medops.application.port.out.LoadUserPort;
import com.medops.application.port.out.SaveUserPort;
import com.medops.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements SaveUserPort, LoadUserPort {

    private final UserDocumentRepository userDocumentRepository;
    private final UserConverter userConverter;

    @Override
    public User saveUser(User user) {
        var savedUser = userDocumentRepository.save(userConverter.toDocument(user));
        return userConverter.toDomain(savedUser);
    }

    @Override
    public Optional<User> loadUserByEmail(String email) {
        return userDocumentRepository.findByEmail(email).map(userConverter::toDomain);
    }

    @Override
    public Optional<User> loadUserById(String id) {
        return userDocumentRepository.findById(id).map(userConverter::toDomain);
    }

    @Override
    public List<User> loadUsersByIds(List<String> ids) {
        return userDocumentRepository.findAllById(ids).stream().map(userConverter::toDomain).toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return userDocumentRepository.existsByEmail(email);
    }
}
