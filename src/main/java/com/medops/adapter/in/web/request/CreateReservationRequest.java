package com.medops.adapter.in.web.request;

import java.time.Instant;

public record CreateReservationRequest(
    String hospitalId,
    String treatmentProductId,
    Instant startTime,
    Instant endTime,
    String userMemo
) {
}