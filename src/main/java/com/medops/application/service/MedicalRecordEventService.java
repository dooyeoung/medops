package com.medops.application.service;

import com.medops.application.port.in.usecase.MedicalRecordEventUseCase;
import com.medops.application.port.out.LoadMedicalRecordEventPort;
import com.medops.domain.event.MedicalRecordEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordEventService implements MedicalRecordEventUseCase {
    private final LoadMedicalRecordEventPort loadMedicalRecordEventPort;

    @Override
    public List<MedicalRecordEvent> getEventsByRecordId(String recordId) {
        return loadMedicalRecordEventPort.loadEventsByRecordId(recordId);
    }
}
