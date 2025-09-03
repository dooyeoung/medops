package com.medops.application.eventsourcing.command;

import lombok.Getter;

@Getter
public class CompleteCommand extends StreamCommand {
    private final String adminId;
    public CompleteCommand(String recordId, String userId, String hospitalId, String adminId) {
        super(recordId, userId, hospitalId);
        this.adminId = adminId;
    }
}
