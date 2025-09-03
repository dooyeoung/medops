package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.converter.DoctorConverter;
import com.medops.adapter.out.persistence.mongodb.document.DoctorDocument;
import com.medops.adapter.out.persistence.mongodb.repository.DoctorDocumentRepository;
import com.medops.application.port.out.LoadDoctorPort;
import com.medops.application.port.out.SaveDoctorPort;
import com.medops.domain.model.Doctor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DoctorPersistenceAdapter implements LoadDoctorPort, SaveDoctorPort {

    private final DoctorDocumentRepository doctorDocumentRepository;
    private final DoctorConverter doctorConverter;

    @Override
    public List<Doctor> loadDoctorsByHospitalId(String hospitalId) {
        return doctorDocumentRepository.findAllByHospitalId(hospitalId).stream().map(doctorConverter::toDomain).toList();
    }

    @Override
    public Optional<Doctor> loadDoctorById(String id) {
        return doctorDocumentRepository.findById(id).map(doctorConverter::toDomain);
    }

    @Override
    public Doctor saveDoctor(Doctor doctor) {
        DoctorDocument savedDocument = doctorDocumentRepository.save(
            doctorConverter.toDocument(doctor)
        );
        return doctorConverter.toDomain(savedDocument);
    }

    @Override
    public void deleteDoctor(String doctorId) {
        DoctorDocument doctor = doctorDocumentRepository.findById(doctorId).orElseThrow();
        doctor.setDeletedAt(Instant.now());
        doctorDocumentRepository.save(doctor);
    }

    @Override
    public void recoverDoctor(String doctorId) {
        DoctorDocument doctor = doctorDocumentRepository.findById(doctorId).orElseThrow();
        doctor.setDeletedAt(null);
        doctorDocumentRepository.save(doctor);
    }
}
