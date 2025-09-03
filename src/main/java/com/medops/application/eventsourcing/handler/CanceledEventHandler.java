package com.medops.application.eventsourcing.handler;

import com.medops.domain.enums.MedicalRecordStatus;
import com.medops.application.eventsourcing.event.Canceled;
import com.medops.domain.model.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class CanceledEventHandler implements EventHandler<Canceled> {

    @Override
    public MedicalRecord handleEvent(MedicalRecord state, Canceled event) {
        return state.toBuilder()
            .status(MedicalRecordStatus.CANCELED)
            .build();
    }
}