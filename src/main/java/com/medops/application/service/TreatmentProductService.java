package com.medops.application.service;

import com.medops.application.port.in.command.DeleteTreatmentProductCommand;
import com.medops.application.port.in.command.RecoverTreatmentProductCommand;
import com.medops.application.port.in.usecase.TreatmentProductUseCase;
import com.medops.application.port.in.command.CreateTreatmentProductCommand;
import com.medops.application.port.in.command.UpdateTreatmentProductCommand;
import com.medops.application.port.out.LoadTreatmentProductPort;
import com.medops.application.port.out.SaveTreatmentProductPort;
import com.medops.domain.model.TreatmentProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TreatmentProductService implements TreatmentProductUseCase {
    private final SaveTreatmentProductPort saveTreatmentProductPort;
    private final LoadTreatmentProductPort loadTreatmentProductPort;

    @Override
    public TreatmentProduct createTreatmentProduct(CreateTreatmentProductCommand command) {
        return saveTreatmentProductPort.saveTreatmentProduct(
            TreatmentProduct.builder()
                .id(UUID.randomUUID().toString())
                .hospitalId(command.hospitalId())
                .name(command.name())
                .description(command.description())
                .maxCapacity(command.maxCapacity())
                .price(command.price())
                .createdAt(Instant.now())
                .deletedAt(null)
                .build()
        );
    }

    @Override
    public void initializeTreatmentProducts(String hospitalId) {
        saveTreatmentProductPort.saveTreatmentProduct(
            TreatmentProduct.createConsultation(hospitalId, 3)
        );
        saveTreatmentProductPort.saveTreatmentProduct(
            TreatmentProduct.createRegularCheckup(hospitalId, 1)
        );
    }

    @Override
    public List<TreatmentProduct> getTreatmentProductsByHospitalId(String hospitalId){
        return loadTreatmentProductPort.loadTreatmentProductsByHospitalId(hospitalId);
    }

    @Override
    public void updateTreatmentProduct(UpdateTreatmentProductCommand command) {
        saveTreatmentProductPort.saveTreatmentProduct(
            TreatmentProduct.builder()
            .id(command.treatmentProductId())
            .name(command.name())
            .description(command.description())
            .maxCapacity(command.maxCapacity())
            .price(command.price())
            .hospitalId(command.hospitalId())
            .build()
        );
    }

    @Override
    public void deleteTreatmentProduct(DeleteTreatmentProductCommand command) {
        saveTreatmentProductPort.deleteTreatmentProduct(
            command.treatmentProductId()
        );
    }

    @Override
    public void recoverTreatmentProduct(RecoverTreatmentProductCommand command) {
        saveTreatmentProductPort.recoverTreatmentProduct(
            command.treatmentProductId()
        );
    }
}
