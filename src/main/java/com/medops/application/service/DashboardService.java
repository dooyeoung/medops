package com.medops.application.service;

import com.medops.adapter.out.persistence.mongodb.document.MedicalRecordViewDocument;
import com.medops.application.dto.DashboardStatsDto;
import com.medops.application.dto.DashboardTrendsDto;
import com.medops.application.dto.DoctorStatsDto;
import com.medops.application.dto.DoctorTreatmentStatsDto;
import com.medops.application.dto.HeatmapDto;
import com.medops.application.port.in.usecase.DashboardUseCase;
import com.medops.application.port.in.usecase.MedicalRecordViewUseCase;
import com.medops.application.port.out.LoadTreatmentProductPort;
import com.medops.domain.enums.MedicalRecordStatus;
import com.medops.domain.model.TreatmentProduct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardService implements DashboardUseCase {
    
    private final MedicalRecordViewUseCase medicalRecordViewUseCase;
    private final LoadTreatmentProductPort loadTreatmentProductPort;

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    
    // 공통 유틸리티 메서드들
    
    /**
     * 오늘 날짜 범위 계산 (00:00:00 ~ 23:59:59.999)
     */
    private Instant[] getTodayRange() {
        Instant startOfToday = LocalDate.now(SEOUL_ZONE)
            .atStartOfDay(SEOUL_ZONE)
            .toInstant();
        Instant endOfToday = LocalDate.now(SEOUL_ZONE)
            .atTime(23, 59, 59, 999_999_999)
            .atZone(SEOUL_ZONE)
            .toInstant();
        return new Instant[]{startOfToday, endOfToday};
    }
    
    /**
     * 이번 달 날짜 범위 계산 (1일 00:00:00 ~ 마지막일 23:59:59.999)
     */
    private Instant[] getThisMonthRange() {
        Instant startOfMonth = LocalDate.now(SEOUL_ZONE).withDayOfMonth(1)
            .atStartOfDay(SEOUL_ZONE).toInstant();
        LocalDate currentMonth = LocalDate.now(SEOUL_ZONE);
        LocalDate lastDayOfMonth = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());
        Instant endOfMonth = lastDayOfMonth
            .atTime(23, 59, 59, 999_999_999)
            .atZone(SEOUL_ZONE)
            .toInstant();
        return new Instant[]{startOfMonth, endOfMonth};
    }
    
    /**
     * N일간 날짜 범위 계산 (N일 전 00:00:00 ~ 오늘 23:59:59.999)
     */
    private Instant[] getDaysRange(int days) {
        LocalDate endDate = LocalDate.now(SEOUL_ZONE);
        LocalDate startDate = endDate.minusDays(days - 1);
        
        Instant startInstant = startDate.atStartOfDay(SEOUL_ZONE).toInstant();
        Instant endInstant = endDate.atTime(23, 59, 59, 999_999_999).atZone(SEOUL_ZONE).toInstant();
        
        return new Instant[]{startInstant, endInstant};
    }
    
    /**
     * 예약 상태별 카운팅 결과를 담는 내부 클래스
     */
    private record StatusCounts(
        int total,
        int pending,
        int confirmed,
        int canceled,
        int completed
    ) {}
    
    /**
     * 예약 리스트에서 상태별 카운팅 수행
     */
    private StatusCounts countByStatus(List<MedicalRecordViewDocument> records) {
        int total = records.size();
        int pending = (int) records.stream()
            .filter(r -> r.getStatus() == MedicalRecordStatus.PENDING)
            .count();
        int confirmed = (int) records.stream()
            .filter(r -> r.getStatus() == MedicalRecordStatus.RESERVED)
            .count();
        int canceled = (int) records.stream()
            .filter(r -> r.getStatus() == MedicalRecordStatus.CANCELED)
            .count();
        int completed = (int) records.stream()
            .filter(r -> r.getStatus() == MedicalRecordStatus.COMPLETED)
            .count();
        
        return new StatusCounts(total, pending, confirmed, canceled, completed);
    }
    
    
    /**
     * 실제 시술 가격을 기반으로 매출 계산
     */
    private long calculateActualRevenue(String hospitalId, List<MedicalRecordViewDocument> confirmedRecords) {
        if (confirmedRecords.isEmpty()) {
            return 0L;
        }
        
        // 시술별 가격 정보 조회 (캐시 최적화 가능)
        List<TreatmentProduct> treatments = loadTreatmentProductPort.loadTreatmentProductsByHospitalId(hospitalId);
        Map<String, BigDecimal> priceMap = treatments.stream()
            .collect(Collectors.toMap(
                TreatmentProduct::getName,
                TreatmentProduct::getPrice,
                (existing, replacement) -> existing // 중복 시 기존 값 유지
            ));
        
        // 확정된 예약의 실제 매출 합계 계산
        return confirmedRecords.stream()
            .mapToLong(record -> {
                BigDecimal price = priceMap.getOrDefault(
                    record.getTreatmentProductName(), 
                    BigDecimal.valueOf(150_000L) // 기본값 (시술 상품이 없는 경우)
                );
                return price.longValue();
            })
            .sum();
    }
    
    @Override
    public DashboardStatsDto getDashboardStats(String hospitalId) {
        // 오늘 데이터
        Instant[] todayRange = getTodayRange();
        List<MedicalRecordViewDocument> todayRecords = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, todayRange[0], todayRange[1]);

        // 최근 30일 데이터 (더 일관성 있는 성과 측정)
        Instant[] monthRange = getDaysRange(30);
        List<MedicalRecordViewDocument> monthRecords = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, monthRange[0], monthRange[1]);

        return DashboardStatsDto.builder()
            .today(calculateTodayStats(hospitalId, todayRecords))
            .thisMonth(calculateMonthStats(hospitalId, monthRecords))
            .performance(calculatePerformanceStats(monthRecords))
            .build();
    }
    
    @Override
    public DashboardTrendsDto getDashboardTrends(String hospitalId, int days) {
        Instant[] range = getDaysRange(days);
        List<MedicalRecordViewDocument> records = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, range[0], range[1]);
        
        return DashboardTrendsDto.builder()
            .dailyTrends(calculateDailyTrends(hospitalId, records, days))
            .timeSlotDistribution(List.of()) // 현재 사용되지 않음
            .treatmentPopularity(List.of()) // 현재 사용되지 않음 - 의사별 시술 API 사용
            .build();
    }

    @Override
    public DashboardStatsDto.TodayStats getRealTimeSummary(String hospitalId) {
        Instant[] range = getTodayRange();
        List<MedicalRecordViewDocument> todayRecords = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, range[0], range[1]);
        
        return calculateTodayStats(hospitalId, todayRecords);
    }

    @Override
    public List<HeatmapDto> getHeatmapData(String hospitalId, int days) {
        Instant[] range = getDaysRange(days);
        List<MedicalRecordViewDocument> records = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, range[0], range[1]);
        
        return calculateHeatmapData(records);
    }

    @Override
    public List<DoctorStatsDto> getDoctorStats(String hospitalId, int days) {
        Instant[] range = getDaysRange(days);
        List<MedicalRecordViewDocument> records = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, range[0], range[1]);
        
        return calculateDoctorStats(hospitalId, records);
    }
    
    private DashboardStatsDto.TodayStats calculateTodayStats(String hospitalId, List<MedicalRecordViewDocument> records) {
        StatusCounts counts = countByStatus(records);
        
        // 확정된 예약과 완료된 예약을 매출로 계산
        List<MedicalRecordViewDocument> revenueRecords = records.stream()
            .filter(r -> r.getStatus() == MedicalRecordStatus.RESERVED || r.getStatus() == MedicalRecordStatus.COMPLETED)
            .collect(Collectors.toList());
        
        long revenue = calculateActualRevenue(hospitalId, revenueRecords);
        
        return DashboardStatsDto.TodayStats.builder()
            .total(counts.total())
            .pending(counts.pending())
            .confirmed(counts.confirmed())
            .canceled(counts.canceled())
            .completed(counts.completed())
            .revenue(revenue)
            .build();
    }
    
    private DashboardStatsDto.MonthStats calculateMonthStats(String hospitalId, List<MedicalRecordViewDocument> records) {
        int totalBookings = records.size();
        int completedTreatments = (int) records.stream()
            .filter(r -> r.getStatus() == MedicalRecordStatus.RESERVED)
            .count();
        
        // 확정된 예약과 완료된 예약을 매출로 계산
        List<MedicalRecordViewDocument> revenueRecords = records.stream()
            .filter(r -> r.getStatus() == MedicalRecordStatus.RESERVED || r.getStatus() == MedicalRecordStatus.COMPLETED)
            .collect(Collectors.toList());
        
        long revenue = calculateActualRevenue(hospitalId, revenueRecords);
        
        // 고객 분석 (간단한 근사치)
        int uniqueCustomers = records.stream()
            .collect(Collectors.groupingBy(MedicalRecordViewDocument::getUserId))
            .size();
        int newCustomers = (int) (uniqueCustomers * 0.6); // 60% 신규 고객으로 가정
        int returningCustomers = uniqueCustomers - newCustomers;
        
        return DashboardStatsDto.MonthStats.builder()
            .totalBookings(totalBookings)
            .completedTreatments(completedTreatments)
            .revenue(revenue)
            .newCustomers(newCustomers)
            .returningCustomers(returningCustomers)
            .build();
    }
    
    private DashboardStatsDto.PerformanceStats calculatePerformanceStats(List<MedicalRecordViewDocument> records) {
        if (records.isEmpty()) {
            return DashboardStatsDto.PerformanceStats.builder()
                .confirmationRate(0.0)
                .cancellationRate(0.0)
                .avgDailyBookings(0.0)
                .noShowRate(0.0)
                .build();
        }
        
        int total = records.size();
        int confirmed = (int) records.stream()
            .filter(r -> r.getStatus() == MedicalRecordStatus.RESERVED)
            .count();
        int canceled = (int) records.stream()
            .filter(r -> r.getStatus() == MedicalRecordStatus.CANCELED)
            .count();
        
        double confirmationRate = (confirmed * 100.0) / total;
        double cancellationRate = (canceled * 100.0) / total;
        double avgDailyBookings = total / 30.0; // 한달 기준
        double noShowRate = 5.0; // 고정값 (실제로는 별도 추적 필요)
        
        return DashboardStatsDto.PerformanceStats.builder()
            .confirmationRate(Math.round(confirmationRate * 100.0) / 100.0)
            .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
            .avgDailyBookings(Math.round(avgDailyBookings * 10.0) / 10.0)
            .noShowRate(noShowRate)
            .build();
    }
    
    private List<DashboardTrendsDto.DailyTrendData> calculateDailyTrends(String hospitalId, List<MedicalRecordViewDocument> records, int days) {
        Map<LocalDate, List<MedicalRecordViewDocument>> recordsByDate = records.stream()
            .collect(Collectors.groupingBy(r -> 
                LocalDateTime.ofInstant(r.getStartTime(), SEOUL_ZONE).toLocalDate()
            ));
        
        return IntStream.range(0, days)
            .mapToObj(i -> {
                LocalDate date = LocalDate.now(SEOUL_ZONE).minusDays(days - 1 - i);
                List<MedicalRecordViewDocument> dayRecords = recordsByDate.getOrDefault(date, List.of());
                
                StatusCounts counts = countByStatus(dayRecords);
                
                // 확정된 예약과 완료된 예약을 매출로 계산
                List<MedicalRecordViewDocument> revenueRecords = dayRecords.stream()
                    .filter(r -> r.getStatus() == MedicalRecordStatus.RESERVED || r.getStatus() == MedicalRecordStatus.COMPLETED)
                    .collect(Collectors.toList());
                
                long revenue = calculateActualRevenue(hospitalId, revenueRecords);
                
                return DashboardTrendsDto.DailyTrendData.builder()
                    .date(date)
                    .totalReservations(counts.total())
                    .pendingReservations(counts.pending())
                    .confirmedReservations(counts.confirmed())
                    .canceledReservations(counts.canceled())
                    .completedReservations(counts.completed())
                    .revenue(revenue)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private List<HeatmapDto> calculateHeatmapData(List<MedicalRecordViewDocument> records) {
        // 요일별/시간대별 예약 건수 집계
        Map<String, Integer> heatmapMap = records.stream()
            .collect(Collectors.groupingBy(
                record -> {
                    LocalDateTime dateTime = LocalDateTime.ofInstant(record.getStartTime(), SEOUL_ZONE);
                    int dayOfWeek = dateTime.getDayOfWeek().getValue() % 7; // 월요일=1 -> 일요일=0, 월요일=1, ...
                    int hour = dateTime.getHour();
                    return dayOfWeek + "_" + hour;
                },
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));

        // 모든 요일(0-6)과 시간(7-22)의 조합에 대해 HeatmapDto 생성
        return IntStream.rangeClosed(0, 6) // 일요일(0) ~ 토요일(6)
            .boxed()
            .flatMap(dayOfWeek -> 
                IntStream.rangeClosed(7, 22) // 07:00 ~ 22:00
                    .mapToObj(hour -> {
                        String key = dayOfWeek + "_" + hour;
                        int count = heatmapMap.getOrDefault(key, 0);
                        return new HeatmapDto(dayOfWeek, hour, count);
                    })
            )
            .filter(dto -> dto.getCount() > 0) // 예약이 있는 시간대만 반환
            .collect(Collectors.toList());
    }

    private List<DoctorStatsDto> calculateDoctorStats(String hospitalId, List<MedicalRecordViewDocument> records) {
        if (records.isEmpty()) {
            return List.of();
        }
        
        // 의사별로 예약 기록을 그룹화
        Map<String, List<MedicalRecordViewDocument>> recordsByDoctor = records.stream()
            .filter(record -> record.getDoctorId() != null && !record.getDoctorId().isEmpty())
            .collect(Collectors.groupingBy(MedicalRecordViewDocument::getDoctorId));
        
        return recordsByDoctor.entrySet().stream()
            .map(entry -> {
                String doctorId = entry.getKey();
                List<MedicalRecordViewDocument> doctorRecords = entry.getValue();
                
                // 의사명은 첫 번째 레코드에서 가져오기 (동일한 의사의 모든 레코드는 동일한 이름을 가짐)
                String doctorName = doctorRecords.isEmpty() ? "Unknown" : doctorRecords.get(0).getDoctorName();
                
                // 상태별 예약 수 계산
                StatusCounts counts = countByStatus(doctorRecords);
                
                // 확정된 예약과 완료된 예약을 매출로 계산
                List<MedicalRecordViewDocument> revenueRecords = doctorRecords.stream()
                    .filter(r -> r.getStatus() == MedicalRecordStatus.RESERVED || r.getStatus() == MedicalRecordStatus.COMPLETED)
                    .collect(Collectors.toList());
                
                long revenue = calculateActualRevenue(hospitalId, revenueRecords);
                
                // 확정률 계산
                double confirmationRate = counts.total() > 0 
                    ? Math.round((counts.confirmed() * 100.0 / counts.total()) * 100.0) / 100.0
                    : 0.0;
                
                return DoctorStatsDto.builder()
                    .doctorId(doctorId)
                    .doctorName(doctorName)
                    .totalReservations(counts.total())
                    .confirmedReservations(counts.confirmed())
                    .pendingReservations(counts.pending())
                    .canceledReservations(counts.canceled())
                    .completedReservations(counts.completed())
                    .revenue(revenue)
                    .confirmationRate(confirmationRate)
                    .build();
            })
            .sorted((a, b) -> Integer.compare(b.totalReservations(), a.totalReservations())) // 총 예약 수 기준 내림차순 정렬
            .collect(Collectors.toList());
    }

    @Override
    public List<DoctorTreatmentStatsDto> getDoctorTreatmentStats(String hospitalId, int days) {
        // 해당 병원의 모든 시술 상품 조회
        List<TreatmentProduct> allTreatments = loadTreatmentProductPort.loadTreatmentProductsByHospitalId(hospitalId);
        
        // 기간 내 예약 데이터 조회
        Instant[] range = getDaysRange(days);
        List<MedicalRecordViewDocument> records = medicalRecordViewUseCase
            .getMedicalRecordsByHospital(hospitalId, range[0], range[1]);
        
        // 의사별로 그룹화
        Map<String, List<MedicalRecordViewDocument>> recordsByDoctor = records.stream()
            .filter(record -> record.getDoctorId() != null && !record.getDoctorId().isEmpty())
            .collect(Collectors.groupingBy(MedicalRecordViewDocument::getDoctorId));
        
        return recordsByDoctor.entrySet().stream()
            .map(entry -> {
                String doctorId = entry.getKey();
                List<MedicalRecordViewDocument> doctorRecords = entry.getValue();
                String doctorName = doctorRecords.isEmpty() ? "Unknown" : doctorRecords.get(0).getDoctorName();
                
                // 해당 의사의 시술별 통계 계산
                List<DoctorTreatmentStatsDto.TreatmentStatsDto> treatmentStats = allTreatments.stream()
                    .map(treatment -> {
                        // 해당 시술의 예약 수 계산
                        int reservationCount = (int) doctorRecords.stream()
                            .filter(record -> treatment.getName().equals(record.getTreatmentProductName()))
                            .count();
                        
                        // 해당 시술의 매출 계산 (확정된 예약과 완료된 예약)
                        List<MedicalRecordViewDocument> treatmentRevenueRecords = doctorRecords.stream()
                            .filter(record -> treatment.getName().equals(record.getTreatmentProductName()))
                            .filter(record -> record.getStatus() == MedicalRecordStatus.RESERVED || record.getStatus() == MedicalRecordStatus.COMPLETED)
                            .collect(Collectors.toList());
                        
                        long revenue = treatmentRevenueRecords.stream()
                            .mapToLong(record -> treatment.getPrice().longValue())
                            .sum();
                        
                        return DoctorTreatmentStatsDto.TreatmentStatsDto.builder()
                            .treatmentName(treatment.getName())
                            .reservationCount(reservationCount)
                            .revenue(revenue)
                            .build();
                    })
                    .collect(Collectors.toList());
                
                return DoctorTreatmentStatsDto.builder()
                    .doctorId(doctorId)
                    .doctorName(doctorName)
                    .treatmentStats(treatmentStats)
                    .build();
            })
            .sorted((a, b) -> {
                int totalA = a.treatmentStats().stream().mapToInt(DoctorTreatmentStatsDto.TreatmentStatsDto::reservationCount).sum();
                int totalB = b.treatmentStats().stream().mapToInt(DoctorTreatmentStatsDto.TreatmentStatsDto::reservationCount).sum();
                return Integer.compare(totalB, totalA); // 총 예약 수 기준 내림차순 정렬
            })
            .collect(Collectors.toList());
    }
}