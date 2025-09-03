package com.medops.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@AllArgsConstructor
@Document("medops_hospital")
public class HospitalDocument {
    @Id
    private String id;
    private String name;
    private String address;
    private Instant createdAt;
}
