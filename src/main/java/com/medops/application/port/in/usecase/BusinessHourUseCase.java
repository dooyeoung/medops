package com.medops.application.port.in.usecase;

import com.medops.application.port.in.command.UpdateBusinessHourCommand;
import com.medops.domain.model.BusinessHour;

import java.util.List;

public interface BusinessHourUseCase {
    List<BusinessHour> initializeBusinessHours(String HospitalId);
    List<BusinessHour> getBusinessHoursByHospitalId(String hospitalId);
    void updateBusinessHour(UpdateBusinessHourCommand command);
}
