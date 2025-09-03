package com.medops.adapter.in.web.request;

import java.math.BigDecimal;

public record UpdateTreatmentProductRequest(
    String hospitalId, String name, String description, Integer maxCapacity, BigDecimal price
) {
}
