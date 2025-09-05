package com.medops.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class BusinessHour {
    private String id;
    private Hospital hospital;
    private DayOfWeek dayOfWeek;
    private String openTime;
    private String closeTime;
    private String breakStartTime;
    private String breakEndTime;
    private boolean isClosed;
}
