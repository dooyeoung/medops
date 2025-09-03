package com.medops.adapter.out.persistence.mongodb.converter;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordSnapshotDocument;
import com.medops.adapter.out.persistence.mongodb.document.ReservationDocumentComponent;
import com.medops.domain.model.MedicalRecord;
import com.medops.domain.model.MedicalRecordSnapshot;
import com.medops.domain.model.Reservation;
import org.springframework.stereotype.Component;

@Component
public class MedicalRecordSnapshotConverter {
    public MedicalRecordSnapshot toDomain(MedicalRecordSnapshotDocument document) {

        ReservationDocumentComponent reservationComponent = document.getReservation();

        MedicalRecord medicalRecordState = MedicalRecord.builder()
            .userId(document.getUserId())
            .hospitalId(document.getHospitalId())
            .doctor(document.getDoctor())
            .note(document.getNote())
            .status(document.getStatus())
            .reservation(
                Reservation.builder()
                    .userMemo(reservationComponent.getUserMemo())
                    .startTime(reservationComponent.getStartTime())
                    .endTime(reservationComponent.getEndTime())
                    .treatmentProductId(reservationComponent.getTreatmentProductId())
                    .build()
            )
            .build();

        return MedicalRecordSnapshot.builder()
            .id(document.getId())
            .recordId(document.getRecordId())
            .createdAt(document.getCreatedAt())
            .version(document.getVersion())
            .state(medicalRecordState)
            .build();
    }

    public MedicalRecordSnapshotDocument toDocument(MedicalRecordSnapshot snapshot) {

        MedicalRecord state = snapshot.getState();
        Reservation reservation = state.getReservation();

        return new MedicalRecordSnapshotDocument(
            snapshot.getId(),
            snapshot.getRecordId(),
            snapshot.getCreatedAt(),
            snapshot.getVersion(),
            state.getUserId(),
            state.getHospitalId(),
            state.getDoctor(),
            state.getNote(),
            state.getStatus(),
            ReservationDocumentComponent
                .builder()
                .userMemo(reservation.getUserMemo())
                .startTime(reservation.getStartTime())
                .endTime(reservation.getEndTime())
                .treatmentProductId(reservation.getTreatmentProductId())
                .build()
        );
    }
}