package com.medops.adapter.out.persistence.eventstore.adapter;

import com.medops.adapter.out.persistence.eventstore.registry.EventTypeRegistry;
import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordEventDocument;
import com.medops.adapter.out.persistence.mongodb.repository.MedicalRecordEventDocumentRepository;
import com.medops.application.port.out.MedicalRecordEventStorePort;
import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MedicalRecordEventStoreAdapter  implements MedicalRecordEventStorePort {

    private final MedicalRecordEventDocumentRepository repository;
    private final ObjectMapper objectMapper;
    private final EventTypeRegistry eventTypeRegistry;

    public void collectEvents(
        String recordId,
        String hospitalId,
        String userId,
        Integer version,
        Iterable<MedicalRecordEvent> events
    ) {
        List<MedicalRecordEventDocument> documentsToSave = new ArrayList<>();
        Instant now = Instant.now();

        for (MedicalRecordEvent event : events) {
            Map<String, Object> payload = objectMapper.convertValue(event, Map.class);

            MedicalRecordEventDocument document = new MedicalRecordEventDocument(
                UUID.randomUUID().toString(),
                recordId,
                now,
                event.getClass().getSimpleName(),
                hospitalId,
                userId,
                version,
                event.getStatus(),
                payload
            );
            documentsToSave.add(document);
            version++;
        }
        repository.saveAll(documentsToSave);
        documentsToSave.forEach(doc -> System.out.println("  - " + doc.getEventType() + " (version: " + doc.getVersion() + ")"));
    }

    public List<Object> queryEvents(String recordId, Integer fromVersion) {
        List<MedicalRecordEventDocument> documents = repository.findAllByRecordIdAndVersionGreaterThanEqualOrderByVersionAsc(recordId, fromVersion);

        List<Object> events = documents.stream()
            .map(document -> {
                try {
                    Class<?> eventClass = eventTypeRegistry.getEventClass(document.getEventType());
                    return objectMapper.convertValue(document.getPayload(), eventClass);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            })
            .filter(java.util.Objects::nonNull)
            .collect(Collectors.toList());

        System.out.println("Queried " + events.size() + " events from MongoDB for streamId: " + recordId + " from version " + fromVersion);
        return events;
    }
}