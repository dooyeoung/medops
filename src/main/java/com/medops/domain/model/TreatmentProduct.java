package com.medops.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TreatmentProduct {
    private String id;
    private String hospitalId;
    private String name;
    private String description;
    private Integer maxCapacity;
    private BigDecimal price;
    private Instant deletedAt;
    private Instant createdAt;

    public static TreatmentProduct createConsultation(String hospitalId, Integer maxCapacity){
        return TreatmentProduct.builder()
            .id(UUID.randomUUID().toString())
            .hospitalId(hospitalId)
            .name("상담")
            .description("일반 상담")
            .maxCapacity(maxCapacity)
            .price(BigDecimal.valueOf(5000))
            .createdAt(Instant.now())
            .build();
    }

    public static TreatmentProduct createRegularCheckup(String hospitalId, Integer maxCapacity){
        return TreatmentProduct.builder()
            .id(UUID.randomUUID().toString())
            .hospitalId(hospitalId)
            .name("정기 검진")
            .description("정기 검진")
            .maxCapacity(maxCapacity)
            .price(BigDecimal.valueOf(10000))
            .createdAt(Instant.now())
            .build();
    }
}
