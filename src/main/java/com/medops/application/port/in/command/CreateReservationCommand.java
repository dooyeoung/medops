package com.medops.application.port.in.command;

import java.time.Instant;

public record CreateReservationCommand(
    String hospitalId, String treatmentProductId, String userId, Instant startTime, Instant endTime, String note
) {
}
