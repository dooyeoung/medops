package com.medops.application.service;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationEventService {

    private final SseEmitterService sseEmitterService;


    /**
     * ViewDocument에서 새 예약 알림 발송
     */
    public void publishNewReservationFromViewDoc(MedicalRecordViewDocument viewDoc) {
        log.info("새 예약 알림 발송 (ViewDoc): recordId={}, hospitalId={}", 
                viewDoc.getId(), viewDoc.getHospitalId());

        try {
            sseEmitterService.sendToHospital(
                    viewDoc.getHospitalId(), 
                    "NEW_RESERVATION", 
                    viewDoc
            );
        } catch (Exception e) {
            log.error("새 예약 알림 발송 실패: recordId={}", viewDoc.getId(), e);
        }
    }

    /**
     * ViewDocument에서 예약 상태 변경 알림 발송
     */
    public void publishReservationUpdateFromViewDoc(MedicalRecordViewDocument viewDoc) {
        log.info("예약 상태 변경 알림 발송 (ViewDoc): recordId={}, status={}, hospitalId={}", 
                viewDoc.getId(), viewDoc.getStatus(), viewDoc.getHospitalId());

        try {
            sseEmitterService.sendToHospital(
                    viewDoc.getHospitalId(), 
                    "RESERVATION_UPDATE", 
                    viewDoc
            );
        } catch (Exception e) {
            log.error("예약 상태 변경 알림 발송 실패: recordId={}", viewDoc.getId(), e);
        }
    }

    /**
     * ViewDocument에서 담당의사 배정 알림 발송
     */
    public void publishDoctorAssignmentFromViewDoc(MedicalRecordViewDocument viewDoc) {
        log.info("담당의사 배정 알림 발송 (ViewDoc): recordId={}, doctorName={}, hospitalId={}", 
                viewDoc.getId(), viewDoc.getDoctorName(), viewDoc.getHospitalId());

        try {
            sseEmitterService.sendToHospital(
                    viewDoc.getHospitalId(), 
                    "DOCTOR_ASSIGN", 
                    viewDoc
            );
        } catch (Exception e) {
            log.error("담당의사 배정 알림 발송 실패: recordId={}", viewDoc.getId(), e);
        }
    }

}