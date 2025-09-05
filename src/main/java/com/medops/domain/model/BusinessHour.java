package com.medops.domain.model;

import com.medops.common.exception.BusinessHourInvalidation;
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
    private String openTime;
    private String closeTime;
    private String breakStartTime;
    private String breakEndTime;
    private boolean isClosed;

    public static void validateTimes(String openTime, String closeTime, String breakStartTime, String breakEndTime, Boolean closed){
        if (!closed) {
            if (!LocalTime.parse(openTime).isBefore(LocalTime.parse(closeTime))) {
                throw new BusinessHourInvalidation("시작시간은 종료시간보다 늦을 수 없습니다.");
            }

            if (!LocalTime.parse(openTime).isBefore(LocalTime.parse(breakStartTime))) {
                throw new BusinessHourInvalidation("시작시간은 쉬는시간 시작시간보다 늦을 수 없습니다");
            }

            if (!LocalTime.parse(breakStartTime).isBefore(LocalTime.parse(breakEndTime))) {
                throw new BusinessHourInvalidation("쉬는시간 시작시간은 쉬는시간 종료시간보다 늦을 수 없습니다");
            }
        }
    }
}
