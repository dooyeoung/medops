package com.medops.application.port.in.command;

import java.math.BigDecimal;

public record UpdateTreatmentProductCommand(
    String hospitalId, String treatmentProductId, String name, String description, Integer maxCapacity, BigDecimal price
) {
}
