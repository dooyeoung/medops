package com.medops.application.eventsourcing.handler;

import com.medops.application.eventsourcing.event.NoteUpdated;
import com.medops.domain.model.MedicalRecord;
import org.springframework.stereotype.Component;

@Component
public class NoteUpdatedEventHandler implements EventHandler<NoteUpdated> {

    @Override
    public MedicalRecord handleEvent(MedicalRecord state, NoteUpdated event) {
        return state.toBuilder()
            .status(state.getStatus())
            .note(event.getNote())
            .build();
    }
}