package com.medops.application.service;

import com.medops.adapter.in.web.request.HospitalCreateRequest;
import com.medops.application.dto.HospitalWithProductsDto;
import com.medops.application.port.in.usecase.BusinessHourUseCase;
import com.medops.application.port.in.usecase.HospitalUseCase;
import com.medops.application.port.in.usecase.TreatmentProductUseCase;
import com.medops.application.port.out.*;
import com.medops.domain.model.Admin;
import com.medops.domain.model.Hospital;
import com.medops.domain.enums.AdminRole;
import com.medops.domain.model.TreatmentProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HospitalService implements HospitalUseCase {
    private final PasswordEncoder passwordEncoder;

    private final SaveHospitalPort saveHospitalPort;
    private final SaveAdminPort saveAdminPort;
    private final LoadHospitalPort loadHospitalPort;
    private final LoadAdminPort loadAdminPort;
    private final LoadTreatmentProductPort loadTreatmentProductPort;

    private final BusinessHourUseCase businessHourUseCase;
    private final TreatmentProductUseCase treatmentProductUseCase;

    @Override
    public Hospital createHospital(HospitalCreateRequest request) {
        if (loadHospitalPort.existsByName(request.name())){
            throw new IllegalArgumentException("병원 이름이 중복입니다");
        }
        if (loadAdminPort.existsByEmail(request.adminEmail())){
            throw new IllegalArgumentException("어드민 중복입니다");
        }

        Instant now = Instant.now();
        Hospital savedHospital = saveHospitalPort.saveHospital(
            Hospital.builder()
                .id(UUID.randomUUID().toString())
                .name(request.name())
                .address(request.address())
                .createdAt(now)
                .build()
        );

        saveAdminPort.saveAdmin(
            Admin.builder()
                .id(UUID.randomUUID().toString())
                .email(request.adminEmail())
                .name(request.adminName())
                .password(passwordEncoder.encode(request.adminPassword()))
                .role(AdminRole.ADMIN)
                .hospital(savedHospital)
                .createdAt(now)
                .deletedAt(null)
                .build()
        );

        // 기본 영업시간 생성
        businessHourUseCase.initializeBusinessHours(savedHospital.getId());

        // 기본 상품 등록
        treatmentProductUseCase.initializeTreatmentProducts(savedHospital.getId());

        return savedHospital;
    }

    @Override
    public List<HospitalWithProductsDto> getAllHospitals() {
        List<Hospital> hospitals = loadHospitalPort.loadAllHospitals();
        List<TreatmentProduct> treatmentProducts = loadTreatmentProductPort.loadTreatmentProductsByHospitalIds(
            hospitals.stream().map(Hospital::getId).toList()
        );

        Map<String, List<TreatmentProduct>> mapped = treatmentProducts.stream().collect(Collectors.groupingBy(TreatmentProduct::getHospitalId));

        return hospitals.stream().map(
            hospital -> {
                var hospitalTreatmentProducts = mapped.getOrDefault(hospital.getId(), Collections.emptyList());
                return HospitalWithProductsDto.from(hospital, hospitalTreatmentProducts);
            }
        ).toList();
    }

    @Override
    public Optional<Hospital> getHospitalByName(String name) {
        return loadHospitalPort.loadHospitalByName(name);
    }

    @Override
    public Optional<Hospital> getHospitalById(String id){
        return loadHospitalPort.loadHospitalById(id);
    }
}
