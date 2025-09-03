package com.medops.application.port.out;

import com.medops.domain.model.Admin;

import java.util.List;
import java.util.Optional;

public interface LoadAdminPort {
    Optional<Admin> loadAdminById(String id);
    Optional<Admin> loadAdminByEmail(String email);
    List<Admin> loadAdminsByHospitalId(String hospitalId);
    boolean existsByEmail(String email);

}
