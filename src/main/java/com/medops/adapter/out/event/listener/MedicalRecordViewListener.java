package com.medops.adapter.out.event.listener;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import com.medops.adapter.out.persistence.mongodb.repository.*;
import com.medops.application.eventsourcing.event.*;
import com.medops.application.service.NotificationEventService;
import com.medops.common.exception.NotFoundResource;
import com.medops.domain.enums.MedicalRecordStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicalRecordViewListener {

    private final MedicalRecordViewDocumentRepository viewRepository;
    private final UserDocumentRepository userDocumentRepository;
    private final TreatmentProductDocumentRepository treatmentProductDocumentRepository;
    private final HospitalDocumentRepository hospitalDocumentRepository;
    private final DoctorDocumentRepository doctorDocumentRepository;
    private final NotificationEventService notificationEventService;

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

        // SSE 새 예약 알림 발송
        try {
            notificationEventService.publishNewReservationFromViewDoc(viewDocument);
            log.info("새 예약 SSE 알림 발송 완료: recordId={}, hospitalId={}", 
                    event.getRecordId(), event.getHospitalId());
        } catch (Exception e) {
            log.error("새 예약 SSE 알림 발송 실패: recordId={}", event.getRecordId(), e);
        }
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

            // SSE 예약 확정 알림 발송
            try {
                notificationEventService.publishReservationUpdateFromViewDoc(updatedDoc);
                log.info("예약 확정 SSE 알림 발송 완료: recordId={}", event.getRecordId());
            } catch (Exception e) {
                log.error("예약 확정 SSE 알림 발송 실패: recordId={}", event.getRecordId(), e);
            }
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

            // SSE 예약 대기 상태 변경 알림 발송
            try {
                notificationEventService.publishReservationUpdateFromViewDoc(updatedDoc);
                log.info("예약 대기 상태 변경 SSE 알림 발송 완료: recordId={}", event.getRecordId());
            } catch (Exception e) {
                log.error("예약 대기 상태 변경 SSE 알림 발송 실패: recordId={}", event.getRecordId(), e);
            }
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

            // SSE 예약 취소 알림 발송
            try {
                notificationEventService.publishReservationUpdateFromViewDoc(updatedDoc);
                log.info("예약 취소 SSE 알림 발송 완료: recordId={}", event.getRecordId());
            } catch (Exception e) {
                log.error("예약 취소 SSE 알림 발송 실패: recordId={}", event.getRecordId(), e);
            }
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

            // SSE 예약 완료 알림 발송
            try {
                notificationEventService.publishReservationUpdateFromViewDoc(updatedDoc);
                log.info("예약 완료 SSE 알림 발송 완료: recordId={}", event.getRecordId());
            } catch (Exception e) {
                log.error("예약 완료 SSE 알림 발송 실패: recordId={}", event.getRecordId(), e);
            }
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

            // SSE 담당의사 배정 알림 발송
            try {
                notificationEventService.publishDoctorAssignmentFromViewDoc(updatedDoc);
                log.info("담당의사 배정 SSE 알림 발송 완료: recordId={}, doctorName={}", event.getRecordId(), doctorName);
            } catch (Exception e) {
                log.error("담당의사 배정 SSE 알림 발송 실패: recordId={}", event.getRecordId(), e);
            }
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

            // SSE 노트 업데이트 알림 발송
            try {
                notificationEventService.publishReservationUpdateFromViewDoc(updatedDoc);
                log.info("노트 업데이트 SSE 알림 발송 완료: recordId={}", event.getRecordId());
            } catch (Exception e) {
                log.error("노트 업데이트 SSE 알림 발송 실패: recordId={}", event.getRecordId(), e);
            }
        });
    }
}
