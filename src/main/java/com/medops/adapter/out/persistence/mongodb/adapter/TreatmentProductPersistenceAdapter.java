package com.medops.adapter.out.persistence.mongodb.adapter;

import com.medops.adapter.out.persistence.mongodb.converter.TreatmentProductConverter;
import com.medops.adapter.out.persistence.mongodb.document.TreatmentProductDocument;
import com.medops.adapter.out.persistence.mongodb.repository.TreatmentProductDocumentRepository;
import com.medops.application.port.out.LoadTreatmentProductPort;
import com.medops.application.port.out.SaveTreatmentProductPort;
import com.medops.domain.model.TreatmentProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TreatmentProductPersistenceAdapter implements LoadTreatmentProductPort, SaveTreatmentProductPort {
    private final TreatmentProductDocumentRepository treatmentProductDocumentRepository;
    private final TreatmentProductConverter treatmentProductConverter;

    @Override
    public TreatmentProduct saveTreatmentProduct(TreatmentProduct treatmentProduct) {
        TreatmentProductDocument savedDocument = treatmentProductDocumentRepository.save(
            treatmentProductConverter.toDocument(treatmentProduct)
        );
        return treatmentProductConverter.toDomain(savedDocument);
    }

    @Override
    public void deleteTreatmentProduct(String treatmentProductId) {
        TreatmentProductDocument treatmentProduct = treatmentProductDocumentRepository.findById(treatmentProductId).orElseThrow();
        treatmentProduct.setDeletedAt(Instant.now());
        treatmentProductDocumentRepository.save(treatmentProduct);
    }

    @Override
    public void recoverTreatmentProduct(String treatmentProductId) {
        TreatmentProductDocument treatmentProduct = treatmentProductDocumentRepository.findById(treatmentProductId).orElseThrow();
        treatmentProduct.setDeletedAt(null);
        treatmentProductDocumentRepository.save(treatmentProduct);
    }

    @Override
    public List<TreatmentProduct> loadTreatmentProductsByHospitalId(String hospitalId) {
        return treatmentProductDocumentRepository.findAllByHospitalId(hospitalId).stream().map(treatmentProductConverter::toDomain).toList();
    }

    @Override
    public List<TreatmentProduct> loadTreatmentProductsByHospitalIds(List<String> hospitalIds) {
        return treatmentProductDocumentRepository.findByHospitalIdIn(hospitalIds).stream().map(treatmentProductConverter::toDomain).toList();
    }
}
