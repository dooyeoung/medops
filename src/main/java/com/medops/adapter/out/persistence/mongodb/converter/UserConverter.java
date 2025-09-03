package com.medops.adapter.out.persistence.mongodb.converter;

import com.medops.adapter.out.persistence.mongodb.document.UserDocument;
import com.medops.domain.model.User;
import org.springframework.stereotype.Component;


@Component
public class UserConverter {
    public UserDocument toDocument(User user){
        return new UserDocument(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getName(),
            user.getCreatedAt()
        );
    }

    public User toDomain(UserDocument userDocument) {
        return User.builder()
            .id(userDocument.getId())
            .email(userDocument.getEmail())
            .name(userDocument.getName())
            .password(userDocument.getPassword())
            .createdAt(userDocument.getCreatedAt())
            .build();
    }
}
