package com.medops.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
@Document("medops_admin_registration_token")
public class AdminRegistrationTokenDocument {
    @Id
    private String id;
    private String adminId;
    private String registrationToken;
    private LocalDateTime registrationTokenExpiresAt;
    private LocalDateTime registeredAt;
}
