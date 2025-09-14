package com.medops.application.eventsourcing.processor;

import com.medops.application.eventsourcing.command.executor.CommandExecutor;
import com.medops.application.eventsourcing.handler.EventHandler;
import com.medops.application.port.out.LoadMedicalRecordSnapshotPort;
import com.medops.application.port.out.MedicalRecordEventStorePort;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.application.port.out.SaveMedicalRecordSnapshotPort;
import com.medops.domain.model.MedicalRecord;
import com.medops.domain.model.MedicalRecordSnapshot;
import com.medops.application.eventsourcing.command.StreamCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class MedicalRecordCommandProcessor {

    private static final int SNAPSHOT_INTERVAL = 5;

    private final MedicalRecordEventStorePort medicalRecordEventStorePort;
    private final LoadMedicalRecordSnapshotPort loadMedicalRecordSnapshotPort;
    private final SaveMedicalRecordSnapshotPort saveMedicalRecordSnapshotPort;
    private final ApplicationEventPublisher eventPublisher;
    private final CommandExecutorFactory commandExecutorFactory;
    private final EventHandlerFactory eventHandlerFactory;
    private final Function<String, MedicalRecord> seedFactory;

    @SuppressWarnings("unchecked")
    private <T extends StreamCommand> Iterable<MedicalRecordEvent> produceEventsForCommand(MedicalRecord state, T command) {
        Class<T> commandType = (Class<T>) command.getClass();
        CommandExecutor<T> executor = commandExecutorFactory.getExecutor(commandType);
        return executor.produceEvents(state, command);
    }

    @SuppressWarnings("unchecked")
    private <T extends MedicalRecordEvent> MedicalRecordSnapshot applyAndWrapEvent(MedicalRecordSnapshot currentSnapshot, T event) {
        Class<T> eventType = (Class<T>) event.getClass();
        EventHandler<T> handler = eventHandlerFactory.getHandler(eventType);
        MedicalRecord newState = handler.handleEvent(currentSnapshot.getState(), event);

        return MedicalRecordSnapshot.builder()
            .id(currentSnapshot.getId())
            .recordId(currentSnapshot.getRecordId())
            .createdAt(Instant.now())
            .state(newState)
            .version(currentSnapshot.getVersion() + 1)
            .build();
    }

    private MedicalRecordSnapshot applyEvents(MedicalRecordSnapshot initialSnapshot, Iterable<?> events) {
        MedicalRecordSnapshot currentSnapshot = initialSnapshot;
        for (Object event : events) {
            currentSnapshot = applyAndWrapEvent(currentSnapshot, (MedicalRecordEvent) event);
        }
        return currentSnapshot;
    }

    public MedicalRecordSnapshot rehydrateState(String recordId) {
        Optional<MedicalRecordSnapshot> optionalLatestSnapshot = loadMedicalRecordSnapshotPort.loadMedicalRecordSnapshot(recordId);
        MedicalRecordSnapshot startingPoint = optionalLatestSnapshot.orElseGet(() -> MedicalRecordSnapshot.seed(recordId, seedFactory.apply(recordId)));

        List<Object> subsequentEvents = medicalRecordEventStorePort.queryEvents(recordId, startingPoint.getVersion() + 1);
        return applyEvents(startingPoint, subsequentEvents);
    }


    public void handle(StreamCommand command) {
        MedicalRecordSnapshot snapshotBefore = rehydrateState(command.getRecordId());

        Iterable<MedicalRecordEvent> newEvents = produceEventsForCommand(snapshotBefore.getState(), command);

        medicalRecordEventStorePort.collectEvents(
            command.getRecordId(),
            command.getHospitalId(),
            command.getUserId(),
            snapshotBefore.getVersion() + 1,
            newEvents
        );

        newEvents.forEach(eventPublisher::publishEvent);

        MedicalRecordSnapshot snapshotAfter = applyEvents(snapshotBefore, newEvents);

        if (shouldCreateSnapshot(snapshotAfter)) {
            saveMedicalRecordSnapshotPort.SaveMedicalRecordSnapshot(snapshotAfter);
        }
    }

    private boolean shouldCreateSnapshot(MedicalRecordSnapshot snapshot) {
        return snapshot.getVersion() % SNAPSHOT_INTERVAL == 0;
    }
}
