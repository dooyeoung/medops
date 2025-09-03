package com.medops.adapter.out.event.listener;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import com.medops.adapter.out.persistence.mongodb.repository.*;
import com.medops.application.eventsourcing.event.*;
import com.medops.common.exception.NotFoundResource;
import com.medops.domain.enums.MedicalRecordStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MedicalRecordViewListener {

    private final MedicalRecordViewDocumentRepository viewRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final TreatmentProductDocumentRepository treatmentProductDocumentRepository;
    private final HospitalDocumentRepository hospitalDocumentRepository;
    private final DoctorDocumentRepository doctorDocumentRepository;

    @EventListener
    public void on(ReservationCreated event) {
        String userName = userDocumentRepository.findById(event.getUserId()).orElseThrow().getName();
        String productName = treatmentProductDocumentRepository.findById(event.getTreatmentProductId()).orElseThrow().getName();
        String hospitalName = hospitalDocumentRepository.findById(event.getHospitalId()).orElseThrow().getName();

        MedicalRecordViewDocument viewDocument = new MedicalRecordViewDocument(
            event.getRecordId(),
            event.getUserId(),
            userName,
            event.getHospitalId(),
            hospitalName,
            null,
            null,
            event.getNote(),
            MedicalRecordStatus.PENDING,
            event.getStartTime(),
            event.getEndTime(),
            event.getTreatmentProductId(),
            productName,
            event.getUserMemo()
        );
        viewRepository.save(viewDocument);
    }

    @EventListener
    public void on(Confirmed event){
        viewRepository.findById(event.getRecordId()).ifPresent(viewDoc -> {
            MedicalRecordViewDocument updatedDoc = new MedicalRecordViewDocument(
                viewDoc.getId(),
                viewDoc.getUserId(),
                viewDoc.getUserName(),
                viewDoc.getHospitalId(),
                viewDoc.getHospitalName(),
                viewDoc.getDoctorId(),
                viewDoc.getDoctorName(),
                viewDoc.getNote(),
                MedicalRecordStatus.RESERVED,
                viewDoc.getStartTime(),
                viewDoc.getEndTime(),
                viewDoc.getTreatmentProductId(),
                viewDoc.getTreatmentProductName(),
                viewDoc.getUserMemo()
            );
            viewRepository.save(updatedDoc);
        });
    }

    @EventListener
    public void on(Pending event){
        viewRepository.findById(event.getRecordId()).ifPresent(viewDoc -> {
            MedicalRecordViewDocument updatedDoc = new MedicalRecordViewDocument(
                viewDoc.getId(),
                viewDoc.getUserId(),
                viewDoc.getUserName(),
                viewDoc.getHospitalId(),
                viewDoc.getHospitalName(),
                viewDoc.getDoctorId(),
                viewDoc.getDoctorName(),
                viewDoc.getNote(),
                MedicalRecordStatus.PENDING,
                viewDoc.getStartTime(),
                viewDoc.getEndTime(),
                viewDoc.getTreatmentProductId(),
                viewDoc.getTreatmentProductName(),
                viewDoc.getUserMemo()
            );
            viewRepository.save(updatedDoc);
        });
    }

    @EventListener
    public void on(Canceled event){
        viewRepository.findById(event.getRecordId()).ifPresent(viewDoc -> {
            MedicalRecordViewDocument updatedDoc = new MedicalRecordViewDocument(
                viewDoc.getId(),
                viewDoc.getUserId(),
                viewDoc.getUserName(),
                viewDoc.getHospitalId(),
                viewDoc.getHospitalName(),
                viewDoc.getDoctorId(),
                viewDoc.getDoctorName(),
                viewDoc.getNote(),
                MedicalRecordStatus.CANCELED,
                viewDoc.getStartTime(),
                viewDoc.getEndTime(),
                viewDoc.getTreatmentProductId(),
                viewDoc.getTreatmentProductName(),
                viewDoc.getUserMemo()
            );
            viewRepository.save(updatedDoc);
        });
    }

    @EventListener
    public void on(Completed event){
        viewRepository.findById(event.getRecordId()).ifPresent(viewDoc -> {
            MedicalRecordViewDocument updatedDoc = new MedicalRecordViewDocument(
                viewDoc.getId(),
                viewDoc.getUserId(),
                viewDoc.getUserName(),
                viewDoc.getHospitalId(),
                viewDoc.getHospitalName(),
                viewDoc.getDoctorId(),
                viewDoc.getDoctorName(),
                viewDoc.getNote(),
                MedicalRecordStatus.COMPLETED,
                viewDoc.getStartTime(),
                viewDoc.getEndTime(),
                viewDoc.getTreatmentProductId(),
                viewDoc.getTreatmentProductName(),
                viewDoc.getUserMemo()
            );
            viewRepository.save(updatedDoc);
        });
    }

    @EventListener
    public void on(DoctorAssigned event) {

        String doctorName = doctorDocumentRepository.findById(event.getDoctorId()).orElseThrow(() -> new NotFoundResource("담당의사 정보를 찾을수 없습니다.")).getName();

        viewRepository.findById(event.getRecordId()).ifPresent(viewDoc -> {
            // 2. 의사 이름과 상태를 업데이트한 새로운 객체를 만듭니다. (불변성 유지)
            MedicalRecordViewDocument updatedDoc = new MedicalRecordViewDocument(
                viewDoc.getId(),
                viewDoc.getUserId(),
                viewDoc.getUserName(),
                viewDoc.getHospitalId(),
                viewDoc.getHospitalName(),
                event.getDoctorId(),
                doctorName,
                viewDoc.getNote(),
                viewDoc.getStatus(),
                viewDoc.getStartTime(),
                viewDoc.getEndTime(),
                viewDoc.getTreatmentProductId(),
                viewDoc.getTreatmentProductName(),
                viewDoc.getUserMemo()
            );
            viewRepository.save(updatedDoc);
        });
    }

    @EventListener
    public void on(NoteUpdated event) {
        viewRepository.findById(event.getRecordId()).ifPresent(viewDoc -> {
            MedicalRecordViewDocument updatedDoc = new MedicalRecordViewDocument(
                viewDoc.getId(),
                viewDoc.getUserId(),
                viewDoc.getUserName(),
                viewDoc.getHospitalId(),
                viewDoc.getHospitalName(),
                viewDoc.getDoctorId(),
                viewDoc.getDoctorName(),
                event.getNote(), // 노트 업데이트
                viewDoc.getStatus(),
                viewDoc.getStartTime(),
                viewDoc.getEndTime(),
                viewDoc.getTreatmentProductId(),
                viewDoc.getTreatmentProductName(),
                viewDoc.getUserMemo()
            );
            viewRepository.save(updatedDoc);
        });
    }
}
