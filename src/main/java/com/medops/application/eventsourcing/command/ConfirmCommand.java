package com.medops.application.eventsourcing.command;

import lombok.Getter;

@Getter
public class ConfirmCommand extends StreamCommand {
    private final String adminId;

    public ConfirmCommand(String recordId, String userId, String hospitalId, String adminId) {
        super(recordId, userId, hospitalId);
        this.adminId = adminId;
    }
}
