package com.medops.adapter.out.persistence.mongodb.document;

import com.medops.domain.enums.AdminRole;
import com.medops.domain.enums.AdminStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Document("medops_admin")
public class AdminDocument {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private AdminRole role;
    private AdminStatus status;
    private String hospitalId;
    private Instant createdAt;
    private Instant deletedAt;
}
