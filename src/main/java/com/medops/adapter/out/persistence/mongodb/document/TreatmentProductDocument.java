package com.medops.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@AllArgsConstructor
@Document("medops_treatment_product")
public class TreatmentProductDocument {
    @Id
    private String id;
    private String hospitalId;
    private String name;
    private String description;
    private Integer maxCapacity;
    private BigDecimal price;
    private Instant createdAt;
    @Setter
    private Instant deletedAt;
}
