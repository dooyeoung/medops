package com.medops.application.port.out;

import com.medops.domain.model.Hospital;

public interface SaveHospitalPort {
    Hospital saveHospital(Hospital hospital);
}
