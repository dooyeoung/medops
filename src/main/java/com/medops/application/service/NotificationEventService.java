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
     * 새 예약 생성 알림 발송
     */
    public void publishNewReservation(MedicalRecord medicalRecord) {
        log.info("새 예약 알림 발송: medicalRecordId={}, hospitalId={}", 
                medicalRecord.getId(), medicalRecord.getHospitalId());

        try {
            // 프론트엔드에서 기대하는 형태로 데이터 구성
            Map<String, Object> notificationData = createReservationNotificationData(medicalRecord);
            
            sseEmitterService.sendToHospital(
                    medicalRecord.getHospitalId(), 
                    "NEW_RESERVATION", 
                    notificationData
            );
        } catch (Exception e) {
            log.error("새 예약 알림 발송 실패: medicalRecordId={}", medicalRecord.getId(), e);
        }
    }

    /**
     * 예약 상태 변경 알림 발송
     */
    public void publishReservationUpdate(MedicalRecord medicalRecord) {
        log.info("예약 상태 변경 알림 발송: medicalRecordId={}, status={}, hospitalId={}", 
                medicalRecord.getId(), medicalRecord.getStatus(), medicalRecord.getHospitalId());

        try {
            Map<String, Object> notificationData = createReservationNotificationData(medicalRecord);
            
            sseEmitterService.sendToHospital(
                    medicalRecord.getHospitalId(), 
                    "RESERVATION_UPDATE", 
                    notificationData
            );
        } catch (Exception e) {
            log.error("예약 상태 변경 알림 발송 실패: medicalRecordId={}", medicalRecord.getId(), e);
        }
    }

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
     * 프론트엔드용 알림 데이터 생성 (MedicalRecord)
     */
    private Map<String, Object> createReservationNotificationData(MedicalRecord medicalRecord) {
        Map<String, Object> data = new HashMap<>();
        
        data.put("id", medicalRecord.getId());
        data.put("hospitalId", medicalRecord.getHospitalId());
        data.put("userId", medicalRecord.getUserId());
        data.put("treatmentProductId", medicalRecord.getReservation().getTreatmentProductId());
        data.put("startTime", medicalRecord.getReservation().getStartTime());
        data.put("endTime", medicalRecord.getReservation().getEndTime());
        data.put("status", medicalRecord.getStatus().name());
        data.put("memo", medicalRecord.getReservation().getUserMemo());
        data.put("note", medicalRecord.getNote());

        return data;
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