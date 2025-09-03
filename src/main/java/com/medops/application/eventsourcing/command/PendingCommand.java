package com.medops.application.eventsourcing.command;

import lombok.Getter;

@Getter
public class PendingCommand extends StreamCommand {
    private final String adminId;

    public PendingCommand(String recordId, String userId, String hospitalId, String adminId) {
        super(recordId, userId, hospitalId);
        this.adminId = adminId;
    }
}
