package com.medops.application.port.in.usecase;

import com.medops.adapter.in.web.request.HospitalCreateRequest;
import com.medops.application.dto.HospitalWithProductsDto;
import com.medops.domain.model.Hospital;

import java.util.List;
import java.util.Optional;

public interface HospitalUseCase {
    Hospital createHospital(HospitalCreateRequest request);
    List<HospitalWithProductsDto> getAllHospitals();
    Optional<Hospital> getHospitalById(String id);
    Optional<Hospital> getHospitalByName(String name);
}
