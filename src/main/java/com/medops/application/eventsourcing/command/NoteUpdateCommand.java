package com.medops.application.eventsourcing.command;

import lombok.Getter;

@Getter
public class NoteUpdateCommand extends StreamCommand{
    private final String note;

    public NoteUpdateCommand(
        String recordId, String userId, String HospitalId, String note
    ){
        super(recordId, userId, HospitalId);
        this.note = note;
    }
}
