package com.medops.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;


@Getter
@AllArgsConstructor
@Builder
public class ReservationDocumentComponent {
    private final Instant startTime;
    private final Instant endTime;
    private final String treatmentProductId;
    private final String userMemo;
}
