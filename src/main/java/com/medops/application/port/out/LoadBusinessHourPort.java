package com.medops.application.port.out;

import com.medops.domain.model.BusinessHour;

import java.util.List;
import java.util.Optional;

public interface LoadBusinessHourPort {
    List<BusinessHour> loadBusinessHoursByHospitalId(String HospitalId);
    Optional<BusinessHour> loadBusinessHourById(String businessHourId);
}
