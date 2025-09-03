package com.medops.application.eventsourcing.handler;

import com.medops.domain.enums.MedicalRecordStatus;
import com.medops.application.eventsourcing.event.Confirmed;
import com.medops.domain.model.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class ConfirmedEventHandler implements EventHandler<Confirmed> {

    @Override
    public MedicalRecord handleEvent(MedicalRecord state, Confirmed event) {
        return state.toBuilder()
            .status(MedicalRecordStatus.RESERVED)
            .build();
    }
}