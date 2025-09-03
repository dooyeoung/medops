package com.medops.adapter.in.web.controller;


import com.medops.adapter.in.web.request.CreateDoctorRequest;
import com.medops.adapter.in.web.request.UpdateDoctorRequest;
import com.medops.application.port.in.command.CreateDoctorCommand;
import com.medops.application.port.in.command.DeleteDoctorCommand;
import com.medops.application.port.in.command.RecoverDoctorCommand;
import com.medops.application.port.in.command.UpdateDoctorCommand;
import com.medops.application.port.in.usecase.DoctorUseCase;
import com.medops.common.response.Api;
import com.medops.domain.model.Doctor;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/doctor")
public class DoctorApiController implements DoctorApiControllerSpec {
    private final DoctorUseCase doctorUseCase;

    @GetMapping("/hospitals/{hospitalId}")
    public Api<List<Doctor>> getDoctorsByHospitalId(
        @PathVariable String hospitalId
    ){
        return Api.OK(doctorUseCase.getDoctorsByHospitalId(hospitalId));
    }

    @PostMapping()
    public Api<Null> createDoctor(
        @RequestBody CreateDoctorRequest request
    ){
        doctorUseCase.createDoctor(
            new CreateDoctorCommand(
                request.hospitalId(),
                request.name()
            )
        );
        return Api.OK(null);
    }

    @PutMapping("/{doctorId}")
    public Api<Null> updateDoctor(
        @PathVariable String doctorId,
        @RequestBody UpdateDoctorRequest request
    ){
        doctorUseCase.updateDoctor(
            new UpdateDoctorCommand(doctorId, request.name())
        );
        return Api.OK(null);
    }

    @DeleteMapping("/{doctorId}")
    public Api<Null> deleteDoctor(
        @PathVariable String doctorId
    ){
        doctorUseCase.deleteDoctor(
            new DeleteDoctorCommand(doctorId)
        );
        return Api.OK(null);
    }

    @PatchMapping("/{doctorId}/recover")
    public Api<Null> recoverDoctor(
        @PathVariable String doctorId
    ){
        doctorUseCase.recoverDoctor(
            new RecoverDoctorCommand(doctorId)
        );
        return Api.OK(null);
    }
}
