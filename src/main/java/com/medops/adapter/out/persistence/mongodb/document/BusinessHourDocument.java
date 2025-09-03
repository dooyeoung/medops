package com.medops.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@Document("medops_business_hour")
public class BusinessHourDocument {
    @Id
    private String id;
    private String hospitalId;

    private DayOfWeek dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private boolean isClosed;
}
