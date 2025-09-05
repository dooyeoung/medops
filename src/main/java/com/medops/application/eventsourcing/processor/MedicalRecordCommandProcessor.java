package com.medops.application.eventsourcing.processor;

import com.medops.application.eventsourcing.command.executor.*;
import com.medops.application.eventsourcing.handler.*;
import com.medops.application.port.in.usecase.ReservationValidationUseCase;
import com.medops.application.port.out.LoadAdminPort;
import com.medops.application.port.out.LoadDoctorPort;
import com.medops.application.port.out.LoadMedicalRecordSnapshotPort;
import com.medops.application.port.out.MedicalRecordEventStorePort;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.domain.model.MedicalRecord;
import com.medops.domain.model.MedicalRecordSnapshot;
import com.medops.application.eventsourcing.command.StreamCommand;
import org.springframework.context.ApplicationEventPublisher; // 추가
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toMap;

@Service
public class MedicalRecordCommandProcessor {

    private static final int SNAPSHOT_INTERVAL = 100;

    private final MedicalRecordEventStorePort medicalRecordEventStorePort;
    private final Function<String, MedicalRecord> seedFactory;
    private final Map<Class<?>, CommandExecutor<Object>> commandExecutors;
    private final Map<Class<?>, EventHandler<MedicalRecordEvent>> eventHandlers;
    private final LoadMedicalRecordSnapshotPort loadMedicalRecordSnapshotPort;
    private final ApplicationEventPublisher eventPublisher;
    private final LoadAdminPort loadAdminPort;
    private final LoadDoctorPort loadDoctorPort;
    private final ReservationValidationUseCase reservationValidationUseCase;

    public MedicalRecordCommandProcessor(
        MedicalRecordEventStorePort medicalRecordEventStorePort,
        LoadMedicalRecordSnapshotPort loadMedicalRecordSnapshotPort,
        ApplicationEventPublisher eventPublisher,
        LoadAdminPort loadAdminPort,
        LoadDoctorPort loadDoctorPort,
        ReservationValidationUseCase reservationValidationUseCase
    ) {
        this.reservationValidationUseCase = reservationValidationUseCase;
        this.medicalRecordEventStorePort = medicalRecordEventStorePort;
        this.loadMedicalRecordSnapshotPort = loadMedicalRecordSnapshotPort;
        this.eventPublisher = eventPublisher; // 추가
        this.seedFactory = MedicalRecord::seedFactory;
        this.loadAdminPort = loadAdminPort;
        this.loadDoctorPort = loadDoctorPort;

        this.eventHandlers = toDictionary(
            List.of(
                new ReservationCreatedEventHandler(),
                new NoteUpdatedEventHandler(),
                new DoctorAssignedEventHandler(),
                new ConfirmedEventHandler(),
                new PendingEventHandler(),
                new CanceledEventHandler(),
                new CompletedEventHandler()
            ),
            EventHandler::getEventType
        );

        this.commandExecutors = toDictionary(
            List.of(
                new ReservationCreatedCommandExecutor(reservationValidationUseCase),
                new NoteUpdateCommandExecutor(),
                new DoctorAssignCommandExecutor(loadAdminPort, loadDoctorPort),
                new ConfirmCommandExecutor(loadAdminPort),
                new PendingCommandExecutor(loadAdminPort),
                new CancelCommandExecutor(loadAdminPort),
                new CompleteCommandExecutor(loadAdminPort)
            ),
            CommandExecutor::getCommandType
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> Map<Class<?>, T> toDictionary(
        Iterable<?> items, Function<T, Class<?>> keyMapper
    ) {
        return StreamSupport.stream(items.spliterator(), false)
            .map(e -> (T) e)
            .collect(toMap(keyMapper, Function.identity()));
    }

    public MedicalRecordSnapshot getSnapshot(String streamId) {
        return rehydrateState(streamId);
    }

    public void handle(StreamCommand command) {
        MedicalRecordSnapshot snapshotBefore = rehydrateState(command.getRecordId());

        CommandExecutor<Object> executor = commandExecutors.get(command.getClass());
        if (executor == null) {
            throw new IllegalArgumentException("No CommandExecutor found for command type: " + command.getClass().getName());
        }

        Iterable<MedicalRecordEvent> newEvents = executor.produceEvents(snapshotBefore.getState(), command);

        // 1. 이벤트를 DB에 저장
        medicalRecordEventStorePort.collectEvents(
            command.getRecordId(),
            command.getHospitalId(),
            command.getUserId(),
            snapshotBefore.getVersion() + 1,
            newEvents
        );

        // 2. 저장된 이벤트를 발행 (추가된 부분)
        newEvents.forEach(eventPublisher::publishEvent);

        MedicalRecordSnapshot snapshotAfter = applyEvents(snapshotBefore, newEvents);

        if (shouldCreateSnapshot(snapshotAfter)) {
//            snapshotRepository.save(snapshotAfter);
            System.out.println("snapshot 저장");
        }
    }

    public MedicalRecordSnapshot rehydrateState(String recordId) {
        // 스냅샷 조회
        Optional<MedicalRecordSnapshot> optionalLatestSnapshot = loadMedicalRecordSnapshotPort.loadMedicalRecordSnapshot(recordId);
        MedicalRecordSnapshot startingPoint = optionalLatestSnapshot.orElseGet(() -> MedicalRecordSnapshot.seed(recordId, seedFactory.apply(recordId)));

        // 스냅샷 버전
        List<Object> subsequentEvents = medicalRecordEventStorePort.queryEvents(recordId, startingPoint.getVersion() + 1);
        return applyEvents(startingPoint, subsequentEvents);
    }

    private MedicalRecordSnapshot applyEvents(MedicalRecordSnapshot initialSnapshot, Iterable<?> events) {
        MedicalRecordSnapshot currentSnapshot = initialSnapshot;
        for (Object event : events) {
            EventHandler<MedicalRecordEvent> handler = eventHandlers.get(event.getClass());
            if (handler == null) {
                throw new IllegalArgumentException("No EventHandler found for event type: " + event.getClass().getName());
            }

            MedicalRecord newState = handler.handleEvent(currentSnapshot.getState(), (MedicalRecordEvent) event);
            currentSnapshot = MedicalRecordSnapshot.builder()
                .id(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .recordId(currentSnapshot.getRecordId())
                .state(newState)
                .version(currentSnapshot.getVersion() + 1)
                .build();
        }
        return currentSnapshot;
    }

    private boolean shouldCreateSnapshot(MedicalRecordSnapshot snapshot) {
        return snapshot.getVersion() % 1 == 0;
    }
}
