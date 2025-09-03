package com.medops.application.eventsourcing.command.executor;

import com.medops.application.eventsourcing.command.ReservationCreatedCommand;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.domain.model.MedicalRecord;
import com.medops.application.eventsourcing.event.ReservationCreated;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationCreatedCommandExecutor implements CommandExecutor<ReservationCreatedCommand> {

    @Override
    public Iterable<MedicalRecordEvent> produceEvents(MedicalRecord state, ReservationCreatedCommand command) {
        if (!state.getEvents().isEmpty()) {
            throw new RuntimeException("Already reserved");
        }
        return List.of(
            new ReservationCreated(
                command.getRecordId(),
                state.getStatus(),
                command.getUserId(),
                command.getHospitalId(),
                command.getTreatmentProductId(),
                command.getStartTime(),
                command.getEndTime(),
                command.getMemo(),
                command.getNote()
            )
        );
    }

}
