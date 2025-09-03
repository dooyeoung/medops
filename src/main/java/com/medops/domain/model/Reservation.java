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
public class Reservation {
    private String userMemo;
    private String treatmentProductId;
    private Instant startTime;
    private Instant endTime;
}
