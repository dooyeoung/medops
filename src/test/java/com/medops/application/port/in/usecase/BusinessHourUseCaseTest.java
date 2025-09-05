package com.medops.application.port.in.usecase;

import com.medops.application.port.in.command.UpdateBusinessHourCommand;
import com.medops.application.port.out.LoadBusinessHourPort;
import com.medops.application.port.out.LoadHospitalPort;
import com.medops.application.port.out.SaveBusinessHourPort;
import com.medops.application.service.BusinessHourService;
import com.medops.common.exception.NotFoundResource;
import com.medops.domain.model.BusinessHour;
import com.medops.domain.model.Hospital;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessHourUseCaseTest {

    @Mock private SaveBusinessHourPort saveBusinessHourPort;
    @Mock private LoadHospitalPort loadHospitalPort;
    @Mock private LoadBusinessHourPort loadBusinessHourPort;

    private BusinessHourUseCase businessHourUseCase;

    private Hospital testHospital;
    private BusinessHour testBusinessHour;

    @BeforeEach
    void setUp() {
        businessHourUseCase = new BusinessHourService(
            saveBusinessHourPort,
            loadHospitalPort,
            loadBusinessHourPort
        );

        testHospital = Hospital.builder()
            .id("hospital-1")
            .name("Test Hospital")
            .address("Test Address")
            .build();

        testBusinessHour = BusinessHour.builder()
            .id("business-hour-1")
            .hospital(testHospital)
            .dayOfWeek(DayOfWeek.MONDAY)
            .isClosed(false)
            .openTime("09:00")
            .closeTime("18:00")
            .breakStartTime("12:00")
            .breakEndTime("13:00")
            .build();
    }

    @Test
    @DisplayName("병원 영업시간 초기화시 7일간의 영업시간 생성")
    void should_createSevenDaysBusinessHours_when_initializeBusinessHours() {
        // given
        String hospitalId = "hospital-1";
        when(loadHospitalPort.loadHospitalById(hospitalId)).thenReturn(Optional.of(testHospital));

        // when
        List<BusinessHour> result = businessHourUseCase.initializeBusinessHours(hospitalId);

        // then
        verify(loadHospitalPort).loadHospitalById(hospitalId);
        verify(saveBusinessHourPort, times(7)).saveBusinessHour(any(BusinessHour.class));
        
        // 평일과 주말 모두 검증하지만 각각 한 번씩만 호출되므로 times 사용하지 않음
    }

    @Test
    @DisplayName("존재하지 않는 병원 ID로 영업시간 초기화시 예외 발생")
    void should_throwException_when_hospitalNotFoundForInitialize() {
        // given
        String nonExistentHospitalId = "nonexistent-hospital";
        when(loadHospitalPort.loadHospitalById(nonExistentHospitalId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(RuntimeException.class, () -> businessHourUseCase.initializeBusinessHours(nonExistentHospitalId));
        verify(loadHospitalPort).loadHospitalById(nonExistentHospitalId);
        verifyNoInteractions(saveBusinessHourPort);
    }

    @Test
    @DisplayName("병원 ID로 영업시간 목록 조회 성공")
    void should_returnBusinessHours_when_getBusinessHoursByHospitalId() {
        // given
        String hospitalId = "hospital-1";
        List<BusinessHour> expectedBusinessHours = List.of(testBusinessHour);
        when(loadBusinessHourPort.loadBusinessHoursByHospitalId(hospitalId)).thenReturn(expectedBusinessHours);

        // when
        List<BusinessHour> result = businessHourUseCase.getBusinessHoursByHospitalId(hospitalId);

        // then
        assertEquals(expectedBusinessHours, result);
        verify(loadBusinessHourPort).loadBusinessHoursByHospitalId(hospitalId);
    }

    @Test
    @DisplayName("존재하지 않는 병원 ID로 영업시간 조회시 빈 목록 반환")
    void should_returnEmptyList_when_hospitalNotFoundForGet() {
        // given
        String nonExistentHospitalId = "nonexistent-hospital";
        when(loadBusinessHourPort.loadBusinessHoursByHospitalId(nonExistentHospitalId)).thenReturn(List.of());

        // when
        List<BusinessHour> result = businessHourUseCase.getBusinessHoursByHospitalId(nonExistentHospitalId);

        // then
        assertTrue(result.isEmpty());
        verify(loadBusinessHourPort).loadBusinessHoursByHospitalId(nonExistentHospitalId);
    }

    @Test
    @DisplayName("영업시간 업데이트 성공")
    void should_updateBusinessHour_when_validCommand() {
        // given
        UpdateBusinessHourCommand command = new UpdateBusinessHourCommand(
            "business-hour-1",
            "08:30",
            "19:00",
            "12:30",
            "13:30",
            false
        );
        
        when(loadBusinessHourPort.loadBusinessHourById("business-hour-1")).thenReturn(Optional.of(testBusinessHour));

        // when
        businessHourUseCase.updateBusinessHour(command);

        // then
        verify(loadBusinessHourPort).loadBusinessHourById("business-hour-1");
        verify(saveBusinessHourPort).saveBusinessHour(argThat(businessHour -> 
            businessHour.getOpenTime().equals("08:30") &&
            businessHour.getCloseTime().equals("19:00") &&
            businessHour.getBreakStartTime().equals("12:30") &&
            businessHour.getBreakEndTime().equals("13:30") &&
            !businessHour.isClosed()
        ));
    }

    @Test
    @DisplayName("존재하지 않는 영업시간 ID로 업데이트시 NotFoundResource 예외 발생")
    void should_throwNotFoundResource_when_businessHourNotExists() {
        // given
        UpdateBusinessHourCommand command = new UpdateBusinessHourCommand(
            "nonexistent-business-hour",
            "08:30",
            "19:00",
            "12:30",
            "13:30",
            false
        );
        
        when(loadBusinessHourPort.loadBusinessHourById("nonexistent-business-hour")).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundResource.class, () -> businessHourUseCase.updateBusinessHour(command));
        verify(loadBusinessHourPort).loadBusinessHourById("nonexistent-business-hour");
        verifyNoInteractions(saveBusinessHourPort);
    }

    @Test
    @DisplayName("영업시간을 휴무로 변경")
    void should_updateBusinessHourToClosed_when_closedIsTrue() {
        // given
        UpdateBusinessHourCommand command = new UpdateBusinessHourCommand(
            "business-hour-1",
            null,
            null,
            null,
            null,
            true
        );
        
        when(loadBusinessHourPort.loadBusinessHourById("business-hour-1")).thenReturn(Optional.of(testBusinessHour));

        // when
        businessHourUseCase.updateBusinessHour(command);

        // then
        verify(loadBusinessHourPort).loadBusinessHourById("business-hour-1");
        verify(saveBusinessHourPort).saveBusinessHour(argThat(businessHour -> 
            businessHour.isClosed() &&
            businessHour.getOpenTime() == null &&
            businessHour.getCloseTime() == null &&
            businessHour.getBreakStartTime() == null &&
            businessHour.getBreakEndTime() == null
        ));
    }

    @Test
    @DisplayName("휴무에서 영업시간으로 변경")
    void should_updateBusinessHourFromClosedToOpen_when_closedIsFalse() {
        // given
        BusinessHour closedBusinessHour = BusinessHour.builder()
            .id("business-hour-1")
            .hospital(testHospital)
            .dayOfWeek(DayOfWeek.SUNDAY)
            .isClosed(true)
            .openTime(null)
            .closeTime(null)
            .breakStartTime(null)
            .breakEndTime(null)
            .build();
            
        UpdateBusinessHourCommand command = new UpdateBusinessHourCommand(
            "business-hour-1",
            "10:00",
            "17:00",
            "12:00",
            "13:00",
            false
        );
        
        when(loadBusinessHourPort.loadBusinessHourById("business-hour-1")).thenReturn(Optional.of(closedBusinessHour));

        // when
        businessHourUseCase.updateBusinessHour(command);

        // then
        verify(loadBusinessHourPort).loadBusinessHourById("business-hour-1");
        verify(saveBusinessHourPort).saveBusinessHour(argThat(businessHour -> 
            !businessHour.isClosed() &&
            businessHour.getOpenTime().equals("10:00") &&
            businessHour.getCloseTime().equals("17:00") &&
            businessHour.getBreakStartTime().equals("12:00") &&
            businessHour.getBreakEndTime().equals("13:00")
        ));
    }
}