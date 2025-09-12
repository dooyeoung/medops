package com.medops.adapter.out.persistence.eventstore.registry;

import com.medops.application.eventsourcing.event.*;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EventTypeRegistry {
    private static final Map<String, Class<? extends MedicalRecordEvent>> EVENT_TYPE_MAP = Map.of(
        Canceled.class.getName(), Canceled.class,
        Completed.class.getName(), Completed.class,
        Confirmed.class.getName(), Confirmed.class,
        DoctorAssigned.class.getName(), DoctorAssigned.class,
        NoteUpdated.class.getName(), NoteUpdated.class,
        Pending.class.getName(), Pending.class,
        ReservationCreated.class.getName(), ReservationCreated.class
    );

    public Class<? extends MedicalRecordEvent> getEventClass(String eventType) {
        Class<? extends MedicalRecordEvent> eventClass = EVENT_TYPE_MAP.get(eventType);
        if (eventClass == null) {
            throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
        return eventClass;
    }
}