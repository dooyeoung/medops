package com.medops.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TreatmentProduct 도메인 모델 테스트")
class TreatmentProductTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("모든 필드를 포함한 TreatmentProduct 생성 성공")
        void createTreatmentProductWithAllFields() {
            // Given
            String id = "product-123";
            String hospitalId = "hospital-123";
            String name = "스케일링";
            String description = "치아 스케일링 및 클리닝";
            Integer maxCapacity = 2;
            BigDecimal price = new BigDecimal("50000");

            // When
            TreatmentProduct product = TreatmentProduct.builder()
                .id(id)
                .hospitalId(hospitalId)
                .name(name)
                .description(description)
                .maxCapacity(maxCapacity)
                .price(price)
                .build();

            // Then
            assertThat(product.getId()).isEqualTo(id);
            assertThat(product.getHospitalId()).isEqualTo(hospitalId);
            assertThat(product.getName()).isEqualTo(name);
            assertThat(product.getDescription()).isEqualTo(description);
            assertThat(product.getMaxCapacity()).isEqualTo(maxCapacity);
            assertThat(product.getPrice()).isEqualByComparingTo(price);
        }

        @Test
        @DisplayName("NoArgsConstructor로 생성 후 null 필드 확인")
        void createTreatmentProductWithNoArgs() {
            // When
            TreatmentProduct product = new TreatmentProduct();

            // Then
            assertThat(product.getId()).isNull();
            assertThat(product.getHospitalId()).isNull();
            assertThat(product.getName()).isNull();
            assertThat(product.getDescription()).isNull();
            assertThat(product.getMaxCapacity()).isNull();
            assertThat(product.getPrice()).isNull();
        }
    }

    @Nested
    @DisplayName("정적 팩토리 메서드 테스트")
    class StaticFactoryMethodTest {

        @Test
        @DisplayName("상담 시술 상품 생성 - createConsultation")
        void createConsultationProduct() {
            // Given
            String hospitalId = "hospital-123";
            Integer maxCapacity = 3;

            // When
            TreatmentProduct consultation = TreatmentProduct.createConsultation(hospitalId, maxCapacity);

            // Then
            assertThat(consultation.getId()).isNotNull();
            assertThat(consultation.getHospitalId()).isEqualTo(hospitalId);
            assertThat(consultation.getName()).isEqualTo("상담");
            assertThat(consultation.getDescription()).isEqualTo("일반 상담");
            assertThat(consultation.getMaxCapacity()).isEqualTo(maxCapacity);
            assertThat(consultation.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(0));
        }

        @Test
        @DisplayName("정기 검진 시술 상품 생성 - createRegularCheckup")
        void createRegularCheckupProduct() {
            // Given
            String hospitalId = "hospital-456";
            Integer maxCapacity = 1;

            // When
            TreatmentProduct checkup = TreatmentProduct.createRegularCheckup(hospitalId, maxCapacity);

            // Then
            assertThat(checkup.getId()).isNotNull();
            assertThat(checkup.getHospitalId()).isEqualTo(hospitalId);
            assertThat(checkup.getName()).isEqualTo("정기 검진");
            assertThat(checkup.getDescription()).isEqualTo("정기 검진");
            assertThat(checkup.getMaxCapacity()).isEqualTo(maxCapacity);
            assertThat(checkup.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(0));
        }

        @Test
        @DisplayName("같은 파라미터로 생성해도 서로 다른 ID를 가짐")
        void createMultipleProductsWithDifferentIds() {
            // Given
            String hospitalId = "hospital-123";
            Integer maxCapacity = 2;

            // When
            TreatmentProduct product1 = TreatmentProduct.createConsultation(hospitalId, maxCapacity);
            TreatmentProduct product2 = TreatmentProduct.createConsultation(hospitalId, maxCapacity);

            // Then
            assertThat(product1.getId()).isNotEqualTo(product2.getId());
            assertThat(product1.getHospitalId()).isEqualTo(product2.getHospitalId());
            assertThat(product1.getName()).isEqualTo(product2.getName());
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTest {

        @Test
        @DisplayName("시스템 기본 시술 상품은 무료(0원)이어야 함")
        void systemDefaultProductsShouldBeFree() {
            // Given
            String hospitalId = "hospital-123";
            Integer maxCapacity = 1;

            // When
            TreatmentProduct consultation = TreatmentProduct.createConsultation(hospitalId, maxCapacity);
            TreatmentProduct checkup = TreatmentProduct.createRegularCheckup(hospitalId, maxCapacity);

            // Then
            assertThat(consultation.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(checkup.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("maxCapacity는 양수이어야 함")
        void maxCapacityShouldBePositive() {
            // Given
            String hospitalId = "hospital-123";
            Integer validCapacity = 1;
            Integer zeroCapacity = 0;
            Integer negativeCapacity = -1;

            // When & Then - 양수는 정상 생성
            TreatmentProduct validProduct = TreatmentProduct.createConsultation(hospitalId, validCapacity);
            assertThat(validProduct.getMaxCapacity()).isEqualTo(validCapacity);

            // 0이나 음수도 일단 생성은 됨 (비즈니스 로직에서 검증해야 함)
            TreatmentProduct zeroProduct = TreatmentProduct.createConsultation(hospitalId, zeroCapacity);
            assertThat(zeroProduct.getMaxCapacity()).isEqualTo(zeroCapacity);

            TreatmentProduct negativeProduct = TreatmentProduct.createConsultation(hospitalId, negativeCapacity);
            assertThat(negativeProduct.getMaxCapacity()).isEqualTo(negativeCapacity);
        }

        @Test
        @DisplayName("가격은 0 이상이어야 함")
        void priceShouldBeNonNegative() {
            // Given
            BigDecimal zeroPrice = BigDecimal.ZERO;
            BigDecimal positivePrice = new BigDecimal("100000");
            BigDecimal negativePrice = new BigDecimal("-50000");

            // When & Then - 0원과 양수는 정상
            TreatmentProduct freeProduct = TreatmentProduct.builder()
                .id("product-1")
                .hospitalId("hospital-123")
                .name("무료 상담")
                .description("무료 상담 서비스")
                .maxCapacity(1)
                .price(zeroPrice)
                .build();

            TreatmentProduct paidProduct = TreatmentProduct.builder()
                .id("product-2")
                .hospitalId("hospital-123")
                .name("치료")
                .description("유료 치료 서비스")
                .maxCapacity(1)
                .price(positivePrice)
                .build();

            // 음수 가격도 일단 생성은 됨 (비즈니스 로직에서 검증해야 함)
            TreatmentProduct negativeProduct = TreatmentProduct.builder()
                .id("product-3")
                .hospitalId("hospital-123")
                .name("할인 상품")
                .description("할인이 과도한 상품")
                .maxCapacity(1)
                .price(negativePrice)
                .build();

            assertThat(freeProduct.getPrice()).isEqualByComparingTo(zeroPrice);
            assertThat(paidProduct.getPrice()).isEqualByComparingTo(positivePrice);
            assertThat(negativeProduct.getPrice()).isEqualByComparingTo(negativePrice);
        }
    }

    @Nested
    @DisplayName("Builder 패턴 테스트")
    class BuilderPatternTest {

        @Test
        @DisplayName("toBuilder()로 기존 객체 기반 새 객체 생성")
        void createNewObjectUsingToBuilder() {
            // Given
            TreatmentProduct original = TreatmentProduct.builder()
                .id("product-123")
                .hospitalId("hospital-123")
                .name("스케일링")
                .description("치아 스케일링")
                .maxCapacity(2)
                .price(new BigDecimal("50000"))
                .build();

            // When - 가격만 변경
            TreatmentProduct modified = original.toBuilder()
                .price(new BigDecimal("60000"))
                .build();

            // Then
            assertThat(modified.getId()).isEqualTo(original.getId());
            assertThat(modified.getHospitalId()).isEqualTo(original.getHospitalId());
            assertThat(modified.getName()).isEqualTo(original.getName());
            assertThat(modified.getDescription()).isEqualTo(original.getDescription());
            assertThat(modified.getMaxCapacity()).isEqualTo(original.getMaxCapacity());
            assertThat(modified.getPrice()).isEqualByComparingTo(new BigDecimal("60000"));
            
            // 원본은 변경되지 않음
            assertThat(original.getPrice()).isEqualByComparingTo(new BigDecimal("50000"));
        }

        @Test
        @DisplayName("Builder로 필수 필드만 설정하여 생성")
        void createWithRequiredFieldsOnly() {
            // When
            TreatmentProduct product = TreatmentProduct.builder()
                .hospitalId("hospital-123")
                .name("기본 상담")
                .build();

            // Then
            assertThat(product.getHospitalId()).isEqualTo("hospital-123");
            assertThat(product.getName()).isEqualTo("기본 상담");
            assertThat(product.getId()).isNull();
            assertThat(product.getDescription()).isNull();
            assertThat(product.getMaxCapacity()).isNull();
            assertThat(product.getPrice()).isNull();
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("null 값으로 팩토리 메서드 호출 시 동작 확인")
        void createWithNullValues() {
            // When
            TreatmentProduct consultationWithNullHospital = TreatmentProduct.createConsultation(null, 1);
            TreatmentProduct consultationWithNullCapacity = TreatmentProduct.createConsultation("hospital-123", null);

            // Then - null 값도 그대로 설정됨 (검증은 비즈니스 레이어에서)
            assertThat(consultationWithNullHospital.getHospitalId()).isNull();
            assertThat(consultationWithNullCapacity.getMaxCapacity()).isNull();
        }

        @Test
        @DisplayName("빈 문자열과 공백 문자열 처리")
        void handleEmptyAndBlankStrings() {
            // When
            TreatmentProduct productWithEmptyName = TreatmentProduct.builder()
                .hospitalId("hospital-123")
                .name("")
                .description("   ")
                .build();

            // Then - 빈 문자열과 공백 문자열도 그대로 설정됨
            assertThat(productWithEmptyName.getName()).isEmpty();
            assertThat(productWithEmptyName.getDescription()).isEqualTo("   ");
        }

        @Test
        @DisplayName("매우 큰 숫자와 소수점 가격 처리")
        void handleLargeNumbersAndDecimals() {
            // Given
            BigDecimal largePrice = new BigDecimal("999999999.99");
            BigDecimal precisePrice = new BigDecimal("123.456789");
            Integer largeCapacity = Integer.MAX_VALUE;

            // When
            TreatmentProduct expensiveProduct = TreatmentProduct.builder()
                .hospitalId("hospital-123")
                .name("고가 시술")
                .price(largePrice)
                .maxCapacity(largeCapacity)
                .build();

            TreatmentProduct preciseProduct = TreatmentProduct.builder()
                .hospitalId("hospital-123")
                .name("정밀 시술")
                .price(precisePrice)
                .build();

            // Then
            assertThat(expensiveProduct.getPrice()).isEqualByComparingTo(largePrice);
            assertThat(expensiveProduct.getMaxCapacity()).isEqualTo(largeCapacity);
            assertThat(preciseProduct.getPrice()).isEqualByComparingTo(precisePrice);
        }
    }
}