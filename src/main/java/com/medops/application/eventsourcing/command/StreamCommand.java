package com.medops.application.eventsourcing.command;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class StreamCommand {
    private final String recordId;
    private final String userId;
    private final String hospitalId;
}
