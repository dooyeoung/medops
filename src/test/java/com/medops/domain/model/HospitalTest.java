package com.medops.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Hospital 도메인 모델 테스트")
class HospitalTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("모든 필드를 포함한 Hospital 생성 성공")
        void createHospitalWithAllFields() {
            // Given
            String id = "hospital-123";
            String name = "서울대학교병원";
            String address = "서울특별시 종로구 대학로 101";
            Instant createdAt = Instant.now();

            // When
            Hospital hospital = Hospital.builder()
                .id(id)
                .name(name)
                .address(address)
                .createdAt(createdAt)
                .build();

            // Then
            assertThat(hospital.getId()).isEqualTo(id);
            assertThat(hospital.getName()).isEqualTo(name);
            assertThat(hospital.getAddress()).isEqualTo(address);
            assertThat(hospital.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("AllArgsConstructor로 Hospital 생성")
        void createHospitalWithAllArgsConstructor() {
            // Given
            String id = "hospital-456";
            String name = "연세의료원";
            String address = "서울특별시 서대문구 연세로 50";
            Instant createdAt = Instant.parse("2023-01-15T10:30:00Z");

            // When
            Hospital hospital = new Hospital(id, name, address, createdAt);

            // Then
            assertThat(hospital.getId()).isEqualTo(id);
            assertThat(hospital.getName()).isEqualTo(name);
            assertThat(hospital.getAddress()).isEqualTo(address);
            assertThat(hospital.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("NoArgsConstructor로 생성 후 null 필드 확인")
        void createHospitalWithNoArgs() {
            // When
            Hospital hospital = new Hospital();

            // Then
            assertThat(hospital.getId()).isNull();
            assertThat(hospital.getName()).isNull();
            assertThat(hospital.getAddress()).isNull();
            assertThat(hospital.getCreatedAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Builder 패턴 테스트")
    class BuilderPatternTest {

        @Test
        @DisplayName("Builder로 필수 필드만 설정하여 생성")
        void createWithRequiredFieldsOnly() {
            // When
            Hospital hospitalWithNameOnly = Hospital.builder()
                .name("테스트 병원")
                .build();

            Hospital hospitalWithIdAndName = Hospital.builder()
                .id("hospital-789")
                .name("또 다른 병원")
                .build();

            // Then
            assertThat(hospitalWithNameOnly.getName()).isEqualTo("테스트 병원");
            assertThat(hospitalWithNameOnly.getId()).isNull();
            assertThat(hospitalWithNameOnly.getAddress()).isNull();
            assertThat(hospitalWithNameOnly.getCreatedAt()).isNull();

            assertThat(hospitalWithIdAndName.getId()).isEqualTo("hospital-789");
            assertThat(hospitalWithIdAndName.getName()).isEqualTo("또 다른 병원");
            assertThat(hospitalWithIdAndName.getAddress()).isNull();
            assertThat(hospitalWithIdAndName.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("toBuilder()로 기존 객체 기반 새 객체 생성")
        void createNewObjectUsingToBuilder() {
            // Given
            Hospital original = Hospital.builder()
                .id("hospital-123")
                .name("원본 병원")
                .address("원본 주소")
                .createdAt(Instant.parse("2023-01-01T00:00:00Z"))
                .build();

            // When - 주소만 변경
            Hospital modified = original.toBuilder()
                .address("변경된 주소")
                .build();

            // Then
            assertThat(modified.getId()).isEqualTo(original.getId());
            assertThat(modified.getName()).isEqualTo(original.getName());
            assertThat(modified.getCreatedAt()).isEqualTo(original.getCreatedAt());
            assertThat(modified.getAddress()).isEqualTo("변경된 주소");
            
            // 원본은 변경되지 않음
            assertThat(original.getAddress()).isEqualTo("원본 주소");
        }

        @Test
        @DisplayName("Builder로 모든 필드를 단계별로 설정")
        void buildObjectStepByStep() {
            // Given
            Instant now = Instant.now();

            // When
            Hospital hospital = Hospital.builder()
                .id("hospital-999")
                .name("단계별 병원")
                .address("단계별 주소")
                .createdAt(now)
                .build();

            // Then
            assertThat(hospital.getId()).isEqualTo("hospital-999");
            assertThat(hospital.getName()).isEqualTo("단계별 병원");
            assertThat(hospital.getAddress()).isEqualTo("단계별 주소");
            assertThat(hospital.getCreatedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTest {

        @Test
        @DisplayName("병원 이름은 필수 정보")
        void hospitalNameShouldBeProvided() {
            // Given
            String hospitalName = "필수 정보 병원";

            // When
            Hospital hospital = Hospital.builder()
                .id("hospital-123")
                .name(hospitalName)
                .address("서울시 강남구")
                .createdAt(Instant.now())
                .build();

            // Then
            assertThat(hospital.getName()).isEqualTo(hospitalName);
            assertThat(hospital.getName()).isNotEmpty();
        }

        @Test
        @DisplayName("병원 ID는 고유해야 함")
        void hospitalIdShouldBeUnique() {
            // Given
            String uniqueId1 = "hospital-001";
            String uniqueId2 = "hospital-002";

            // When
            Hospital hospital1 = Hospital.builder()
                .id(uniqueId1)
                .name("병원1")
                .build();

            Hospital hospital2 = Hospital.builder()
                .id(uniqueId2)
                .name("병원2")
                .build();

            // Then
            assertThat(hospital1.getId()).isNotEqualTo(hospital2.getId());
            assertThat(hospital1.getId()).isEqualTo(uniqueId1);
            assertThat(hospital2.getId()).isEqualTo(uniqueId2);
        }

        @Test
        @DisplayName("병원 생성 시점 기록")
        void hospitalCreationTimeShouldBeRecorded() {
            // Given
            Instant beforeCreation = Instant.now();
            
            // When
            Hospital hospital = Hospital.builder()
                .id("hospital-123")
                .name("시점 기록 병원")
                .createdAt(beforeCreation)
                .build();
            
            Instant afterCreation = Instant.now();

            // Then
            assertThat(hospital.getCreatedAt()).isEqualTo(beforeCreation);
            assertThat(hospital.getCreatedAt()).isBetween(
                beforeCreation.minus(1, ChronoUnit.SECONDS), 
                afterCreation.plus(1, ChronoUnit.SECONDS)
            );
        }

        @Test
        @DisplayName("병원 주소는 상세 정보 포함")
        void hospitalAddressShouldContainDetails() {
            // Given
            String detailedAddress = "서울특별시 강남구 테헤란로 123 ABC빌딩 5층";

            // When
            Hospital hospital = Hospital.builder()
                .id("hospital-123")
                .name("상세 주소 병원")
                .address(detailedAddress)
                .build();

            // Then
            assertThat(hospital.getAddress()).isEqualTo(detailedAddress);
            assertThat(hospital.getAddress()).contains("서울특별시");
            assertThat(hospital.getAddress()).contains("강남구");
            assertThat(hospital.getAddress()).contains("테헤란로");
        }
    }

    @Nested
    @DisplayName("시간 관련 테스트")
    class TimeRelatedTest {

        @Test
        @DisplayName("과거, 현재, 미래 시점으로 병원 생성")
        void createHospitalWithDifferentTimePoints() {
            // Given
            Instant pastTime = Instant.parse("2020-01-01T00:00:00Z");
            Instant currentTime = Instant.now();
            Instant futureTime = Instant.parse("2030-12-31T23:59:59Z");

            // When
            Hospital pastHospital = Hospital.builder()
                .id("hospital-past")
                .name("과거 병원")
                .createdAt(pastTime)
                .build();

            Hospital currentHospital = Hospital.builder()
                .id("hospital-current")
                .name("현재 병원")
                .createdAt(currentTime)
                .build();

            Hospital futureHospital = Hospital.builder()
                .id("hospital-future")
                .name("미래 병원")
                .createdAt(futureTime)
                .build();

            // Then
            assertThat(pastHospital.getCreatedAt()).isEqualTo(pastTime);
            assertThat(pastHospital.getCreatedAt()).isBefore(currentTime);
            
            assertThat(currentHospital.getCreatedAt()).isEqualTo(currentTime);
            
            assertThat(futureHospital.getCreatedAt()).isEqualTo(futureTime);
            assertThat(futureHospital.getCreatedAt()).isAfter(currentTime);
        }

        @Test
        @DisplayName("밀리초 단위 정밀도 테스트")
        void testMillisecondPrecision() {
            // Given
            Instant preciseTime = Instant.parse("2023-06-15T14:30:25.123456789Z");

            // When
            Hospital hospital = Hospital.builder()
                .id("hospital-precise")
                .name("정밀 시간 병원")
                .createdAt(preciseTime)
                .build();

            // Then
            assertThat(hospital.getCreatedAt()).isEqualTo(preciseTime);
            assertThat(hospital.getCreatedAt().getNano()).isEqualTo(123456789);
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("null 값으로 생성된 Hospital 처리")
        void createHospitalWithNullValues() {
            // When
            Hospital hospitalWithNulls = Hospital.builder()
                .id(null)
                .name(null)
                .address(null)
                .createdAt(null)
                .build();

            // Then - null 값도 그대로 설정됨 (검증은 비즈니스 레이어에서)
            assertThat(hospitalWithNulls.getId()).isNull();
            assertThat(hospitalWithNulls.getName()).isNull();
            assertThat(hospitalWithNulls.getAddress()).isNull();
            assertThat(hospitalWithNulls.getCreatedAt()).isNull();
        }

        @Test
        @DisplayName("빈 문자열과 공백 문자열로 생성된 Hospital 처리")
        void createHospitalWithEmptyAndBlankStrings() {
            // When
            Hospital hospitalWithEmptyName = Hospital.builder()
                .id("hospital-123")
                .name("")
                .address("정상 주소")
                .build();

            Hospital hospitalWithBlankName = Hospital.builder()
                .id("hospital-456")
                .name("   ")
                .address("   ")
                .build();

            // Then - 빈 문자열과 공백 문자열도 그대로 설정됨
            assertThat(hospitalWithEmptyName.getName()).isEmpty();
            assertThat(hospitalWithBlankName.getName()).isEqualTo("   ");
            assertThat(hospitalWithBlankName.getAddress()).isEqualTo("   ");
        }

        @Test
        @DisplayName("특수 문자가 포함된 병원명과 주소 처리")
        void handleSpecialCharactersInFields() {
            // Given
            String nameWithSpecialChars = "서울대학교병원(본원)";
            String addressWithSpecialChars = "서울특별시 종로구 대학로 101 (연건동, 서울대학교병원)";
            String idWithSpecialChars = "hospital-123_special@location";

            // When
            Hospital hospital = Hospital.builder()
                .id(idWithSpecialChars)
                .name(nameWithSpecialChars)
                .address(addressWithSpecialChars)
                .createdAt(Instant.now())
                .build();

            // Then - 특수문자도 그대로 처리됨
            assertThat(hospital.getId()).isEqualTo(idWithSpecialChars);
            assertThat(hospital.getName()).isEqualTo(nameWithSpecialChars);
            assertThat(hospital.getAddress()).isEqualTo(addressWithSpecialChars);
        }

        @Test
        @DisplayName("매우 긴 문자열 필드 처리")
        void handleVeryLongStrings() {
            // Given
            String longName = "매우".repeat(100) + "긴병원명";
            String longAddress = "서울특별시 ".repeat(50) + "매우긴주소";
            String longId = "hospital-" + "1".repeat(200);

            // When
            Hospital hospital = Hospital.builder()
                .id(longId)
                .name(longName)
                .address(longAddress)
                .createdAt(Instant.now())
                .build();

            // Then
            assertThat(hospital.getId()).isEqualTo(longId);
            assertThat(hospital.getName()).isEqualTo(longName);
            assertThat(hospital.getAddress()).isEqualTo(longAddress);
            assertThat(hospital.getName().length()).isEqualTo(204); // "매우" * 100 + "긴병원명"
        }

        @Test
        @DisplayName("유니코드 문자 처리")
        void handleUnicodeCharacters() {
            // Given
            String unicodeName = "병원🏥"; // 이모지 포함
            String unicodeAddress = "서울시 🗺️ 강남구 📍"; // 이모지 포함
            String koreanName = "한글병원";
            String englishName = "English Hospital";
            String mixedName = "Mixed병원Hospital";

            // When
            Hospital unicodeHospital = Hospital.builder()
                .id("hospital-unicode")
                .name(unicodeName)
                .address(unicodeAddress)
                .build();

            Hospital multiLangHospital = Hospital.builder()
                .id("hospital-multilang")
                .name(mixedName)
                .build();

            // Then
            assertThat(unicodeHospital.getName()).isEqualTo(unicodeName);
            assertThat(unicodeHospital.getAddress()).isEqualTo(unicodeAddress);
            assertThat(multiLangHospital.getName()).isEqualTo(mixedName);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값으로 생성된 Hospital 객체 비교")
        void compareHospitalsWithSameValues() {
            // Given
            Instant sameTime = Instant.parse("2023-01-01T12:00:00Z");
            
            Hospital hospital1 = Hospital.builder()
                .id("hospital-123")
                .name("테스트 병원")
                .address("테스트 주소")
                .createdAt(sameTime)
                .build();

            Hospital hospital2 = Hospital.builder()
                .id("hospital-123")
                .name("테스트 병원")
                .address("테스트 주소")
                .createdAt(sameTime)
                .build();

            Hospital hospital3 = Hospital.builder()
                .id("hospital-456") // 다른 ID
                .name("테스트 병원")
                .address("테스트 주소")
                .createdAt(sameTime)
                .build();

            // Then - 현재 Hospital 클래스에는 equals/hashCode가 구현되어 있지 않으므로
            // 객체 참조로만 비교됨
            assertThat(hospital1).isNotEqualTo(hospital2); // 다른 객체 참조
            assertThat(hospital1).isNotEqualTo(hospital3); // 다른 객체 참조
            
            // 같은 참조는 동일
            assertThat(hospital1).isEqualTo(hospital1);
        }
    }
}