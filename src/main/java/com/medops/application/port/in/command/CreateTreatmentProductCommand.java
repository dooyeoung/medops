package com.medops.application.port.in.command;

import java.math.BigDecimal;

public record CreateTreatmentProductCommand(
    String hospitalId, String name, String description, Integer maxCapacity, BigDecimal price
) {
}
