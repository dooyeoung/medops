package com.medops.application.port.out;

import com.medops.domain.model.Doctor;

public interface SaveDoctorPort {

    Doctor saveDoctor(Doctor doctor);
    void deleteDoctor(String doctorId);
    void recoverDoctor(String doctorId);
}
