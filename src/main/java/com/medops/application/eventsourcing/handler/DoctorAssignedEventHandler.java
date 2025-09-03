package com.medops.application.eventsourcing.handler;

import com.medops.application.eventsourcing.event.DoctorAssigned;
import com.medops.domain.model.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class DoctorAssignedEventHandler implements EventHandler<DoctorAssigned> {

    @Override
    public MedicalRecord handleEvent(MedicalRecord state, DoctorAssigned event) {
        return state.toBuilder()
            .doctor(event.getDoctorId())
            .build();
    }
}