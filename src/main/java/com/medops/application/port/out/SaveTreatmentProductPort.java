package com.medops.application.port.out;

import com.medops.domain.model.TreatmentProduct;

public interface SaveTreatmentProductPort {
    TreatmentProduct saveTreatmentProduct(TreatmentProduct treatmentProduct);
    void deleteTreatmentProduct(String treatmentProductId);
    void recoverTreatmentProduct(String treatmentProductId);
}
