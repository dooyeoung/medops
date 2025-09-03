package com.medops.application.port.in.usecase;

import com.medops.domain.event.MedicalRecordEvent;

import java.util.List;

public interface MedicalRecordEventUseCase {
    List<MedicalRecordEvent> getEventsByRecordId(String recordId);

}
