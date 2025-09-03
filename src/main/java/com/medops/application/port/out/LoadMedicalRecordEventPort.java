package com.medops.application.port.out;

import com.medops.domain.event.MedicalRecordEvent;

import java.util.List;

public interface LoadMedicalRecordEventPort {
    List<MedicalRecordEvent> loadEventsByRecordId(String recordId);
}
