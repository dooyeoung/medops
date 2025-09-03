package com.medops.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class BusinessHour {
    private String id;
    private Hospital hospital;
    private DayOfWeek dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private LocalTime breakStartTime;
    private LocalTime breakEndTime;
    private boolean isClosed;
}
