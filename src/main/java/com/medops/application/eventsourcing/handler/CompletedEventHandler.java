package com.medops.application.eventsourcing.handler;

import com.medops.domain.enums.MedicalRecordStatus;
import com.medops.application.eventsourcing.event.Completed;
import com.medops.domain.model.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class CompletedEventHandler implements EventHandler<Completed> {

    @Override
    public MedicalRecord handleEvent(MedicalRecord state, Completed event) {
        return state.toBuilder()
            .status(MedicalRecordStatus.COMPLETED)
            .build();
    }
}