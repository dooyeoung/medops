package com.medops.application.eventsourcing.command.executor;

import com.medops.application.eventsourcing.command.DoctorAssignCommand;
import com.medops.application.port.out.LoadAdminPort;
import com.medops.application.port.out.LoadDoctorPort;
import com.medops.application.eventsourcing.event.DoctorAssigned;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.domain.model.MedicalRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DoctorAssignCommandExecutor implements CommandExecutor<DoctorAssignCommand> {
    private final LoadAdminPort loadAdminPort;
    private final LoadDoctorPort loadDoctorPort;


    @Override
    public Iterable<MedicalRecordEvent> produceEvents(MedicalRecord state, DoctorAssignCommand command) {
        if (!state.getEvents().isEmpty()) {
            throw new RuntimeException("Already reserved");
        }
        String adminName = loadAdminPort.loadAdminById(command.getAdminId()).orElseThrow().getName();
        String doctorName = loadDoctorPort.loadDoctorById(command.getDoctorId()).orElseThrow().getName();

        return List.of(
            new DoctorAssigned(
                command.getRecordId(),
                state.getStatus(),
                command.getDoctorId(),
                doctorName,
                command.getAdminId(),
                adminName
            )
        );
    }

}
