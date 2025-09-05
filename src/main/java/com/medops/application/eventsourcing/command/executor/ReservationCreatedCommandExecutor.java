package com.medops.application.eventsourcing.command.executor;

import com.medops.application.eventsourcing.command.ReservationCreatedCommand;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.application.port.in.usecase.ReservationValidationUseCase;
import com.medops.domain.model.MedicalRecord;
import com.medops.application.eventsourcing.event.ReservationCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationCreatedCommandExecutor implements CommandExecutor<ReservationCreatedCommand> {
    private final ReservationValidationUseCase reservationValidationUseCase;

    @Override
    public Iterable<MedicalRecordEvent> produceEvents(MedicalRecord state, ReservationCreatedCommand command) {
        if (!state.getEvents().isEmpty()) {
            throw new RuntimeException("Already reserved");
        }

        if (
            reservationValidationUseCase.isReservationAvailable(
                command.getHospitalId(), command.getTreatmentProductId(), command.getStartTime(), command.getEndTime()
            )
        ) {
          throw new IllegalArgumentException("해당 시간에 예약이 완료되어 예약 신청이 불가합니다.");
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
