package com.medops.application.eventsourcing.handler;

import com.medops.domain.enums.MedicalRecordStatus;
import com.medops.domain.model.MedicalRecord;
import com.medops.application.eventsourcing.event.ReservationCreated;
import org.springframework.stereotype.Component;

@Component
public class ReservationCreatedEventHandler implements EventHandler<ReservationCreated> {

    @Override
    public MedicalRecord handleEvent(MedicalRecord state, ReservationCreated event) {
        return state.toBuilder()
            .reservation(
                state.getReservation().toBuilder()
                    .startTime(event.getStartTime())
                    .endTime(event.getEndTime())
                    .treatmentProductId(event.getTreatmentProductId())
                    .userMemo(event.getUserMemo())
                    .build()
            )
            .status(MedicalRecordStatus.PENDING)
            .build();
    }
}