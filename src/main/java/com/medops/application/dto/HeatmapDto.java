package com.medops.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapDto {
    private int dayOfWeek; // 0=일요일, 1=월요일, ... 6=토요일
    private int hour; // 0-23
    private int count; // 해당 시간대 예약 건수

    public static List<HeatmapDto> createSampleData() {
        // 샘플 데이터 생성 (실제 구현에서는 DB에서 조회)
        return List.of(
            new HeatmapDto(1, 9, 3),  // 월요일 9시 3건
            new HeatmapDto(1, 10, 5), // 월요일 10시 5건
            new HeatmapDto(1, 14, 4), // 월요일 14시 4건
            new HeatmapDto(2, 9, 2),  // 화요일 9시 2건
            new HeatmapDto(2, 11, 7), // 화요일 11시 7건
            new HeatmapDto(3, 15, 6), // 수요일 15시 6건
            new HeatmapDto(4, 10, 4), // 목요일 10시 4건
            new HeatmapDto(5, 16, 8), // 금요일 16시 8건
            new HeatmapDto(6, 14, 3)  // 토요일 14시 3건
        );
    }
}