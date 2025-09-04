package com.medops.application.service;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import com.medops.domain.model.MedicalRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
            Map<String, Object> notificationData = createReservationNotificationDataFromViewDoc(viewDoc);
            
            sseEmitterService.sendToHospital(
                    viewDoc.getHospitalId(), 
                    "NEW_RESERVATION", 
                    notificationData
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
            Map<String, Object> notificationData = createReservationNotificationDataFromViewDoc(viewDoc);
            
            sseEmitterService.sendToHospital(
                    viewDoc.getHospitalId(), 
                    "RESERVATION_UPDATE", 
                    notificationData
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
            Map<String, Object> notificationData = createReservationNotificationDataFromViewDoc(viewDoc);
            
            sseEmitterService.sendToHospital(
                    viewDoc.getHospitalId(), 
                    "DOCTOR_ASSIGN", 
                    notificationData
            );
        } catch (Exception e) {
            log.error("담당의사 배정 알림 발송 실패: recordId={}", viewDoc.getId(), e);
        }
    }

    /**
     * 프론트엔드용 알림 데이터 생성 (ViewDocument)
     */
    private Map<String, Object> createReservationNotificationDataFromViewDoc(MedicalRecordViewDocument viewDoc) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("id", viewDoc.getId());
        data.put("hospitalId", viewDoc.getHospitalId());
        data.put("userId", viewDoc.getUserId());
        data.put("userName", viewDoc.getUserName());
        data.put("doctorId", viewDoc.getDoctorId());
        data.put("doctorName", viewDoc.getDoctorName());
        data.put("treatmentProductId", viewDoc.getTreatmentProductId());
        data.put("treatmentProductName", viewDoc.getTreatmentProductName());
        data.put("startTime", viewDoc.getStartTime());
        data.put("endTime", viewDoc.getEndTime());
        data.put("status", viewDoc.getStatus().name());
        data.put("memo", viewDoc.getUserMemo()); // ViewDoc에서는 userMemo
        data.put("note", viewDoc.getNote());
        
        return data;
    }
}