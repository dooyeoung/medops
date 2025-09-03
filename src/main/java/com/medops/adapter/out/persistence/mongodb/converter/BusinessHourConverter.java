package com.medops.adapter.out.persistence.mongodb.converter;

import com.medops.adapter.out.persistence.mongodb.document.BusinessHourDocument;
import com.medops.application.port.out.LoadHospitalPort;
import com.medops.domain.model.BusinessHour;
import com.medops.domain.model.Hospital;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BusinessHourConverter {

    private final LoadHospitalPort loadHospitalPort;

    public BusinessHourDocument toDocument(BusinessHour businessHour){
        return new BusinessHourDocument(
            businessHour.getId(),
            businessHour.getHospital().getId(),
            businessHour.getDayOfWeek(),
            businessHour.getOpenTime(),
            businessHour.getCloseTime(),
            businessHour.getBreakStartTime(),
            businessHour.getBreakEndTime(),
            businessHour.isClosed()
        );
    }

    public BusinessHour toDomain(BusinessHourDocument businessHourDocument) {
        Hospital hospital = loadHospitalPort.loadHospitalById(
            businessHourDocument.getHospitalId()
        ).orElseThrow(IllegalAccessError::new);

        return BusinessHour.builder()
            .id(businessHourDocument.getId())
            .hospital(hospital)
            .dayOfWeek(businessHourDocument.getDayOfWeek())
            .openTime(businessHourDocument.getOpenTime())
            .closeTime(businessHourDocument.getCloseTime())
            .breakStartTime(businessHourDocument.getBreakStartTime())
            .breakEndTime(businessHourDocument.getBreakEndTime())
            .isClosed(businessHourDocument.isClosed())
            .build();
    }
}
