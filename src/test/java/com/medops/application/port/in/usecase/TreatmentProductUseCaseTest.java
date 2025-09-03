package com.medops.application.port.in.usecase;

import com.medops.application.port.in.command.CreateTreatmentProductCommand;
import com.medops.application.port.in.command.DeleteTreatmentProductCommand;
import com.medops.application.port.in.command.UpdateTreatmentProductCommand;
import com.medops.application.port.out.LoadTreatmentProductPort;
import com.medops.application.port.out.SaveTreatmentProductPort;
import com.medops.application.service.TreatmentProductService;
import com.medops.domain.model.TreatmentProduct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TreatmentProductUseCaseTest {

    @Mock private SaveTreatmentProductPort saveTreatmentProductPort;
    @Mock private LoadTreatmentProductPort loadTreatmentProductPort;

    private TreatmentProductUseCase treatmentProductUseCase;

    private TreatmentProduct testProduct;

    @BeforeEach
    void setUp() {
        treatmentProductUseCase = new TreatmentProductService(
            saveTreatmentProductPort,
            loadTreatmentProductPort
        );

        testProduct = TreatmentProduct.builder()
            .id("product-1")
            .hospitalId("hospital-1")
            .name("기본상담")
            .description("기본 의료 상담")
            .price(BigDecimal.valueOf(30000))
            .maxCapacity(5)
            .build();
    }

    @Test
    @DisplayName("초기 치료 상품 생성 - 상담과 정기검진")
    void should_createInitialProducts_when_initializeTreatmentProducts() {
        // given
        String hospitalId = "hospital-1";

        // when
        treatmentProductUseCase.initializeTreatmentProducts(hospitalId);

        // then
        verify(saveTreatmentProductPort, times(2)).saveTreatmentProduct(any(TreatmentProduct.class));
        
        // 상담 상품 검증
        verify(saveTreatmentProductPort).saveTreatmentProduct(argThat(product -> 
            product.getName().equals("상담") &&
            product.getMaxCapacity().equals(3) &&
            product.getHospitalId().equals(hospitalId)
        ));
        
        // 정기검진 상품 검증
        verify(saveTreatmentProductPort).saveTreatmentProduct(argThat(product -> 
            product.getName().equals("정기 검진") &&
            product.getMaxCapacity().equals(1) &&
            product.getHospitalId().equals(hospitalId)
        ));
    }

    @Test
    @DisplayName("치료 상품 생성 성공")
    void should_createTreatmentProduct_when_validCommand() {
        // given
        CreateTreatmentProductCommand command = new CreateTreatmentProductCommand(
            "hospital-1", "특별상담", "전문의 특별 상담", 2, BigDecimal.valueOf(50000)
        );
        
        when(saveTreatmentProductPort.saveTreatmentProduct(any(TreatmentProduct.class)))
            .thenReturn(testProduct);

        // when
        TreatmentProduct result = treatmentProductUseCase.createTreatmentProduct(command);

        // then
        assertEquals(testProduct, result);
        verify(saveTreatmentProductPort).saveTreatmentProduct(argThat(product -> 
            product.getHospitalId().equals("hospital-1") &&
            product.getName().equals("특별상담") &&
            product.getDescription().equals("전문의 특별 상담") &&
            product.getMaxCapacity().equals(2) &&
            product.getPrice().equals(BigDecimal.valueOf(50000)) &&
            product.getId() != null
        ));
    }

    @Test
    @DisplayName("병원별 치료 상품 목록 조회 성공")
    void should_returnTreatmentProducts_when_getTreatmentProductsByHospitalId() {
        // given
        String hospitalId = "hospital-1";
        List<TreatmentProduct> expectedProducts = List.of(testProduct);
        when(loadTreatmentProductPort.loadTreatmentProductsByHospitalId(hospitalId))
            .thenReturn(expectedProducts);

        // when
        List<TreatmentProduct> result = treatmentProductUseCase.getTreatmentProductsByHospitalId(hospitalId);

        // then
        assertEquals(expectedProducts, result);
        assertEquals(1, result.size());
        assertEquals("기본상담", result.get(0).getName());
        verify(loadTreatmentProductPort).loadTreatmentProductsByHospitalId(hospitalId);
    }

    @Test
    @DisplayName("존재하지 않는 병원의 치료 상품 조회시 빈 목록 반환")
    void should_returnEmptyList_when_hospitalNotFound() {
        // given
        String nonExistentHospitalId = "nonexistent-hospital";
        when(loadTreatmentProductPort.loadTreatmentProductsByHospitalId(nonExistentHospitalId))
            .thenReturn(List.of());

        // when
        List<TreatmentProduct> result = treatmentProductUseCase.getTreatmentProductsByHospitalId(nonExistentHospitalId);

        // then
        assertTrue(result.isEmpty());
        verify(loadTreatmentProductPort).loadTreatmentProductsByHospitalId(nonExistentHospitalId);
    }

    @Test
    @DisplayName("치료 상품 정보 업데이트 성공")
    void should_updateTreatmentProduct_when_validCommand() {
        // given
        UpdateTreatmentProductCommand command = new UpdateTreatmentProductCommand(
            "hospital-1", "product-1", "수정된상담", "수정된 설명", 10, BigDecimal.valueOf(40000)
        );

        // when
        treatmentProductUseCase.updateTreatmentProduct(command);

        // then
        verify(saveTreatmentProductPort).saveTreatmentProduct(argThat(product -> 
            product.getId().equals("product-1") &&
            product.getHospitalId().equals("hospital-1") &&
            product.getName().equals("수정된상담") &&
            product.getDescription().equals("수정된 설명") &&
            product.getMaxCapacity().equals(10) &&
            product.getPrice().equals(BigDecimal.valueOf(40000))
        ));
    }

    @Test
    @DisplayName("치료 상품 삭제 성공")
    void should_deleteTreatmentProduct_when_validCommand() {
        // given
        DeleteTreatmentProductCommand command = new DeleteTreatmentProductCommand("product-1");

        // when
        treatmentProductUseCase.deleteTreatmentProduct(command);

        // then
        verify(saveTreatmentProductPort).deleteTreatmentProduct("product-1");
    }

    @Test
    @DisplayName("여러 치료 상품 조회")
    void should_returnMultipleProducts_when_hospitalHasMultipleProducts() {
        // given
        String hospitalId = "hospital-1";
        TreatmentProduct product2 = TreatmentProduct.builder()
            .id("product-2")
            .hospitalId(hospitalId)
            .name("정밀검사")
            .description("정밀 의료 검사")
            .price(BigDecimal.valueOf(100000))
            .maxCapacity(2)
            .build();
            
        List<TreatmentProduct> multipleProducts = List.of(testProduct, product2);
        when(loadTreatmentProductPort.loadTreatmentProductsByHospitalId(hospitalId))
            .thenReturn(multipleProducts);

        // when
        List<TreatmentProduct> result = treatmentProductUseCase.getTreatmentProductsByHospitalId(hospitalId);

        // then
        assertEquals(2, result.size());
        assertEquals(multipleProducts, result);
        assertTrue(result.stream().allMatch(product -> product.getHospitalId().equals(hospitalId)));
        verify(loadTreatmentProductPort).loadTreatmentProductsByHospitalId(hospitalId);
    }

    @Test
    @DisplayName("무료 상품 생성")
    void should_createFreeProduct_when_priceIsZero() {
        // given
        CreateTreatmentProductCommand command = new CreateTreatmentProductCommand(
            "hospital-1", "무료상담", "무료 의료 상담", 10, BigDecimal.ZERO
        );
        
        when(saveTreatmentProductPort.saveTreatmentProduct(any(TreatmentProduct.class)))
            .thenReturn(testProduct);

        // when
        TreatmentProduct result = treatmentProductUseCase.createTreatmentProduct(command);

        // then
        assertNotNull(result);
        verify(saveTreatmentProductPort).saveTreatmentProduct(argThat(product -> 
            product.getPrice().equals(BigDecimal.ZERO) &&
            product.getName().equals("무료상담")
        ));
    }

    @Test
    @DisplayName("대용량 상품 생성")
    void should_createHighCapacityProduct_when_maxCapacityIsHigh() {
        // given
        CreateTreatmentProductCommand command = new CreateTreatmentProductCommand(
            "hospital-1", "대규모상담", "대규모 그룹 상담", 100, BigDecimal.valueOf(20000)
        );
        
        when(saveTreatmentProductPort.saveTreatmentProduct(any(TreatmentProduct.class)))
            .thenReturn(testProduct);

        // when
        TreatmentProduct result = treatmentProductUseCase.createTreatmentProduct(command);

        // then
        assertNotNull(result);
        verify(saveTreatmentProductPort).saveTreatmentProduct(argThat(product -> 
            product.getMaxCapacity().equals(100) &&
            product.getName().equals("대규모상담")
        ));
    }

    @Test
    @DisplayName("상품 생성시 UUID 자동 생성 확인")
    void should_generateUUID_when_createTreatmentProduct() {
        // given
        CreateTreatmentProductCommand command = new CreateTreatmentProductCommand(
            "hospital-1", "테스트상품", "테스트 설명", 5, BigDecimal.valueOf(25000)
        );
        
        when(saveTreatmentProductPort.saveTreatmentProduct(any(TreatmentProduct.class)))
            .thenReturn(testProduct);

        // when
        treatmentProductUseCase.createTreatmentProduct(command);

        // then
        verify(saveTreatmentProductPort).saveTreatmentProduct(argThat(product -> 
            product.getId() != null && !product.getId().isEmpty()
        ));
    }
}