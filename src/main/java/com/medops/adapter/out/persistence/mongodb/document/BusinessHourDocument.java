package com.medops.adapter.out.persistence.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DayOfWeek;

@Getter
@AllArgsConstructor
@Document("medops_business_hour")
public class BusinessHourDocument {
    @Id
    private String id;
    private String hospitalId;

    private DayOfWeek dayOfWeek;
    private String openTime;
    private String closeTime;
    private String breakStartTime;
    private String breakEndTime;
    private boolean isClosed;
}
