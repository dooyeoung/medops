package com.medops.adapter.in.web.request;


import java.time.Instant;

public record FollowUpReservationRequest(
    String userId,
    String hospitalId,
    String treatmentProductId,
    Instant startTime,
    Instant endTime,
    String note
) {
}