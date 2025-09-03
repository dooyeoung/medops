package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.document.UserDocument;
import com.medops.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class UserPersistenceAdapterTest {
    @Autowired
    private UserPersistenceAdapter sut;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Test
    void saveUser() {
        // given
        var user = User.builder()
            .id(UUID.randomUUID().toString())
            .email("test@test.com")
            .password("1234!@#$")
            .name("test")
            .build();

        // when
        sut.saveUser(user);

        // then
        var savedUserDocument = mongoTemplate.findById(user.getId(), UserDocument.class);
        assertEquals(savedUserDocument.getId(), user.getId());
    }

}
