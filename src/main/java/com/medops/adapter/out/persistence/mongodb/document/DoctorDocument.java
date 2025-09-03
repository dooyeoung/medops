package com.medops.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Document("medops_doctor")
public class DoctorDocument {
    @Id
    private String id;
    private String hospitalId;
    private String name;
    private Instant createdAt;
    @Setter
    private Instant deletedAt;
}
