package com.medops.application.eventsourcing.command.executor;

import com.medops.application.eventsourcing.command.NoteUpdateCommand;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.application.eventsourcing.event.NoteUpdated;
import com.medops.domain.model.MedicalRecord;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoteUpdateCommandExecutor implements CommandExecutor<NoteUpdateCommand> {

    @Override
    public Iterable<MedicalRecordEvent> produceEvents(MedicalRecord state, NoteUpdateCommand command) {
        if (!state.getEvents().isEmpty()) {
            throw new RuntimeException("Already reserved");
        }
        return List.of(
            new NoteUpdated(
                command.getRecordId(),
                state.getStatus(),
                command.getNote()
            )
        );
    }

}
