package com.medops.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Document("medops_user")
public class UserDocument {
    @Id
    private String id;
    private String email;
    private String password;
    private String name;
    private Instant createdAt;
}
