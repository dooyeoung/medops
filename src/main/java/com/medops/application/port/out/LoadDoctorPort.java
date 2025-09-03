package com.medops.application.port.out;

import com.medops.domain.model.Doctor;

import java.util.List;
import java.util.Optional;

public interface LoadDoctorPort {
    List<Doctor> loadDoctorsByHospitalId(String hospitalId);
    Optional<Doctor> loadDoctorById(String id);
}
