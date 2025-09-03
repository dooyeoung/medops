package com.medops.adapter.in.web.controller;


import com.medops.adapter.in.web.request.UpdateBusinessHourRequest;
import com.medops.application.port.in.command.UpdateBusinessHourCommand;
import com.medops.application.port.in.usecase.BusinessHourUseCase;
import com.medops.common.response.Api;
import com.medops.domain.model.BusinessHour;
import jakarta.validation.constraints.Null;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/business-hours")
public class BusinessHourApiController implements BusinessHourApiControllerSpec {
    private final BusinessHourUseCase businessHourUseCase;

    @GetMapping("/hospital/{hospitalId}")
    public Api<List<BusinessHour>> getBusinessHours(
        @PathVariable String hospitalId
    ){
        List<BusinessHour> businessHours = businessHourUseCase.getBusinessHoursByHospitalId(hospitalId);
        return Api.OK(businessHours);
    }

    @PutMapping("/{businessHourId}")
    public Api<Null> updateBusinessHour(
        @PathVariable String businessHourId,
        @RequestBody UpdateBusinessHourRequest request
    ){
        businessHourUseCase.updateBusinessHour(
            new UpdateBusinessHourCommand(
                businessHourId,
                request.openTime(),
                request.closeTime(),
                request.breakStartTime(),
                request.breakEndTime(),
                request.closed()
            )
        );
        return Api.OK(null);
    }
}
