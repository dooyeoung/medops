package com.medops.application.eventsourcing.command;

import lombok.Getter;

@Getter
public class DoctorAssignCommand extends StreamCommand{
    private final String doctorId;
    private final String adminId;

    public DoctorAssignCommand(
        String recordId, String userId, String HospitalId, String doctorId, String adminId
    ){
        super(recordId, userId, HospitalId);
        this.doctorId = doctorId;
        this.adminId = adminId;
    }
}
