package com.medops.domain.model;

import lombok.*;

import java.time.Instant;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Hospital {
    private String id;
    private String name;
    private String address;
    private Instant createdAt;
}
