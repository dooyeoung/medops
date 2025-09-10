package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.annotation.AdminSession;
import com.medops.adapter.in.web.request.HospitalCreateRequest;
import com.medops.application.dto.HospitalWithProductsDto;
import com.medops.application.port.in.usecase.AdminUseCase;
import com.medops.application.port.in.usecase.HospitalUseCase;
import com.medops.application.port.in.usecase.TreatmentProductUseCase;
import com.medops.common.exception.NotFoundResource;
import com.medops.common.response.Api;
import com.medops.domain.model.Admin;
import com.medops.domain.model.Hospital;
import com.medops.domain.model.TreatmentProduct;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospital")
@RequiredArgsConstructor
public class HospitalApiController implements HospitalApiControllerSpec{

    private final HospitalUseCase hospitalUseCase;
    private final TreatmentProductUseCase treatmentProductUseCase;
    private final AdminUseCase adminUseCase;

    @Override
    @PostMapping("")
    public Api<Hospital> createHospital(
        @RequestBody
        HospitalCreateRequest request
    ) {
        return Api.OK(hospitalUseCase.createHospital(request));
    }

    @Override
    @GetMapping("")
    public Api<List<HospitalWithProductsDto>> getAllHospitals() {
        return Api.OK(hospitalUseCase.getAllHospitals());
    }

    @GetMapping("/{hospitalId}")
    public Api<Hospital> getHospital(
        @Parameter(hidden = true) @AdminSession Admin admin,
        @PathVariable String hospitalId
    ) {
        if (!admin.getHospital().getId().equals(hospitalId)) {
            throw new AccessDeniedException("자신의 병원 정보만 조회할 수 있습니다.");
        }

        Hospital hospital = hospitalUseCase.getHospitalById(hospitalId).orElseThrow(
            () -> new NotFoundResource("병원을 조회할 수 없습니다.")
        );
        return Api.OK(hospital);
    }

    @GetMapping("/{hospitalId}/admins")
    public Api<List<Admin>> getHospitalAdmins(
        @Parameter(hidden = true) @AdminSession Admin admin,
        @PathVariable String hospitalId
    ) {
        if (!admin.getHospital().getId().equals(hospitalId)) {
            throw new AccessDeniedException("자신의 병원 정보만 조회할 수 있습니다.");
        }
        return Api.OK(adminUseCase.getAdminsByHospitalId(hospitalId));
    }

    @Override
    @GetMapping("/{hospitalId}/treatment-products")
    public Api<List<TreatmentProduct>> getHospitalTreatmentProducts(@PathVariable String hospitalId) {
        return Api.OK(treatmentProductUseCase.getTreatmentProductsByHospitalId(hospitalId));
    }
}
