package com.medops.application.eventsourcing.command.executor;

import com.medops.application.eventsourcing.command.CancelCommand;
import com.medops.application.port.out.LoadAdminPort;
import com.medops.application.eventsourcing.event.Canceled;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.domain.model.MedicalRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CancelCommandExecutor implements CommandExecutor<CancelCommand> {
    private final LoadAdminPort loadAdminPort;

    @Override
    public Iterable<MedicalRecordEvent> produceEvents(MedicalRecord state, CancelCommand command) {
        if (!state.getEvents().isEmpty()) {
            throw new RuntimeException("Already reserved");
        }

        String adminName = loadAdminPort.loadAdminById(command.getAdminId()).orElseThrow().getName();

        return List.of(
            new Canceled(command.getRecordId(), command.getAdminId(), adminName)
        );
    }

}
