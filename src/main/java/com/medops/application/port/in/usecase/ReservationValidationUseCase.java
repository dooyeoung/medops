package com.medops.application.port.in.usecase;

import java.time.Instant;

public interface ReservationValidationUseCase {

    boolean isReservationAvailable(String hospitalId, String treatmentProductId, Instant startTime, Instant endTime);
}
