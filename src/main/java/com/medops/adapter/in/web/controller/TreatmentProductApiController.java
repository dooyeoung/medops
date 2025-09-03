package com.medops.adapter.in.web.controller;

import com.medops.adapter.in.web.request.CreateTreatmentProductRequest;
import com.medops.adapter.in.web.request.UpdateTreatmentProductRequest;
import com.medops.application.port.in.command.DeleteTreatmentProductCommand;
import com.medops.application.port.in.command.RecoverTreatmentProductCommand;
import com.medops.application.port.in.usecase.TreatmentProductUseCase;
import com.medops.application.port.in.command.CreateTreatmentProductCommand;
import com.medops.application.port.in.command.UpdateTreatmentProductCommand;
import com.medops.common.response.Api;
import com.medops.domain.model.TreatmentProduct;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/treatment-products")
public class TreatmentProductApiController implements TreatmentProductApiControllerSpec {
    private final TreatmentProductUseCase treatmentProductUseCase;

    @PostMapping("")
    public Api<TreatmentProduct> createTreatmentProduct(
        @RequestBody CreateTreatmentProductRequest request
    ){
        TreatmentProduct product = treatmentProductUseCase.createTreatmentProduct(
            new CreateTreatmentProductCommand(
                request.hospitalId(),
                request.name(),
                request.description(),
                request.maxCapacity(),
                request.price()
            )
        );
        return Api.OK(product);
    }

    @PutMapping("/{treatmentProductId}")
    public Api<Null> updateTreatmentProduct(
        @PathVariable String treatmentProductId,
        @RequestBody UpdateTreatmentProductRequest request
    ){
        treatmentProductUseCase.updateTreatmentProduct(
            new UpdateTreatmentProductCommand(
                request.hospitalId(),
                treatmentProductId,
                request.name(),
                request.description(),
                request.maxCapacity(),
                request.price()
            )
        );
        return Api.OK(null);
    }

    @DeleteMapping("/{treatmentProductId}")
    public Api<Null> deleteTreatmentProduct(
        @PathVariable String treatmentProductId
    ){
        treatmentProductUseCase.deleteTreatmentProduct(
            new DeleteTreatmentProductCommand(
                treatmentProductId
            )
        );
        return Api.OK(null);
    }

    @PatchMapping("/{treatmentProductId}/recover")
    public Api<Null> recoverTreatmentProduct(
        @PathVariable String treatmentProductId
    ){
        treatmentProductUseCase.recoverTreatmentProduct(
            new RecoverTreatmentProductCommand(
                treatmentProductId
            )
        );
        return Api.OK(null);
    }
}
