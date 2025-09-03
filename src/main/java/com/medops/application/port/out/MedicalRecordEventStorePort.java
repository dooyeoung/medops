package com.medops.application.port.out;

import com.medops.application.eventsourcing.event.MedicalRecordEvent;

import java.util.List;

public interface MedicalRecordEventStorePort {
    void collectEvents(
        String recordId,
        String hospitalId,
        String userId,
        Integer version,
        Iterable<MedicalRecordEvent> events
    );
    List<Object> queryEvents(String recordId, Integer fromVersion);
}
