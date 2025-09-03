package com.medops.application.port.out;

import com.medops.domain.model.TreatmentProduct;

import java.util.List;

public interface LoadTreatmentProductPort {
    List<TreatmentProduct> loadTreatmentProductsByHospitalId(String hospitalId);
    List<TreatmentProduct> loadTreatmentProductsByHospitalIds(List<String> hospitalIds);
}
