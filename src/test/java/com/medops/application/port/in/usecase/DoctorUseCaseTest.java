package com.medops.application.port.in.usecase;

import com.medops.application.port.in.command.CreateDoctorCommand;
import com.medops.application.port.in.command.UpdateDoctorCommand;
import com.medops.application.port.out.LoadDoctorPort;
import com.medops.application.port.out.SaveDoctorPort;
import com.medops.application.service.DoctorService;
import com.medops.common.exception.NotFoundResource;
import com.medops.domain.model.Doctor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorUseCaseTest {

    @Mock private LoadDoctorPort loadDoctorPort;
    @Mock private SaveDoctorPort saveDoctorPort;

    private DoctorUseCase doctorUseCase;

    private Doctor testDoctor;

    @BeforeEach
    void setUp() {
        doctorUseCase = new DoctorService(loadDoctorPort, saveDoctorPort);

        testDoctor = Doctor.builder()
            .id("doctor-1")
            .name("김의사")
            .hospitalId("hospital-1")
            .build();
    }

    @Test
    @DisplayName("병원 ID로 의사 목록 조회 성공")
    void should_returnDoctors_when_getDoctorsByHospitalId() {
        // given
        String hospitalId = "hospital-1";
        List<Doctor> expectedDoctors = List.of(testDoctor);
        when(loadDoctorPort.loadDoctorsByHospitalId(hospitalId)).thenReturn(expectedDoctors);

        // when
        List<Doctor> result = doctorUseCase.getDoctorsByHospitalId(hospitalId);

        // then
        assertEquals(expectedDoctors, result);
        assertEquals(1, result.size());
        assertEquals("김의사", result.get(0).getName());
        verify(loadDoctorPort).loadDoctorsByHospitalId(hospitalId);
    }

    @Test
    @DisplayName("존재하지 않는 병원 ID로 의사 조회시 빈 목록 반환")
    void should_returnEmptyList_when_hospitalNotFound() {
        // given
        String nonExistentHospitalId = "nonexistent-hospital";
        when(loadDoctorPort.loadDoctorsByHospitalId(nonExistentHospitalId)).thenReturn(List.of());

        // when
        List<Doctor> result = doctorUseCase.getDoctorsByHospitalId(nonExistentHospitalId);

        // then
        assertTrue(result.isEmpty());
        verify(loadDoctorPort).loadDoctorsByHospitalId(nonExistentHospitalId);
    }

    @Test
    @DisplayName("의사 생성 성공")
    void should_createDoctor_when_validCommand() {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand("hospital-1", "이의사");

        // when
        doctorUseCase.createDoctor(command);

        // then
        verify(saveDoctorPort).saveDoctor(argThat(doctor -> 
            doctor.getName().equals("이의사") &&
            doctor.getHospitalId().equals("hospital-1") &&
            doctor.getId() != null
        ));
    }

    @Test
    @DisplayName("의사 생성시 UUID가 자동 생성됨")
    void should_generateUUID_when_createDoctor() {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand("hospital-1", "박의사");

        // when
        doctorUseCase.createDoctor(command);

        // then
        verify(saveDoctorPort).saveDoctor(argThat(doctor -> 
            doctor.getId() != null && 
            !doctor.getId().isEmpty()
        ));
    }

    @Test
    @DisplayName("의사 정보 업데이트 성공")
    void should_updateDoctor_when_validCommand() {
        // given
        UpdateDoctorCommand command = new UpdateDoctorCommand("doctor-1", "김수정의사");
        when(loadDoctorPort.loadDoctorById("doctor-1")).thenReturn(Optional.of(testDoctor));

        // when
        doctorUseCase.updateDoctor(command);

        // then
        verify(loadDoctorPort).loadDoctorById("doctor-1");
        verify(saveDoctorPort).saveDoctor(argThat(doctor -> 
            doctor.getId().equals("doctor-1") &&
            doctor.getName().equals("김수정의사") &&
            doctor.getHospitalId().equals("hospital-1")
        ));
    }

    @Test
    @DisplayName("존재하지 않는 의사 업데이트시 NotFoundResource 예외 발생")
    void should_throwNotFoundResource_when_doctorNotExists() {
        // given
        UpdateDoctorCommand command = new UpdateDoctorCommand("nonexistent-doctor", "새이름");
        when(loadDoctorPort.loadDoctorById("nonexistent-doctor")).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundResource.class, () -> doctorUseCase.updateDoctor(command));
        verify(loadDoctorPort).loadDoctorById("nonexistent-doctor");
        verifyNoInteractions(saveDoctorPort);
    }

    @Test
    @DisplayName("의사 이름만 변경하고 다른 정보는 유지")
    void should_updateOnlyName_when_updateDoctor() {
        // given
        Doctor originalDoctor = Doctor.builder()
            .id("doctor-2")
            .name("원래이름")
            .hospitalId("hospital-2")
            .build();
            
        UpdateDoctorCommand command = new UpdateDoctorCommand("doctor-2", "변경된이름");
        when(loadDoctorPort.loadDoctorById("doctor-2")).thenReturn(Optional.of(originalDoctor));

        // when
        doctorUseCase.updateDoctor(command);

        // then
        verify(saveDoctorPort).saveDoctor(argThat(doctor -> 
            doctor.getId().equals("doctor-2") &&
            doctor.getName().equals("변경된이름") &&
            doctor.getHospitalId().equals("hospital-2")
        ));
    }

    @Test
    @DisplayName("여러 의사가 있는 병원의 의사 목록 조회")
    void should_returnMultipleDoctors_when_hospitalHasMultipleDoctors() {
        // given
        String hospitalId = "hospital-1";
        List<Doctor> multipleDoctors = List.of(
            Doctor.builder().id("doctor-1").name("김의사").hospitalId(hospitalId).build(),
            Doctor.builder().id("doctor-2").name("이의사").hospitalId(hospitalId).build(),
            Doctor.builder().id("doctor-3").name("박의사").hospitalId(hospitalId).build()
        );
        when(loadDoctorPort.loadDoctorsByHospitalId(hospitalId)).thenReturn(multipleDoctors);

        // when
        List<Doctor> result = doctorUseCase.getDoctorsByHospitalId(hospitalId);

        // then
        assertEquals(3, result.size());
        assertEquals(multipleDoctors, result);
        assertTrue(result.stream().allMatch(doctor -> doctor.getHospitalId().equals(hospitalId)));
        verify(loadDoctorPort).loadDoctorsByHospitalId(hospitalId);
    }

    @Test
    @DisplayName("빈 이름으로 의사 생성")
    void should_createDoctorWithEmptyName_when_emptyNameProvided() {
        // given
        CreateDoctorCommand command = new CreateDoctorCommand("hospital-1", "");

        // when
        doctorUseCase.createDoctor(command);

        // then
        verify(saveDoctorPort).saveDoctor(argThat(doctor -> 
            doctor.getName().equals("") &&
            doctor.getHospitalId().equals("hospital-1")
        ));
    }
}