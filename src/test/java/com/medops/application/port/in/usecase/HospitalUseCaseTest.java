package com.medops.application.port.in.usecase;

import com.medops.adapter.in.web.request.HospitalCreateRequest;
import com.medops.application.dto.HospitalWithProductsDto;
import com.medops.application.port.out.*;
import com.medops.application.service.HospitalService;
import com.medops.domain.enums.AdminRole;
import com.medops.domain.model.Hospital;
import com.medops.domain.model.TreatmentProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalUseCaseTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SaveHospitalPort saveHospitalPort;
    @Mock private SaveAdminPort saveAdminPort;
    @Mock private LoadHospitalPort loadHospitalPort;
    @Mock private LoadAdminPort loadAdminPort;
    @Mock private LoadTreatmentProductPort loadTreatmentProductPort;
    @Mock private BusinessHourUseCase businessHourUseCase;
    @Mock private TreatmentProductUseCase treatmentProductUseCase;

    private HospitalUseCase hospitalUseCase;

    private Hospital testHospital;
    private TreatmentProduct testProduct;

    @BeforeEach
    void setUp() {
        hospitalUseCase = new HospitalService(
            passwordEncoder,
            saveHospitalPort,
            saveAdminPort,
            loadHospitalPort,
            loadAdminPort,
            loadTreatmentProductPort,
            businessHourUseCase,
            treatmentProductUseCase
        );

        testHospital = Hospital.builder()
            .id("hospital-1")
            .name("테스트병원")
            .address("서울시 강남구")
            .createdAt(Instant.now())
            .build();

        testProduct = TreatmentProduct.builder()
            .id("product-1")
            .name("기본상품")
            .description("기본상품 설명")
            .price(BigDecimal.valueOf(10000))
            .maxCapacity(10)
            .hospitalId("hospital-1")
            .build();
    }

    @Test
    @DisplayName("병원 생성 성공 - 관리자와 기본 설정도 함께 생성")
    void should_createHospitalWithAdminAndDefaults_when_validRequest() {
        // given
        HospitalCreateRequest request = new HospitalCreateRequest(
            "새병원", "서울시 서초구", "admin@test.com", "김관리자", "password123"
        );
        
        when(loadHospitalPort.existsByName("새병원")).thenReturn(false);
        when(loadAdminPort.existsByEmail("admin@test.com")).thenReturn(false);
        when(saveHospitalPort.saveHospital(any(Hospital.class))).thenReturn(testHospital);

        // when
        Hospital result = hospitalUseCase.createHospital(request);

        // then
        assertEquals(testHospital, result);
        
        // 병원 저장 확인
        verify(saveHospitalPort).saveHospital(argThat(hospital -> 
            hospital.getName().equals("새병원") &&
            hospital.getAddress().equals("서울시 서초구") &&
            hospital.getId() != null
        ));
        
        // 관리자 저장 확인
        verify(saveAdminPort).saveAdmin(argThat(admin -> 
            admin.getEmail().equals("admin@test.com") &&
            admin.getName().equals("김관리자") &&
            admin.getPassword().equals("password123") &&
            admin.getRole() == AdminRole.ADMIN &&
            admin.getHospital().equals(testHospital)
        ));
        
        // 기본 설정 생성 확인
        verify(businessHourUseCase).initializeBusinessHours(testHospital.getId());
        verify(treatmentProductUseCase).initializeTreatmentProducts(testHospital.getId());
    }

    @Test
    @DisplayName("중복된 병원 이름으로 생성시 예외 발생")
    void should_throwException_when_duplicateHospitalName() {
        // given
        HospitalCreateRequest request = new HospitalCreateRequest(
            "기존병원", "서울시 강남구", "admin@test.com", "김관리자", "password123"
        );
        when(loadHospitalPort.existsByName("기존병원")).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> hospitalUseCase.createHospital(request)
        );
        assertEquals("병원 이름이 중복입니다", exception.getMessage());
        
        verify(loadHospitalPort).existsByName("기존병원");
        verifyNoInteractions(saveHospitalPort, saveAdminPort);
    }

    @Test
    @DisplayName("중복된 관리자 이메일로 생성시 예외 발생")
    void should_throwException_when_duplicateAdminEmail() {
        // given
        HospitalCreateRequest request = new HospitalCreateRequest(
            "새병원", "서울시 강남구", "existing@test.com", "김관리자", "password123"
        );
        when(loadHospitalPort.existsByName("새병원")).thenReturn(false);
        when(loadAdminPort.existsByEmail("existing@test.com")).thenReturn(true);

        // when & then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class, 
            () -> hospitalUseCase.createHospital(request)
        );
        assertEquals("어드민 중복입니다", exception.getMessage());
        
        verify(loadHospitalPort).existsByName("새병원");
        verify(loadAdminPort).existsByEmail("existing@test.com");
        verifyNoInteractions(saveHospitalPort, saveAdminPort);
    }

    @Test
    @DisplayName("모든 병원 목록 조회 성공 - 상품 정보와 함께")
    void should_returnHospitalsWithProducts_when_getAllHospitals() {
        // given
        List<Hospital> hospitals = List.of(testHospital);
        List<TreatmentProduct> products = List.of(testProduct);
        
        when(loadHospitalPort.loadAllHospitals()).thenReturn(hospitals);
        when(loadTreatmentProductPort.loadTreatmentProductsByHospitalIds(List.of("hospital-1")))
            .thenReturn(products);

        // when
        List<HospitalWithProductsDto> result = hospitalUseCase.getAllHospitals();

        // then
        assertEquals(1, result.size());
        HospitalWithProductsDto dto = result.get(0);
        assertEquals("hospital-1", dto.id());
        assertEquals("테스트병원", dto.name());
        assertEquals("서울시 강남구", dto.address());
        assertEquals(1, dto.treatmentProducts().size());
        assertEquals("기본상품", dto.treatmentProducts().get(0).name());
        
        verify(loadHospitalPort).loadAllHospitals();
        verify(loadTreatmentProductPort).loadTreatmentProductsByHospitalIds(List.of("hospital-1"));
    }

    @Test
    @DisplayName("병원이 없는 경우 빈 목록 반환")
    void should_returnEmptyList_when_noHospitals() {
        // given
        when(loadHospitalPort.loadAllHospitals()).thenReturn(List.of());
        when(loadTreatmentProductPort.loadTreatmentProductsByHospitalIds(List.of()))
            .thenReturn(List.of());

        // when
        List<HospitalWithProductsDto> result = hospitalUseCase.getAllHospitals();

        // then
        assertTrue(result.isEmpty());
        verify(loadHospitalPort).loadAllHospitals();
        verify(loadTreatmentProductPort).loadTreatmentProductsByHospitalIds(List.of());
    }

    @Test
    @DisplayName("병원 ID로 조회 성공")
    void should_returnHospital_when_getHospitalById() {
        // given
        String hospitalId = "hospital-1";
        when(loadHospitalPort.loadHospitalById(hospitalId)).thenReturn(Optional.of(testHospital));

        // when
        Optional<Hospital> result = hospitalUseCase.getHospitalById(hospitalId);

        // then
        assertTrue(result.isPresent());
        assertEquals(testHospital, result.get());
        verify(loadHospitalPort).loadHospitalById(hospitalId);
    }

    @Test
    @DisplayName("존재하지 않는 병원 ID로 조회시 빈 Optional 반환")
    void should_returnEmpty_when_hospitalNotFoundById() {
        // given
        String nonExistentId = "nonexistent-hospital";
        when(loadHospitalPort.loadHospitalById(nonExistentId)).thenReturn(Optional.empty());

        // when
        Optional<Hospital> result = hospitalUseCase.getHospitalById(nonExistentId);

        // then
        assertTrue(result.isEmpty());
        verify(loadHospitalPort).loadHospitalById(nonExistentId);
    }

    @Test
    @DisplayName("병원 이름으로 조회 성공")
    void should_returnHospital_when_getHospitalByName() {
        // given
        String hospitalName = "테스트병원";
        when(loadHospitalPort.loadHospitalByName(hospitalName)).thenReturn(Optional.of(testHospital));

        // when
        Optional<Hospital> result = hospitalUseCase.getHospitalByName(hospitalName);

        // then
        assertTrue(result.isPresent());
        assertEquals(testHospital, result.get());
        verify(loadHospitalPort).loadHospitalByName(hospitalName);
    }

    @Test
    @DisplayName("존재하지 않는 병원 이름으로 조회시 빈 Optional 반환")
    void should_returnEmpty_when_hospitalNotFoundByName() {
        // given
        String nonExistentName = "존재하지않는병원";
        when(loadHospitalPort.loadHospitalByName(nonExistentName)).thenReturn(Optional.empty());

        // when
        Optional<Hospital> result = hospitalUseCase.getHospitalByName(nonExistentName);

        // then
        assertTrue(result.isEmpty());
        verify(loadHospitalPort).loadHospitalByName(nonExistentName);
    }

    @Test
    @DisplayName("여러 병원의 상품 정보와 함께 조회")
    void should_returnMultipleHospitalsWithProducts_when_multipleHospitalsExist() {
        // given
        Hospital hospital2 = Hospital.builder()
            .id("hospital-2")
            .name("두번째병원")
            .address("부산시 해운대구")
            .createdAt(Instant.now())
            .build();
            
        TreatmentProduct product2 = TreatmentProduct.builder()
            .id("product-2")
            .name("특별상품")
            .description("특별상품 설명")
            .price(BigDecimal.valueOf(20000))
            .maxCapacity(5)
            .hospitalId("hospital-2")
            .build();
        
        List<Hospital> hospitals = List.of(testHospital, hospital2);
        List<TreatmentProduct> products = List.of(testProduct, product2);
        
        when(loadHospitalPort.loadAllHospitals()).thenReturn(hospitals);
        when(loadTreatmentProductPort.loadTreatmentProductsByHospitalIds(List.of("hospital-1", "hospital-2")))
            .thenReturn(products);

        // when
        List<HospitalWithProductsDto> result = hospitalUseCase.getAllHospitals();

        // then
        assertEquals(2, result.size());
        
        // 첫 번째 병원 확인
        HospitalWithProductsDto firstHospital = result.stream()
            .filter(dto -> dto.id().equals("hospital-1"))
            .findFirst()
            .orElseThrow();
        assertEquals("테스트병원", firstHospital.name());
        assertEquals(1, firstHospital.treatmentProducts().size());
        
        // 두 번째 병원 확인
        HospitalWithProductsDto secondHospital = result.stream()
            .filter(dto -> dto.id().equals("hospital-2"))
            .findFirst()
            .orElseThrow();
        assertEquals("두번째병원", secondHospital.name());
        assertEquals(1, secondHospital.treatmentProducts().size());
        
        verify(loadHospitalPort).loadAllHospitals();
        verify(loadTreatmentProductPort).loadTreatmentProductsByHospitalIds(List.of("hospital-1", "hospital-2"));
    }
}