package com.medops.application.eventsourcing.processor;

import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.application.eventsourcing.handler.EventHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EventHandlerFactory {
    private final Map<Class<? extends MedicalRecordEvent>, EventHandler<?>> eventHandlers;

    public EventHandlerFactory(List<EventHandler<?>> handlers) {
        this.eventHandlers = handlers.stream()
                .collect(Collectors.toMap(
                        handler -> (Class<? extends MedicalRecordEvent>) handler.getEventType(),
                        Function.identity()
                ));
    }

    public <T extends MedicalRecordEvent> EventHandler<T> getHandler(Class<T> eventType) {
        EventHandler<?> handler = eventHandlers.get(eventType);
        if (handler == null) {
            throw new IllegalArgumentException("No EventHandler found for event type: " + eventType.getName());
        }
        return (EventHandler<T>) handler;
    }
}
