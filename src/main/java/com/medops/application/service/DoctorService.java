package com.medops.application.service;

import com.medops.application.port.in.command.CreateDoctorCommand;
import com.medops.application.port.in.command.DeleteDoctorCommand;
import com.medops.application.port.in.command.RecoverDoctorCommand;
import com.medops.application.port.in.command.UpdateDoctorCommand;
import com.medops.application.port.in.usecase.DoctorUseCase;
import com.medops.application.port.out.LoadDoctorPort;
import com.medops.application.port.out.SaveDoctorPort;
import com.medops.common.exception.NotFoundResource;
import com.medops.domain.model.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService implements DoctorUseCase {
    private final LoadDoctorPort loadDoctorPort;
    private final SaveDoctorPort saveDoctorPort;


    @Override
    public List<Doctor> getDoctorsByHospitalId(String hospitalId) {
        return loadDoctorPort.loadDoctorsByHospitalId(hospitalId);
    }

    @Override
    public void createDoctor(CreateDoctorCommand command) {
        saveDoctorPort.saveDoctor(
            Doctor.builder()
                .id(UUID.randomUUID().toString())
                .name(command.name())
                .hospitalId(command.hospitalId())
                .createdAt(Instant.now())
                .deletedAt(null)
                .build()
        );
    }

    @Override
    public void updateDoctor(UpdateDoctorCommand command) {
        Doctor doctor = loadDoctorPort.loadDoctorById(command.id()).orElseThrow(
            () -> new NotFoundResource("소속 의사를 찾을수 없습니다")
        );
        saveDoctorPort.saveDoctor(
            doctor.toBuilder().name(command.name()).build()
        );
    }

    @Override
    public void deleteDoctor(DeleteDoctorCommand command) {
        saveDoctorPort.deleteDoctor(command.doctorId());
    }

    @Override
    public void recoverDoctor(RecoverDoctorCommand command) {
        saveDoctorPort.recoverDoctor(command.doctorId());
    }
}
