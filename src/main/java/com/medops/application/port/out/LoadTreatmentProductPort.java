package com.medops.application.port.out;

import com.medops.domain.model.TreatmentProduct;

import java.util.List;
import java.util.Optional;

public interface LoadTreatmentProductPort {
    Optional<TreatmentProduct> loadTreatmentProductById(String treatmentProductId);
    List<TreatmentProduct> loadTreatmentProductsByHospitalId(String hospitalId);
    List<TreatmentProduct> loadTreatmentProductsByHospitalIds(List<String> hospitalIds);
}
