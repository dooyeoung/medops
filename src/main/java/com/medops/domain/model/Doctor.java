package com.medops.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Doctor {
    private String id;
    private String name;
    private String hospitalId;
    private Instant createdAt;
    private Instant deletedAt;
}
