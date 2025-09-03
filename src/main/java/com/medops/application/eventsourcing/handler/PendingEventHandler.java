package com.medops.application.eventsourcing.handler;

import com.medops.domain.enums.MedicalRecordStatus;
import com.medops.application.eventsourcing.event.Pending;
import com.medops.domain.model.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class PendingEventHandler implements EventHandler<Pending> {

    @Override
    public MedicalRecord handleEvent(MedicalRecord state, Pending event) {
        return state.toBuilder()
            .status(MedicalRecordStatus.PENDING)
            .build();
    }
}