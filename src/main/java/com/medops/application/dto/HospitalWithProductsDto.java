package com.medops.application.dto;

import com.medops.domain.model.Hospital;
import com.medops.domain.model.TreatmentProduct;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record HospitalWithProductsDto(
    String id,
    String name,
    String address,
    Instant createdAt,
    List<TreatmentProductDto> treatmentProducts
) {
    public static HospitalWithProductsDto from(Hospital hospital, List<TreatmentProduct>
        products) {
        return new HospitalWithProductsDto(
            hospital.getId(),
            hospital.getName(),
            hospital.getAddress(),
            hospital.getCreatedAt(),
            products.stream()
                .map(TreatmentProductDto::from)
                .collect(Collectors.toList())
        );
    }
}