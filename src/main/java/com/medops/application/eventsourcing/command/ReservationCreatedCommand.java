package com.medops.application.eventsourcing.command;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ReservationCreatedCommand extends StreamCommand {
    private final String treatmentProductId;
    private final Instant startTime;
    private final Instant endTime;
    private final String memo;
    private final String note;

    public ReservationCreatedCommand(
        String streamId, String userId, String hospitalId, String treatmentProductId, Instant startTime, Instant endTime, String memo, String note
    ) {
        super(streamId, userId, hospitalId);
        this.treatmentProductId = treatmentProductId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
        this.note = note;
    }
}