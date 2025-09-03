package com.medops.application.eventsourcing.command.executor;

import com.medops.application.eventsourcing.event.MedicalRecordEvent;
import com.medops.domain.model.MedicalRecord;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;

public interface CommandExecutor<C> {
    Iterable<MedicalRecordEvent> produceEvents(MedicalRecord state, C command);

    default Class<?> getCommandType() {
        return Arrays
            .stream(getClass().getGenericInterfaces())
            .map(i -> (ParameterizedType) i)
            .filter(p -> p.getRawType().equals(CommandExecutor.class))
            .map(p -> p.getActualTypeArguments()[0])
            .map(a -> (Class<?>) a)
            .findFirst()
            .get();
    }
}