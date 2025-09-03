package com.medops.application.dto;

import com.medops.domain.model.TreatmentProduct;

import java.math.BigDecimal;

public record TreatmentProductDto(
    String id,
    String name,
    String description,
    BigDecimal price,
    Integer maxCapacity
) {
    public static TreatmentProductDto from(TreatmentProduct product) {
        return new TreatmentProductDto(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getMaxCapacity()
        );
    }
}