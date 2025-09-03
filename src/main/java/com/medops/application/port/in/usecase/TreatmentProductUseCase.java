package com.medops.application.port.in.usecase;


import com.medops.application.port.in.command.CreateTreatmentProductCommand;
import com.medops.application.port.in.command.DeleteTreatmentProductCommand;
import com.medops.application.port.in.command.RecoverTreatmentProductCommand;
import com.medops.application.port.in.command.UpdateTreatmentProductCommand;
import com.medops.domain.model.TreatmentProduct;

import java.util.List;

public interface TreatmentProductUseCase {
    void initializeTreatmentProducts(String hospitalId);
    TreatmentProduct createTreatmentProduct(CreateTreatmentProductCommand command);
    List<TreatmentProduct> getTreatmentProductsByHospitalId(String hospitalId);
    void updateTreatmentProduct(UpdateTreatmentProductCommand command);
    void deleteTreatmentProduct(DeleteTreatmentProductCommand command);
    void recoverTreatmentProduct(RecoverTreatmentProductCommand command);
}
