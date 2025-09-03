package com.medops.application.eventsourcing.command;

import lombok.Getter;

@Getter
public class CancelCommand extends StreamCommand {
    private final String adminId;
    public CancelCommand(String recordId, String userId, String hospitalId, String adminId) {
        super(recordId, userId, hospitalId);
        this.adminId = adminId;
    }
}
