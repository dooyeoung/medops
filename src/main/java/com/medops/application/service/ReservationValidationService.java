package com.medops.application.service;

import com.medops.application.port.in.usecase.ReservationValidationUseCase;
import com.medops.application.port.out.LoadMedicalRecordViewPort;
import com.medops.application.port.out.LoadTreatmentProductPort;
import com.medops.domain.model.TreatmentProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
@RequiredArgsConstructor
public class ReservationValidationService implements ReservationValidationUseCase {
    private final LoadTreatmentProductPort loadTreatmentProductPort;
    private final LoadMedicalRecordViewPort medicalRecordViewPort;

    @Override
    public boolean isReservationAvailable(String hospitalId, String treatmentProductId, Instant startTime, Instant endTime) {
        TreatmentProduct treatmentProduct = loadTreatmentProductPort.loadTreatmentProductById(treatmentProductId).orElseThrow();
        return treatmentProduct.getMaxCapacity() <= medicalRecordViewPort.loadMedicalRecordsByTreatmentIdInRange(treatmentProductId, startTime, endTime).size();
    }
}
