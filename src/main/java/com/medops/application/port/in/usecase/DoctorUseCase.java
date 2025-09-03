package com.medops.application.port.in.usecase;

import com.medops.application.port.in.command.CreateDoctorCommand;
import com.medops.application.port.in.command.DeleteDoctorCommand;
import com.medops.application.port.in.command.RecoverDoctorCommand;
import com.medops.application.port.in.command.UpdateDoctorCommand;
import com.medops.domain.model.Doctor;

import java.util.List;

public interface DoctorUseCase {

    List<Doctor> getDoctorsByHospitalId(String hospitalId);
    void createDoctor(CreateDoctorCommand command);
    void updateDoctor(UpdateDoctorCommand command);
    void deleteDoctor(DeleteDoctorCommand command);
    void recoverDoctor(RecoverDoctorCommand command);
}
