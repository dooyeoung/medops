package com.medops.domain.model;

import com.medops.domain.enums.MedicalRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class MedicalRecord {
    private final String id;

    private final String userId;
    private final String hospitalId;
    private final String doctor;
    private final String note;

    private final MedicalRecordStatus status;
    private final Reservation reservation;

    // 과거부터의 이벤트(예약 등등) 내역
    @Builder.Default
    private List<Object> events = new ArrayList<>();

    public static MedicalRecord seedFactory(String id) {
        return MedicalRecord.builder()
            .id(id)
            .reservation(new Reservation())
            .status(MedicalRecordStatus.PENDING)
            .build();
    }
}
