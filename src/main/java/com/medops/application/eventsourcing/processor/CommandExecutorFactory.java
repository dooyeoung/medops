package com.medops.application.eventsourcing.processor;

import com.medops.application.eventsourcing.command.StreamCommand;
import com.medops.application.eventsourcing.command.executor.CommandExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommandExecutorFactory {

    private final Map<Class<? extends StreamCommand>, CommandExecutor<?>> commandExecutors;

    public CommandExecutorFactory(List<CommandExecutor<?>> executors) {
        this.commandExecutors = executors.stream()
                .collect(Collectors.toMap(
                        executor -> (Class<? extends StreamCommand>) executor.getCommandType(),
                        Function.identity()
                ));
    }

    @SuppressWarnings("unchecked")
    public <T extends StreamCommand> CommandExecutor<T> getExecutor(Class<T> commandType) {
        CommandExecutor<?> executor = commandExecutors.get(commandType);
        if (executor == null) {
            throw new IllegalArgumentException("No CommandExecutor found for command type: " + commandType.getName());
        }
        return (CommandExecutor<T>) executor;
    }
}
