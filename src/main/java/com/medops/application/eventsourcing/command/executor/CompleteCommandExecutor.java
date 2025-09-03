package com.medops.application.eventsourcing.command.executor;

import com.medops.application.eventsourcing.command.CompleteCommand;
import com.medops.application.port.out.LoadAdminPort;
import com.medops.application.eventsourcing.event.Completed;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.domain.model.MedicalRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CompleteCommandExecutor implements CommandExecutor<CompleteCommand> {
    private final LoadAdminPort loadAdminPort;

    @Override
    public Iterable<MedicalRecordEvent> produceEvents(MedicalRecord state, CompleteCommand command) {
        if (!state.getEvents().isEmpty()) {
            throw new RuntimeException("Already reserved");
        }

        String adminName = loadAdminPort.loadAdminById(command.getAdminId()).orElseThrow().getName();

        return List.of(
            new Completed(command.getRecordId(), command.getAdminId(), adminName)
        );
    }

}
