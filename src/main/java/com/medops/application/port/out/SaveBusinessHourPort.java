package com.medops.application.port.out;

import com.medops.domain.model.BusinessHour;

public interface SaveBusinessHourPort {
    BusinessHour saveBusinessHour(BusinessHour businessHour);
}
