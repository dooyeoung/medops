package com.medops.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@DisplayName("BusinessHour 도메인 모델 테스트")
class BusinessHourTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {

        @Test
        @DisplayName("모든 필드를 포함한 BusinessHour 생성 성공")
        void createBusinessHourWithAllFields() {
            // Given
            String id = "business-hour-123";
            Hospital hospital = Hospital.builder().id("hospital-123").name("테스트 병원").build();
            DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
            String openTime = "09:00";
            String closeTime = "18:00";
            String breakStartTime = "12:00";
            String breakEndTime = "13:00";
            boolean isClosed = false;

            // When
            BusinessHour businessHour = BusinessHour.builder()
                .id(id)
                .hospital(hospital)
                .dayOfWeek(dayOfWeek)
                .openTime(openTime)
                .closeTime(closeTime)
                .breakStartTime(breakStartTime)
                .breakEndTime(breakEndTime)
                .isClosed(isClosed)
                .build();

            // Then
            assertThat(businessHour.getId()).isEqualTo(id);
            assertThat(businessHour.getHospital()).isEqualTo(hospital);
            assertThat(businessHour.getDayOfWeek()).isEqualTo(dayOfWeek);
            assertThat(businessHour.getOpenTime()).isEqualTo(openTime);
            assertThat(businessHour.getCloseTime()).isEqualTo(closeTime);
            assertThat(businessHour.getBreakStartTime()).isEqualTo(breakStartTime);
            assertThat(businessHour.getBreakEndTime()).isEqualTo(breakEndTime);
            assertThat(businessHour.isClosed()).isEqualTo(isClosed);
        }

        @Test
        @DisplayName("AllArgsConstructor로 BusinessHour 생성")
        void createBusinessHourWithAllArgsConstructor() {
            // Given
            String id = "business-hour-456";
            Hospital hospital = Hospital.builder().id("hospital-456").name("다른 병원").build();
            DayOfWeek dayOfWeek = DayOfWeek.FRIDAY;
            String openTime = "08:30";
            String closeTime = "17:30";
            String breakStartTime = "12:30";
            String breakEndTime = "13:30";
            boolean isClosed = true;

            // When
            BusinessHour businessHour = new BusinessHour(
                id, hospital, dayOfWeek, openTime, closeTime, 
                breakStartTime, breakEndTime, isClosed
            );

            // Then
            assertThat(businessHour.getId()).isEqualTo(id);
            assertThat(businessHour.getHospital()).isEqualTo(hospital);
            assertThat(businessHour.getDayOfWeek()).isEqualTo(dayOfWeek);
            assertThat(businessHour.getOpenTime()).isEqualTo(openTime);
            assertThat(businessHour.getCloseTime()).isEqualTo(closeTime);
            assertThat(businessHour.getBreakStartTime()).isEqualTo(breakStartTime);
            assertThat(businessHour.getBreakEndTime()).isEqualTo(breakEndTime);
            assertThat(businessHour.isClosed()).isEqualTo(isClosed);
        }
    }

    @Nested
    @DisplayName("Builder 패턴 테스트")
    class BuilderPatternTest {

        @Test
        @DisplayName("toBuilder()로 기존 객체 기반 새 객체 생성")
        void createNewObjectUsingToBuilder() {
            // Given
            Hospital hospital = Hospital.builder().id("hospital-123").name("테스트 병원").build();
            BusinessHour original = BusinessHour.builder()
                .id("business-hour-123")
                .hospital(hospital)
                .dayOfWeek(DayOfWeek.MONDAY)
                .openTime("09:00")
                .closeTime("18:00")
                .isClosed(false)
                .build();

            // When - 휴무일로 변경
            BusinessHour modified = original.toBuilder()
                .isClosed(true)
                .openTime(null) // 휴무일이므로 영업시간 제거
                .closeTime(null)
                .build();

            // Then
            assertThat(modified.getId()).isEqualTo(original.getId());
            assertThat(modified.getHospital()).isEqualTo(original.getHospital());
            assertThat(modified.getDayOfWeek()).isEqualTo(original.getDayOfWeek());
            assertThat(modified.isClosed()).isTrue();
            assertThat(modified.getOpenTime()).isNull();
            assertThat(modified.getCloseTime()).isNull();
            
            // 원본은 변경되지 않음
            assertThat(original.isClosed()).isFalse();
            assertThat(original.getOpenTime()).isEqualTo("09:00");
        }

        @Test
        @DisplayName("Builder로 필수 필드만 설정하여 생성")
        void createWithRequiredFieldsOnly() {
            // When
            BusinessHour businessHour = BusinessHour.builder()
                .dayOfWeek(DayOfWeek.SUNDAY)
                .isClosed(true) // 일요일 휴무
                .build();

            // Then
            assertThat(businessHour.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            assertThat(businessHour.isClosed()).isTrue();
            assertThat(businessHour.getId()).isNull();
            assertThat(businessHour.getHospital()).isNull();
            assertThat(businessHour.getOpenTime()).isNull();
            assertThat(businessHour.getCloseTime()).isNull();
        }
    }

    @Nested
    @DisplayName("비즈니스 규칙 테스트")
    class BusinessRuleTest {

        @Test
        @DisplayName("평일 영업시간 설정")
        void setWeekdayBusinessHours() {
            // Given
            Hospital hospital = Hospital.builder().id("hospital-123").name("평일 병원").build();
            String openTime = "09:00";
            String closeTime = "18:00";
            String breakStart = "12:00";
            String breakEnd = "13:00";

            // When
            BusinessHour mondayHours = BusinessHour.builder()
                .hospital(hospital)
                .dayOfWeek(DayOfWeek.MONDAY)
                .openTime(openTime)
                .closeTime(closeTime)
                .breakStartTime(breakStart)
                .breakEndTime(breakEnd)
                .isClosed(false)
                .build();

            // Then
            assertThat(mondayHours.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
            assertThat(mondayHours.isClosed()).isFalse();
            assertThat(LocalTime.parse(mondayHours.getOpenTime()))
                .isBefore(LocalTime.parse(mondayHours.getCloseTime()));
            assertThat(LocalTime.parse(mondayHours.getBreakStartTime()))
                .isAfter(LocalTime.parse(mondayHours.getOpenTime()));
            assertThat(LocalTime.parse(mondayHours.getBreakEndTime()))
                .isBefore(LocalTime.parse(mondayHours.getCloseTime()));
            assertThat(LocalTime.parse(mondayHours.getBreakStartTime()))
                .isBefore(LocalTime.parse(mondayHours.getBreakEndTime()));
        }

        @Test
        @DisplayName("휴무일 설정")
        void setClosedDay() {
            // Given
            Hospital hospital = Hospital.builder().id("hospital-123").name("휴무 병원").build();

            // When
            BusinessHour sundayHours = BusinessHour.builder()
                .hospital(hospital)
                .dayOfWeek(DayOfWeek.SUNDAY)
                .isClosed(true)
                .build();

            // Then
            assertThat(sundayHours.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
            assertThat(sundayHours.isClosed()).isTrue();
            // 휴무일에는 영업시간이 설정되지 않을 수 있음
            assertThat(sundayHours.getOpenTime()).isNull();
            assertThat(sundayHours.getCloseTime()).isNull();
        }

        @Test
        @DisplayName("모든 요일에 대한 영업시간 설정 가능")
        void setBusinessHoursForAllDaysOfWeek() {
            // Given
            Hospital hospital = Hospital.builder().id("hospital-123").name("7일 병원").build();
            DayOfWeek[] allDays = DayOfWeek.values();

            // When & Then
            for (DayOfWeek day : allDays) {
                BusinessHour businessHour = BusinessHour.builder()
                    .hospital(hospital)
                    .dayOfWeek(day)
                    .openTime("09:00")
                    .closeTime("18:00")
                    .isClosed(false)
                    .build();

                assertThat(businessHour.getDayOfWeek()).isEqualTo(day);
                assertThat(businessHour.isClosed()).isFalse();
            }
        }

        @Test
        @DisplayName("점심시간 없는 영업시간 설정")
        void setBusinessHoursWithoutBreakTime() {
            // Given
            Hospital hospital = Hospital.builder().id("hospital-123").name("무휴 병원").build();

            // When
            BusinessHour noBreakHours = BusinessHour.builder()
                .hospital(hospital)
                .dayOfWeek(DayOfWeek.SATURDAY)
                .openTime("10:00")
                .closeTime("16:00")
                .isClosed(false)
                .build();

            // Then
            assertThat(noBreakHours.getBreakStartTime()).isNull();
            assertThat(noBreakHours.getBreakEndTime()).isNull();
            assertThat(noBreakHours.getOpenTime()).isEqualTo("10:00");
            assertThat(noBreakHours.getCloseTime()).isEqualTo("16:00");
        }
    }

    @Nested
    @DisplayName("시간 유효성 테스트")
    class TimeValidationTest {

        @Test
        @DisplayName("정상적인 시간 순서 - 오픈 < 점심시작 < 점심끝 < 마감")
        void validTimeSequence() {
            // Given
            String openTime = "09:00";
            String breakStart = "12:00";
            String breakEnd = "13:00";
            String closeTime = "18:00";

            // When
            BusinessHour businessHour = BusinessHour.builder()
                .dayOfWeek(DayOfWeek.TUESDAY)
                .openTime(openTime)
                .closeTime(closeTime)
                .breakStartTime(breakStart)
                .breakEndTime(breakEnd)
                .isClosed(false)
                .build();

            // Then - 시간 순서 확인
            assertThat(LocalTime.parse(businessHour.getOpenTime()))
                .isBefore(LocalTime.parse(businessHour.getBreakStartTime()));
            assertThat(LocalTime.parse(businessHour.getBreakStartTime()))
                .isBefore(LocalTime.parse(businessHour.getBreakEndTime()));
            assertThat(LocalTime.parse(businessHour.getBreakEndTime()))
                .isBefore(LocalTime.parse(businessHour.getCloseTime()));
        }

        @Test
        @DisplayName("잘못된 시간 순서로도 객체는 생성됨 - 비즈니스 로직에서 검증 필요")
        void invalidTimeSequenceStillCreatesObject() {
            // Given - 잘못된 시간 순서
            String openTime = "18:00";
            String closeTime = "09:00";
            String breakStart = "20:00";
            String breakEnd = "08:00";

            // When - 객체는 생성됨
            BusinessHour invalidHours = BusinessHour.builder()
                .dayOfWeek(DayOfWeek.WEDNESDAY)
                .openTime(openTime)
                .closeTime(closeTime)
                .breakStartTime(breakStart)
                .breakEndTime(breakEnd)
                .isClosed(false)
                .build();

            // Then - 잘못된 값도 그대로 저장됨 (비즈니스 로직에서 검증해야 함)
            assertThat(invalidHours.getOpenTime()).isEqualTo(openTime);
            assertThat(invalidHours.getCloseTime()).isEqualTo(closeTime);
            assertThat(invalidHours.getBreakStartTime()).isEqualTo(breakStart);
            assertThat(invalidHours.getBreakEndTime()).isEqualTo(breakEnd);
        }

        @Test
        @DisplayName("자정을 넘나드는 영업시간 처리")
        void handleOvernightBusinessHours() {
            // Given - 자정을 넘는 영업시간 (예: 응급실)
            String openTime = "22:00";
            String closeTime = "06:00";

            // When
            BusinessHour overnightHours = BusinessHour.builder()
                .dayOfWeek(DayOfWeek.FRIDAY)
                .openTime(openTime)
                .closeTime(closeTime)
                .isClosed(false)
                .build();

            // Then - 값은 저장되지만 로직에서는 날짜 고려 필요
            assertThat(overnightHours.getOpenTime()).isEqualTo(openTime);
            assertThat(overnightHours.getCloseTime()).isEqualTo(closeTime);
            assertThat(LocalTime.parse(overnightHours.getOpenTime()))
                .isBefore(LocalTime.parse(overnightHours.getCloseTime()));
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTest {

        @Test
        @DisplayName("null 병원으로 영업시간 생성")
        void createBusinessHourWithNullHospital() {
            // When
            BusinessHour businessHour = BusinessHour.builder()
                .hospital(null)
                .dayOfWeek(DayOfWeek.MONDAY)
                .openTime("09:00")
                .closeTime("18:00")
                .isClosed(false)
                .build();

            // Then
            assertThat(businessHour.getHospital()).isNull();
            assertThat(businessHour.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        }

        @Test
        @DisplayName("동일한 시간으로 설정된 영업시간")
        void createBusinessHourWithSameTimes() {
            // Given
            String sameTime = "12:00";

            // When
            BusinessHour sameTimes = BusinessHour.builder()
                .dayOfWeek(DayOfWeek.THURSDAY)
                .openTime(sameTime)
                .closeTime(sameTime)
                .breakStartTime(sameTime)
                .breakEndTime(sameTime)
                .isClosed(false)
                .build();

            // Then - 같은 시간도 설정 가능 (논리적으로는 문제가 있지만)
            assertThat(sameTimes.getOpenTime()).isEqualTo(sameTime);
            assertThat(sameTimes.getCloseTime()).isEqualTo(sameTime);
            assertThat(sameTimes.getBreakStartTime()).isEqualTo(sameTime);
            assertThat(sameTimes.getBreakEndTime()).isEqualTo(sameTime);
        }
    }
}