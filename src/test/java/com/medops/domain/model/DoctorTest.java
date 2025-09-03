package com.medops.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Doctor 도메인 모델 테스트")
class DoctorTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("모든 필드를 포함한 Doctor 생성 성공")
        void createDoctorWithAllFields() {
            // Given
            String id = UUID.randomUUID().toString();
            String name = "김의사";
            String hospitalId = UUID.randomUUID().toString();

            // When
            Doctor doctor = Doctor.builder()
                .id(id)
                .name(name)
                .hospitalId(hospitalId)
                .build();

            // Then
            assertThat(doctor.getId()).isEqualTo(id);
            assertThat(doctor.getName()).isEqualTo(name);
            assertThat(doctor.getHospitalId()).isEqualTo(hospitalId);
        }

        @Test
        @DisplayName("AllArgsConstructor로 Doctor 생성")
        void createDoctorWithAllArgsConstructor() {
            // Given
            String id = UUID.randomUUID().toString();
            String name = "이의사";
            String hospitalId = UUID.randomUUID().toString();

            // When
            Doctor doctor = new Doctor(id, name, hospitalId, Instant.now(), null);

            // Then
            assertThat(doctor.getId()).isEqualTo(id);
            assertThat(doctor.getName()).isEqualTo(name);
            assertThat(doctor.getHospitalId()).isEqualTo(hospitalId);
        }

        @Test
        @DisplayName("NoArgsConstructor로 생성 후 null 필드 확인")
        void createDoctorWithNoArgs() {
            // When
            Doctor doctor = new Doctor();

            // Then
            assertThat(doctor.getId()).isNull();
            assertThat(doctor.getName()).isNull();
            assertThat(doctor.getHospitalId()).isNull();
        }
    }

    @Nested
    @DisplayName("Builder 패턴 테스트")
    class BuilderPatternTest {

        @Test
        @DisplayName("Builder로 필수 필드만 설정하여 생성")
        void createWithRequiredFieldsOnly() {
            // When
            Doctor doctorWithNameOnly = Doctor.builder()
                .name("박의사")
                .build();

            Doctor doctorWithHospitalOnly = Doctor.builder()
                .hospitalId("hospital-789")
                .build();

            // Then
            assertThat(doctorWithNameOnly.getName()).isEqualTo("박의사");
            assertThat(doctorWithNameOnly.getId()).isNull();
            assertThat(doctorWithNameOnly.getHospitalId()).isNull();

            assertThat(doctorWithHospitalOnly.getHospitalId()).isEqualTo("hospital-789");
            assertThat(doctorWithHospitalOnly.getId()).isNull();
            assertThat(doctorWithHospitalOnly.getName()).isNull();
        }

        @Test
        @DisplayName("toBuilder()로 기존 객체 기반 새 객체 생성")
        void createNewObjectUsingToBuilder() {
            // Given
            Doctor original = Doctor.builder()
                .id(UUID.randomUUID().toString())
                .name("김의사")
                .hospitalId(UUID.randomUUID().toString())
                .build();

            // When - 이름만 변경
            Doctor modified = original.toBuilder()
                .name("김원장")
                .build();

            // Then
            assertThat(modified.getId()).isEqualTo(original.getId());
            assertThat(modified.getHospitalId()).isEqualTo(original.getHospitalId());
            assertThat(modified.getName()).isEqualTo("김원장");
            
            // 원본은 변경되지 않음
            assertThat(original.getName()).isEqualTo("김의사");
        }

        @Test
        @DisplayName("Builder로 모든 필드를 단계별로 설정")
        void buildObjectStepByStep() {
            // given
            String doctorId = UUID.randomUUID().toString();
            String hospitalId = UUID.randomUUID().toString();

            // When
            Doctor doctor = Doctor.builder()
                .id(doctorId)
                .name("최의사")
                .hospitalId(hospitalId)
                .build();

            // Then
            assertThat(doctor.getId()).isEqualTo(doctorId);
            assertThat(doctor.getName()).isEqualTo("최의사");
            assertThat(doctor.getHospitalId()).isEqualTo(hospitalId);
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTest {

        @Test
        @DisplayName("의사는 특정 병원에 소속되어야 함")
        void doctorShouldBelongToHospital() {
            // Given
            String hospitalId = UUID.randomUUID().toString();

            // When
            Doctor doctor = Doctor.builder()
                .id(UUID.randomUUID().toString())
                .name("김의사")
                .hospitalId(hospitalId)
                .build();

            // Then
            assertThat(doctor.getHospitalId()).isEqualTo(hospitalId);
            assertThat(doctor.getHospitalId()).isNotEmpty();
        }

        @Test
        @DisplayName("의사 이름은 필수 정보")
        void doctorNameShouldBeProvided() {
            // Given
            String doctorName = "이의사";

            // When
            Doctor doctor = Doctor.builder()
                .id(UUID.randomUUID().toString())
                .name(doctorName)
                .hospitalId(UUID.randomUUID().toString())
                .build();

            // Then
            assertThat(doctor.getName()).isEqualTo(doctorName);
            assertThat(doctor.getName()).isNotEmpty();
        }

        @Test
        @DisplayName("의사 ID는 고유해야 함")
        void doctorIdShouldBeUnique() {
            // Given
            String uniqueId1 = UUID.randomUUID().toString();
            String uniqueId2 = UUID.randomUUID().toString();

            // When
            Doctor doctor1 = Doctor.builder()
                .id(uniqueId1)
                .name("김의사")
                .hospitalId(UUID.randomUUID().toString())
                .build();

            Doctor doctor2 = Doctor.builder()
                .id(uniqueId2)
                .name("이의사")
                .hospitalId(UUID.randomUUID().toString())
                .build();

            // Then
            assertThat(doctor1.getId()).isNotEqualTo(doctor2.getId());
            assertThat(doctor1.getId()).isEqualTo(uniqueId1);
            assertThat(doctor2.getId()).isEqualTo(uniqueId2);
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("null 값으로 생성된 Doctor 처리")
        void createDoctorWithNullValues() {
            // When
            Doctor doctorWithNulls = Doctor.builder()
                .id(null)
                .name(null)
                .hospitalId(null)
                .build();

            // Then - null 값도 그대로 설정됨 (검증은 비즈니스 레이어에서)
            assertThat(doctorWithNulls.getId()).isNull();
            assertThat(doctorWithNulls.getName()).isNull();
            assertThat(doctorWithNulls.getHospitalId()).isNull();
        }

        @Test
        @DisplayName("빈 문자열과 공백 문자열로 생성된 Doctor 처리")
        void createDoctorWithEmptyAndBlankStrings() {
            // When
            Doctor doctorWithEmptyName = Doctor.builder()
                .id(UUID.randomUUID().toString())
                .name("")
                .hospitalId(UUID.randomUUID().toString())
                .build();

            Doctor doctorWithBlankName = Doctor.builder()
                .id(UUID.randomUUID().toString())
                .name("   ")
                .hospitalId(UUID.randomUUID().toString())
                .build();

            Doctor doctorWithEmptyHospitalId = Doctor.builder()
                .id(UUID.randomUUID().toString())
                .name("김의사")
                .hospitalId("")
                .build();

            // Then - 빈 문자열과 공백 문자열도 그대로 설정됨
            assertThat(doctorWithEmptyName.getName()).isEmpty();
            assertThat(doctorWithBlankName.getName()).isEqualTo("   ");
            assertThat(doctorWithEmptyHospitalId.getHospitalId()).isEmpty();
        }

        @Test
        @DisplayName("특수 문자가 포함된 이름과 ID 처리")
        void handleSpecialCharactersInFields() {
            // Given
            String nameWithSpecialChars = "김의사-전문의(내과)";
            String idWithSpecialChars = "doctor-123_special@hospital";
            String hospitalIdWithSpecialChars = "hospital-999#branch-A";

            // When
            Doctor doctor = Doctor.builder()
                .id(idWithSpecialChars)
                .name(nameWithSpecialChars)
                .hospitalId(hospitalIdWithSpecialChars)
                .build();

            // Then - 특수문자도 그대로 처리됨
            assertThat(doctor.getId()).isEqualTo(idWithSpecialChars);
            assertThat(doctor.getName()).isEqualTo(nameWithSpecialChars);
            assertThat(doctor.getHospitalId()).isEqualTo(hospitalIdWithSpecialChars);
        }
    }

    @Nested
    @DisplayName("동등성 테스트")
    class EqualityTest {

        @Test
        @DisplayName("같은 값으로 생성된 Doctor 객체 비교")
        void compareDoctorsWithSameValues() {
            // Given
            Doctor doctor1 = Doctor.builder()
                .id("doctor-123")
                .name("김의사")
                .hospitalId("hospital-123")
                .build();

            Doctor doctor2 = Doctor.builder()
                .id("doctor-123")
                .name("김의사")
                .hospitalId("hospital-123")
                .build();

            Doctor doctor3 = Doctor.builder()
                .id("doctor-456") // 다른 ID
                .name("김의사")
                .hospitalId("hospital-123")
                .build();

            // Then - 현재 Doctor 클래스에는 equals/hashCode가 구현되어 있지 않으므로
            // 객체 참조로만 비교됨
            assertThat(doctor1).isNotEqualTo(doctor2); // 다른 객체 참조
            assertThat(doctor1).isNotEqualTo(doctor3); // 다른 객체 참조
            
            // 같은 참조는 동일
            assertThat(doctor1).isEqualTo(doctor1);
        }
    }
}