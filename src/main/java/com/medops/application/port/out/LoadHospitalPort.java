package com.medops.application.port.out;

import com.medops.domain.model.Hospital;

import java.util.List;
import java.util.Optional;


public interface LoadHospitalPort {
    Optional<Hospital> loadHospitalById(String id);
    Optional<Hospital> loadHospitalByName(String name);
    List<Hospital> loadAllHospitals();
    boolean existsByName(String Name);
}
