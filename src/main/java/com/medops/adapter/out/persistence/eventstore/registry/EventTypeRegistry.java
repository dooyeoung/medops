package com.medops.adapter.out.persistence.eventstore.registry;

import com.medops.application.eventsourcing.event.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EventTypeRegistry {
    private static final Map<String, Class<? extends MedicalRecordEvent>> EVENT_TYPE_MAP = Map.of(
        Canceled.class.getSimpleName(), Canceled.class,
        Completed.class.getSimpleName(), Completed.class,
        Confirmed.class.getSimpleName(), Confirmed.class,
        DoctorAssigned.class.getSimpleName(), DoctorAssigned.class,
        NoteUpdated.class.getSimpleName(), NoteUpdated.class,
        Pending.class.getSimpleName(), Pending.class,
        ReservationCreated.class.getSimpleName(), ReservationCreated.class
    );

    public Class<? extends MedicalRecordEvent> getEventClass(String eventType) {
        Class<? extends MedicalRecordEvent> eventClass = EVENT_TYPE_MAP.get(eventType);
        if (eventClass == null) {
            throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
        return eventClass;
    }
}