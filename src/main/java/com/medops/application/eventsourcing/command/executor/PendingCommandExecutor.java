package com.medops.application.eventsourcing.command.executor;

import com.medops.application.eventsourcing.command.PendingCommand;
import com.medops.application.port.out.LoadAdminPort;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.application.eventsourcing.event.Pending;
import com.medops.domain.model.MedicalRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PendingCommandExecutor implements CommandExecutor<PendingCommand> {
    private final LoadAdminPort loadAdminPort;


    @Override
    public Iterable<MedicalRecordEvent> produceEvents(MedicalRecord state, PendingCommand command) {
        if (!state.getEvents().isEmpty()) {
            throw new RuntimeException("Already reserved");
        }

        String adminName = loadAdminPort.loadAdminById(command.getAdminId()).orElseThrow().getName();
        return List.of(
            new Pending(command.getRecordId(), command.getAdminId(), adminName)
        );
    }

}
