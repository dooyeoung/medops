package com.medops.application.service;

import com.medops.application.port.in.command.UpdateBusinessHourCommand;
import com.medops.application.port.in.usecase.BusinessHourUseCase;
import com.medops.application.port.out.LoadBusinessHourPort;
import com.medops.application.port.out.LoadHospitalPort;
import com.medops.application.port.out.SaveBusinessHourPort;
import com.medops.common.exception.NotFoundResource;
import com.medops.domain.model.BusinessHour;
import com.medops.domain.model.Hospital;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessHourService implements BusinessHourUseCase {
    private final SaveBusinessHourPort saveBusinessHourPort;
    private final LoadHospitalPort loadHospitalPort;
    private final LoadBusinessHourPort loadBusinessHourPort;

    @Override
    public List<BusinessHour> initializeBusinessHours(String HospitalId) {
        Hospital hospital = loadHospitalPort.loadHospitalById(HospitalId).orElseThrow();

        List<BusinessHour> businessHours = new ArrayList<BusinessHour>();

        EnumSet.allOf(DayOfWeek.class).forEach(dayOfWeek -> {
            boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            BusinessHour businessHour = BusinessHour.builder()
                .id(UUID.randomUUID().toString())
                .hospital(hospital)
                .dayOfWeek(dayOfWeek)
                .isClosed(isWeekend)
                .openTime(isWeekend? null: LocalTime.of(9, 0))
                .closeTime(isWeekend? null: LocalTime.of(18, 0))
                .breakStartTime(isWeekend? null: LocalTime.of(12, 0))
                .breakEndTime(isWeekend? null: LocalTime.of(13, 0))
                .build();

            saveBusinessHourPort.saveBusinessHour(businessHour);
        });
        return businessHours;
    }

    @Override
    public List<BusinessHour> getBusinessHoursByHospitalId(String hospitalId) {
        return loadBusinessHourPort.loadBusinessHoursByHospitalId(hospitalId);
    }

    @Override
    public void updateBusinessHour(UpdateBusinessHourCommand command) {
        BusinessHour businessHour = loadBusinessHourPort.loadBusinessHourById(command.businessHourId()).orElseThrow(
            () -> new NotFoundResource("영업시간 정보를 찾을수 없습니다.")
        );;
        saveBusinessHourPort.saveBusinessHour(
        businessHour.toBuilder()
            .openTime(command.openTime())
            .closeTime(command.closeTime())
            .breakEndTime(command.breakEndTime())
            .breakStartTime(command.breakStartTime())
            .isClosed(command.closed())
            .build()
        );
    }
}
