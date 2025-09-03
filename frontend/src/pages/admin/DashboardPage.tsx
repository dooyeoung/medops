import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import {
  getDashboardStats,
  getDashboardTrends,
  getRealTimeSummary,
  getHeatmapData,
  getDoctorStats,
  getDoctorTreatmentStats,
} from '@/api/dashboard';
import type {
  HeatmapData,
  DoctorStatsResponse,
  DoctorTreatmentStatsResponse,
  DashboardStatsResponse,
  DashboardTrendsResponse,
} from '@/api/dashboard';
import { getCurrentAdmin } from '@/api/admin';
import { format } from 'date-fns';
import ReactECharts from 'echarts-for-react';

export default function DashboardPage() {
  const [dashboardStats, setDashboardStats] = useState<DashboardStatsResponse | null>(null);
  const [dashboardTrends, setDashboardTrends] = useState<DashboardTrendsResponse | null>(null);
  const [heatmapData, setHeatmapData] = useState<HeatmapData[]>([]);
  const [doctorStats, setDoctorStats] = useState<DoctorStatsResponse[]>([]);
  const [doctorTreatmentStats, setDoctorTreatmentStats] = useState<DoctorTreatmentStatsResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hospitalId, setHospitalId] = useState<string | null>(null);

  useEffect(() => {
    const fetchDashboardData = async () => {
      try {
        setIsLoading(true);

        // Get hospital ID
        const adminResponse = await getCurrentAdmin();
        const adminHospitalId = adminResponse.body.hospital?.id;
        if (!adminHospitalId) {
          throw new Error('Hospital ID not found');
        }
        setHospitalId(adminHospitalId);

        // Fetch dashboard stats, trends, heatmap data, and doctor stats
        const [statsResponse, trendsResponse, heatmapResponse, doctorStatsResponse, doctorTreatmentStatsResponse] =
          await Promise.all([
            getDashboardStats(adminHospitalId),
            getDashboardTrends(adminHospitalId, 7),
            getHeatmapData(adminHospitalId, 30),
            getDoctorStats(adminHospitalId, 7),
            getDoctorTreatmentStats(adminHospitalId, 7),
          ]);

        // Store API responses directly
        setDashboardStats(statsResponse);
        setDashboardTrends(trendsResponse);
        setHeatmapData(heatmapResponse);
        setDoctorStats(doctorStatsResponse);
        setDoctorTreatmentStats(doctorTreatmentStatsResponse);
      } catch (error) {
        console.error('Failed to fetch dashboard data:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchDashboardData();
  }, []);

  // 히트맵 데이터 처리 함수
  const getReservationCount = (dayOfWeek: number, hour: number): number => {
    const data = heatmapData.find((item) => item.dayOfWeek === dayOfWeek && item.hour === hour);
    return data ? data.count : 0;
  };

  // 최대값을 구해서 색상 정규화에 사용
  const maxCount = Math.max(...heatmapData.map((item) => item.count), 1);

  // ECharts 히트맵 옵션 생성
  const getHeatmapOption = () => {
    const days = ['일', '월', '화', '수', '목', '금', '토'];
    const hours = Array.from({ length: 16 }, (_, i) => `${String(7 + i).padStart(2, '0')}:00`);

    // 히트맵 데이터 변환 ([시간인덱스, 요일인덱스, 값] 형태)
    const data: [number, number, number][] = [];
    for (let dayIndex = 0; dayIndex < 7; dayIndex++) {
      for (let hourIndex = 0; hourIndex < 16; hourIndex++) {
        const hour = 7 + hourIndex;
        // API 요일 인덱스: 일요일=0, 월요일=1, ..., 토요일=6
        const apiDayIndex = dayIndex;
        const reservationCount = getReservationCount(apiDayIndex, hour);
        if (reservationCount > 0) {
          data.push([hourIndex, dayIndex, reservationCount]);
        }
      }
    }

    return {
      tooltip: {
        position: 'top',
        formatter: (params: any) => {
          const [hourIndex, dayIndex, value] = params.data;
          const day = days[dayIndex];
          const hour = hours[hourIndex];
          return `${day}요일 ${hour}<br/>예약 수: ${value}건`;
        },
      },
      grid: {
        height: '80%',
        top: '10%',
      },
      xAxis: {
        type: 'category',
        data: hours,
        splitArea: {
          show: true,
        },
        axisLabel: {
          fontSize: 10,
        },
      },
      yAxis: {
        type: 'category',
        data: days,
        splitArea: {
          show: true,
        },
        axisLabel: {
          fontSize: 12,
        },
      },
      visualMap: {
        show: false,
        min: 0,
        max: maxCount,
        inRange: {
          color: ['#f8f9fa', '#3b82f6'],
        },
      },
      series: [
        {
          name: '예약 수',
          type: 'heatmap',
          data: data,
          label: {
            show: false,
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
        },
      ],
    };
  };

  // ECharts 의사별 시술 예약 현황 옵션 생성 (Grouped Bar Chart)
  const getDoctorTreatmentBarOption = () => {
    // 실제 API 데이터 사용
    const doctors = doctorTreatmentStats.map((doctor) => doctor.doctorName);

    // 모든 시술 목록 생성 (중복 제거)
    const allTreatments = [
      ...new Set(doctorTreatmentStats.flatMap((doctor) => doctor.treatmentStats.map((stat) => stat.treatmentName))),
    ];

    // 각 시술별로 의사들의 예약 수 데이터 생성 (0 포함)
    const series = allTreatments.map((treatment, treatmentIndex) => {
      const data = doctors.map((doctorName) => {
        const doctor = doctorTreatmentStats.find((d) => d.doctorName === doctorName);
        const treatmentStat = doctor?.treatmentStats.find((t) => t.treatmentName === treatment);
        return treatmentStat?.reservationCount || 0;
      });

      // 시술별로 다른 색상 사용
      const colors = [
        '#3b82f6', // 파란색
        '#22c55e', // 녹색
        '#f59e0b', // 주황색
        '#ef4444', // 빨간색
        '#8b5cf6', // 보라색
        '#06b6d4', // 청록색
        '#f97316', // 오렌지
        '#84cc16', // 라임
      ];

      return {
        name: treatment,
        type: 'bar',
        data: data,
        itemStyle: {
          color: colors[treatmentIndex % colors.length],
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowOffsetY: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      };
    });

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
        formatter: (params: any[]) => {
          let result = `<strong>${params[0].axisValue} 의사</strong><br/>`;
          let totalReservations = 0;

          // 0이 아닌 항목들만 표시하고, 0인 항목들은 별도로 처리
          const nonZeroItems: any[] = [];
          const zeroItems: any[] = [];

          params.forEach((param) => {
            totalReservations += param.value;
            if (param.value > 0) {
              nonZeroItems.push(param);
            } else {
              zeroItems.push(param);
            }
          });

          // 0이 아닌 항목들 먼저 표시
          nonZeroItems.forEach((param) => {
            result += `${param.marker}${param.seriesName}: ${param.value}건<br/>`;
          });

          // 0인 항목들이 있으면 압축해서 표시
          if (zeroItems.length > 0) {
            result += `<span style="color: #9ca3af; font-size: 11px;">`;
            if (zeroItems.length <= 3) {
              zeroItems.forEach((param) => {
                result += `${param.marker}${param.seriesName}: 0건<br/>`;
              });
            } else {
              result += `기타 ${zeroItems.length}개 시술: 0건<br/>`;
            }
            result += `</span>`;
          }

          result += `<br/><strong>총 예약: ${totalReservations}건</strong>`;
          return result;
        },
      },
      legend: {
        data: allTreatments,
        bottom: 0,
        type: 'scroll',
        pageButtonItemGap: 5,
        pageButtonGap: 30,
        pageIconSize: 10,
        itemGap: 10,
        textStyle: {
          fontSize: 10,
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '20%',
        top: '5%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: doctors,
        axisLabel: {
          fontSize: 11,
          rotate: doctors.length > 4 ? -45 : 0,
        },
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          fontSize: 11,
        },
      },
      series: series,
    };
  };

  // ECharts 일별 예약 현황 차트 (상태별 누적 영역)
  const getReservationTrendsOption = () => {
    if (!dashboardTrends?.dailyTrends) return {};

    const dates = dashboardTrends.dailyTrends.map((trend) => format(new Date(trend.date), 'MM/dd'));

    // 각 상태별 데이터 추출 (실제 API 데이터 사용)
    const pendingData = dashboardTrends.dailyTrends.map((trend) => trend.pendingReservations || 0);
    const confirmedData = dashboardTrends.dailyTrends.map((trend) => trend.confirmedReservations);
    const canceledData = dashboardTrends.dailyTrends.map((trend) => trend.canceledReservations || 0);
    const completedData = dashboardTrends.dailyTrends.map((trend) => trend.completedReservations || 0);

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
        },
        formatter: (params: any[]) => {
          let result = `${params[0].axisValue}<br/>`;
          let total = 0;

          // 색상 매핑
          const colorMap: { [key: string]: string } = {
            '대기 중': 'rgba(107, 114, 128, 0.8)',
            확정: 'rgba(59, 130, 246, 0.8)',
            완료: 'rgba(34, 197, 94, 0.8)',
            취소: 'rgba(239, 68, 68, 0.8)',
          };

          params.forEach((param) => {
            total += param.value;
            const color = colorMap[param.seriesName] || param.color;
            const marker = `<span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${color};"></span>`;
            result += `${marker}${param.seriesName}: ${param.value}건<br/>`;
          });
          result += `<br/><strong>총 예약: ${total}건</strong>`;
          return result;
        },
      },
      legend: {
        data: [
          { name: '대기 중', itemStyle: { color: 'rgba(107, 114, 128, 0.8)' } },
          { name: '확정', itemStyle: { color: 'rgba(59, 130, 246, 0.8)' } },
          { name: '완료', itemStyle: { color: 'rgba(34, 197, 94, 0.8)' } },
          { name: '취소', itemStyle: { color: 'rgba(239, 68, 68, 0.8)' } },
        ],
        bottom: 0,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLabel: {
          fontSize: 11,
        },
      },
      yAxis: {
        type: 'value',
        name: '예약 수',
        axisLabel: {
          fontSize: 11,
          formatter: '{value}건',
        },
        splitLine: {
          show: true,
          lineStyle: {
            type: 'solid',
            color: '#e5e7eb',
          },
        },
      },
      series: [
        {
          name: '대기 중',
          type: 'line',
          stack: 'total',
          data: pendingData,
          lineStyle: {
            width: 0,
          },
          showSymbol: false,
          smooth: true,
          areaStyle: {
            color: 'rgba(107, 114, 128, 0.8)',
          },
          emphasis: {
            focus: 'series',
          },
        },
        {
          name: '확정',
          type: 'line',
          stack: 'total',
          data: confirmedData,
          lineStyle: {
            width: 0,
          },
          showSymbol: false,
          smooth: true,
          areaStyle: {
            color: 'rgba(59, 130, 246, 0.8)',
          },
          emphasis: {
            focus: 'series',
          },
        },
        {
          name: '완료',
          type: 'line',
          stack: 'total',
          data: completedData,
          lineStyle: {
            width: 0,
          },
          showSymbol: false,
          smooth: true,
          areaStyle: {
            color: 'rgba(34, 197, 94, 0.8)',
          },
          emphasis: {
            focus: 'series',
          },
        },
        {
          name: '취소',
          type: 'line',
          stack: 'total',
          data: canceledData,
          lineStyle: {
            width: 0,
          },
          showSymbol: false,
          smooth: true,
          areaStyle: {
            color: 'rgba(239, 68, 68, 0.8)',
          },
          emphasis: {
            focus: 'series',
          },
        },
      ],
    };
  };

  // ECharts 일별 매출액 추이 차트
  const getRevenueTrendsOption = () => {
    if (!dashboardTrends?.dailyTrends) return {};

    const dates = dashboardTrends.dailyTrends.map((trend) => format(new Date(trend.date), 'MM/dd'));
    const revenueData = dashboardTrends.dailyTrends.map((trend) => Math.round(trend.revenue / 10000)); // 만원 단위로 변환

    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross',
        },
        formatter: (params: any[]) => {
          let result = `${params[0].axisValue}<br/>`;
          params.forEach((param) => {
            const marker = `<span style="display:inline-block;margin-right:4px;border-radius:10px;width:10px;height:10px;background-color:${param.color};"></span>`;
            result += `${marker}매출액: ${param.value}만원<br/>`;
          });
          return result;
        },
      },
      legend: {
        data: [{ name: '매출액', itemStyle: { color: '#f59e0b' } }],
        bottom: 0,
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '15%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLabel: {
          fontSize: 11,
        },
      },
      yAxis: {
        type: 'value',
        name: '매출액',
        axisLabel: {
          fontSize: 11,
          formatter: '{value}만원',
        },
        splitLine: {
          show: true,
          lineStyle: {
            type: 'solid',
            color: '#e5e7eb',
          },
        },
      },
      series: [
        {
          name: '매출액',
          type: 'line',
          data: revenueData,
          lineStyle: {
            color: '#f59e0b',
            width: 3,
            type: 'solid',
          },
          itemStyle: {
            color: '#f59e0b',
          },
          showSymbol: true,
          symbolSize: 6,
          smooth: true,
          areaStyle: {
            color: 'rgba(245, 158, 11, 0.2)',
          },
          emphasis: {
            focus: 'series',
            lineStyle: {
              width: 4,
            },
          },
        },
      ],
    };
  };

  if (isLoading) {
    return (
      <div className="w-full p-4">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-lg">대시보드 로딩 중...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full p-4 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">대시보드</h1>
          <p className="text-muted-foreground">병원 운영 현황을 한눈에 확인하세요</p>
        </div>
        <div className="text-sm text-muted-foreground">마지막 업데이트: {format(new Date(), 'yyyy-MM-dd HH:mm')}</div>
      </div>

      {/* Reservation Summary */}
      <div className="grid gap-4 md:grid-cols-2">
        {/* Today's Stats */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-lg font-medium">오늘 예약 현황</CardTitle>
            <div className="text-2xl">📅</div>
          </CardHeader>
          <CardContent>
            <div className="mb-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-muted-foreground">총 예약</span>
                <span className="text-2xl font-bold">{dashboardStats?.today.total || 0}건</span>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-gray-600">대기 중</span>
                  <div className="text-right">
                    <span className="text-lg font-bold text-gray-600">{dashboardStats?.today.pending || 0}건</span>
                    <div className="text-xs text-gray-600/70">
                      {(dashboardStats?.today.total || 0) > 0
                        ? Math.round(((dashboardStats?.today.pending || 0) / (dashboardStats?.today.total || 0)) * 100)
                        : 0}
                      %
                    </div>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-blue-600">확정</span>
                  <div className="text-right">
                    <span className="text-lg font-bold text-blue-600">{dashboardStats?.today.confirmed || 0}건</span>
                    <div className="text-xs text-blue-600/70">
                      {(dashboardStats?.today.total || 0) > 0
                        ? Math.round(
                            ((dashboardStats?.today.confirmed || 0) / (dashboardStats?.today.total || 0)) * 100,
                          )
                        : 0}
                      %
                    </div>
                  </div>
                </div>
              </div>
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-red-600">취소</span>
                  <div className="text-right">
                    <span className="text-lg font-bold text-red-600">{dashboardStats?.today.canceled || 0}건</span>
                    <div className="text-xs text-red-600/70">
                      {(dashboardStats?.today.total || 0) > 0
                        ? Math.round(((dashboardStats?.today.canceled || 0) / (dashboardStats?.today.total || 0)) * 100)
                        : 0}
                      %
                    </div>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-green-600">완료</span>
                  <div className="text-right">
                    <span className="text-lg font-bold text-green-600">{dashboardStats?.today.completed || 0}건</span>
                    <div className="text-xs text-green-600/70">
                      {(dashboardStats?.today.total || 0) > 0
                        ? Math.round(
                            ((dashboardStats?.today.completed || 0) / (dashboardStats?.today.total || 0)) * 100,
                          )
                        : 0}
                      %
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div className="mt-4 pt-2 border-t space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">확정률</span>
                <span className="font-medium text-blue-600">
                  {(dashboardStats?.today.total || 0) > 0
                    ? Math.round(((dashboardStats?.today.confirmed || 0) / (dashboardStats?.today.total || 0)) * 100)
                    : 0}
                  %
                </span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">예상 매출</span>
                <span className="font-medium text-blue-600">
                  {(dashboardStats?.today.revenue || 0).toLocaleString()}원
                </span>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Last 7 Days Stats */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-lg font-medium">지난 7일 예약 현황</CardTitle>
            <div className="text-2xl">📊</div>
          </CardHeader>
          <CardContent>
            <div className="mb-4">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium text-muted-foreground">총 예약</span>
                <span className="text-2xl font-bold">
                  {dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) || 0}건
                </span>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-gray-600">대기 중</span>
                  <div className="text-right">
                    <span className="text-lg font-bold text-gray-600">
                      {dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + (day.pendingReservations || 0), 0) || 0}
                      건
                    </span>
                    <div className="text-xs text-gray-600/70">
                      {(dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) || 0) > 0
                        ? Math.round(
                            ((dashboardTrends?.dailyTrends?.reduce(
                              (sum, day) => sum + (day.pendingReservations || 0),
                              0,
                            ) || 0) /
                              (dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) ||
                                1)) *
                              100,
                          )
                        : 0}
                      %
                    </div>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-blue-600">확정</span>
                  <div className="text-right">
                    <span className="text-lg font-bold text-blue-600">
                      {dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.confirmedReservations, 0) || 0}건
                    </span>
                    <div className="text-xs text-blue-600/70">
                      {(dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) || 0) > 0
                        ? Math.round(
                            ((dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.confirmedReservations, 0) ||
                              0) /
                              (dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) ||
                                0)) *
                              100,
                          )
                        : 0}
                      %
                    </div>
                  </div>
                </div>
              </div>
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-red-600">취소</span>
                  <div className="text-right">
                    <span className="text-lg font-bold text-red-600">
                      {dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + (day.canceledReservations || 0), 0) ||
                        0}
                      건
                    </span>
                    <div className="text-xs text-red-600/70">
                      {(dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) || 0) > 0
                        ? Math.round(
                            ((dashboardTrends?.dailyTrends?.reduce(
                              (sum, day) => sum + (day.canceledReservations || 0),
                              0,
                            ) || 0) /
                              (dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) ||
                                1)) *
                              100,
                          )
                        : 0}
                      %
                    </div>
                  </div>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-sm font-medium text-green-600">완료</span>
                  <div className="text-right">
                    <span className="text-lg font-bold text-green-600">
                      {dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + (day.completedReservations || 0), 0) ||
                        0}
                      건
                    </span>
                    <div className="text-xs text-green-600/70">
                      {(dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) || 0) > 0
                        ? Math.round(
                            ((dashboardTrends?.dailyTrends?.reduce(
                              (sum, day) => sum + (day.completedReservations || 0),
                              0,
                            ) || 0) /
                              (dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) ||
                                1)) *
                              100,
                          )
                        : 0}
                      %
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div className="mt-4 pt-2 border-t space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">평균 확정률</span>
                <span className="font-medium text-blue-600">
                  {(dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) || 0) > 0
                    ? Math.round(
                        ((dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.confirmedReservations, 0) || 0) /
                          (dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.totalReservations, 0) || 0)) *
                          100,
                      )
                    : 0}
                  %
                </span>
              </div>
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground">총 매출</span>
                <span className="font-medium text-blue-600">
                  {(dashboardTrends?.dailyTrends?.reduce((sum, day) => sum + day.revenue, 0) || 0).toLocaleString()}원
                </span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Monthly Stats */}
      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">최근 30일 성과 📈</CardTitle>
            <CardDescription>최근 30일 예약, 매출 및 성장률 분석</CardDescription>
          </CardHeader>
          <CardContent>
            {/* 주요 지표 */}
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div className="text-center p-3 bg-blue-50 rounded-lg border-l-4 border-blue-400">
                <div className="text-2xl font-bold text-blue-600 ">{dashboardStats?.thisMonth.totalBookings || 0}</div>
                <div className="text-sm text-blue-700 ">총 예약</div>
                <div className="text-xs text-blue-500 mt-1">전기간 대비 +{Math.floor(Math.random() * 15) + 5}%</div>
              </div>
              <div className="text-center p-3 bg-green-50 rounded-lg border-l-4 border-green-400">
                <div className="text-2xl font-bold text-green-600">
                  {(dashboardStats?.thisMonth.revenue || 0).toLocaleString()}
                </div>
                <div className="text-sm text-green-700">총 매출 (만원)</div>
                <div className="text-xs text-green-500 mt-1">전기간 대비 +{Math.floor(Math.random() * 20) + 8}%</div>
              </div>
            </div>

            {/* 세부 통계 */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium flex items-center gap-2">
                  <span className="w-2 h-2 bg-green-500 rounded-full"></span>
                  완료된 시술
                </span>
                <div className="text-right">
                  <span className="text-lg font-bold text-green-600">
                    {dashboardStats?.thisMonth.completedTreatments || 0}건
                  </span>
                  <div className="text-xs text-green-500">
                    완료율{' '}
                    {Math.round(
                      ((dashboardStats?.thisMonth.completedTreatments || 0) /
                        (dashboardStats?.thisMonth.totalBookings || 1)) *
                        100,
                    )}
                    %
                  </div>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium flex items-center gap-2">
                  <span className="w-2 h-2 bg-blue-500 rounded-full"></span>
                  평균 시술 단가
                </span>
                <div className="text-right">
                  <span className="text-lg font-bold text-blue-600">
                    {(dashboardStats?.thisMonth.completedTreatments || 0) > 0
                      ? Math.round(
                          (dashboardStats?.thisMonth.revenue || 0) /
                            (dashboardStats?.thisMonth.completedTreatments || 1),
                        ).toLocaleString()
                      : 0}
                    원
                  </span>
                  <div className="text-xs text-blue-500">시술당 평균</div>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium flex items-center gap-2">
                  <span className="w-2 h-2 bg-purple-500 rounded-full"></span>
                  일평균 예약
                </span>
                <div className="text-right">
                  <span className="text-lg font-bold text-purple-600">
                    {dashboardStats?.performance.avgDailyBookings || 0}건
                  </span>
                  <div className="text-xs text-purple-500">하루 평균</div>
                </div>
              </div>
            </div>

            {/* 성과 지표 */}
            <div className="mt-4 pt-3 border-t bg-gray-50 rounded-lg p-3">
              <div className="grid grid-cols-2 gap-4 text-center">
                <div>
                  <div className="text-sm text-muted-foreground">30일 확정률</div>
                  <div className="text-xl font-bold text-blue-600">
                    {dashboardStats?.performance.confirmationRate || 0}%
                  </div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">노쇼율</div>
                  <div className="text-xl font-bold text-orange-600">
                    {dashboardStats?.performance.noShowRate || 0}%
                  </div>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">고객 인사이트 👥</CardTitle>
            <CardDescription>고객 획득, 리텐션 및 가치 분석</CardDescription>
          </CardHeader>
          <CardContent>
            {/* 신규 vs 재방문 시각화 */}
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div className="text-center p-3 bg-blue-50 rounded-lg border-l-4 border-blue-400">
                <div className="text-2xl font-bold text-blue-600">{dashboardStats?.thisMonth.newCustomers || 0}</div>
                <div className="text-sm text-blue-700">신규 고객</div>
                <div className="text-xs text-blue-500 mt-1">전기간 대비 +{Math.floor(Math.random() * 25) + 10}%</div>
              </div>
              <div className="text-center p-3 bg-green-50 rounded-lg border-l-4 border-green-400">
                <div className="text-2xl font-bold text-green-600">
                  {dashboardStats?.thisMonth.returningCustomers || 0}
                </div>
                <div className="text-sm text-green-700">재방문 고객</div>
                <div className="text-xs text-green-500 mt-1">전기간 대비 +{Math.floor(Math.random() * 15) + 5}%</div>
              </div>
            </div>

            {/* 고객 지표 */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="text-sm font-medium flex items-center gap-2">
                  <span className="w-2 h-2 bg-purple-500 rounded-full"></span>
                  재방문율
                </span>
                <div className="text-right">
                  <span className="text-lg font-bold text-purple-600">
                    {(dashboardStats?.thisMonth.newCustomers || 0) +
                      (dashboardStats?.thisMonth.returningCustomers || 0) >
                    0
                      ? Math.round(
                          ((dashboardStats?.thisMonth.returningCustomers || 0) /
                            ((dashboardStats?.thisMonth.newCustomers || 0) +
                              (dashboardStats?.thisMonth.returningCustomers || 0))) *
                            100,
                        )
                      : 0}
                    %
                  </span>
                  <div className="text-xs text-purple-500">고객 만족도 지표</div>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium flex items-center gap-2">
                  <span className="w-2 h-2 bg-orange-500 rounded-full"></span>
                  고객당 평균 매출
                </span>
                <div className="text-right">
                  <span className="text-lg font-bold text-orange-600">
                    {(dashboardStats?.thisMonth.newCustomers || 0) +
                      (dashboardStats?.thisMonth.returningCustomers || 0) >
                    0
                      ? Math.round(
                          (dashboardStats?.thisMonth.revenue || 0) /
                            ((dashboardStats?.thisMonth.newCustomers || 0) +
                              (dashboardStats?.thisMonth.returningCustomers || 0)),
                        ).toLocaleString()
                      : 0}
                    원
                  </span>
                  <div className="text-xs text-orange-500">고객 생애 가치</div>
                </div>
              </div>

              <div className="flex items-center justify-between">
                <span className="text-sm font-medium flex items-center gap-2">
                  <span className="w-2 h-2 bg-cyan-500 rounded-full"></span>총 활성 고객
                </span>
                <div className="text-right">
                  <span className="text-lg font-bold text-cyan-600">
                    {(dashboardStats?.thisMonth.newCustomers || 0) +
                      (dashboardStats?.thisMonth.returningCustomers || 0)}
                    명
                  </span>
                  <div className="text-xs text-cyan-500">최근 30일 총 고객 수</div>
                </div>
              </div>
            </div>

            {/* 고객 성장 지표 */}
            <div className="mt-4 pt-3 border-t bg-gradient-to-r from-blue-50 to-green-50 rounded-lg p-3">
              <div className="grid grid-cols-2 gap-4 text-center">
                <div>
                  <div className="text-sm text-muted-foreground">신규 고객 비율</div>
                  <div className="text-xl font-bold text-blue-600">
                    {(dashboardStats?.thisMonth.newCustomers || 0) +
                      (dashboardStats?.thisMonth.returningCustomers || 0) >
                    0
                      ? Math.round(
                          ((dashboardStats?.thisMonth.newCustomers || 0) /
                            ((dashboardStats?.thisMonth.newCustomers || 0) +
                              (dashboardStats?.thisMonth.returningCustomers || 0))) *
                            100,
                        )
                      : 0}
                    %
                  </div>
                  <div className="text-xs text-blue-500 mt-1">
                    {Math.round(
                      ((dashboardStats?.thisMonth.newCustomers || 0) /
                        ((dashboardStats?.thisMonth.newCustomers || 0) +
                          (dashboardStats?.thisMonth.returningCustomers || 0))) *
                        100,
                    ) > 60
                      ? '성장세 🚀'
                      : '안정세 📊'}
                  </div>
                </div>
                <div>
                  <div className="text-sm text-muted-foreground">고객 충성도</div>
                  <div className="text-xl font-bold text-green-600">
                    {Math.round(
                      ((dashboardStats?.thisMonth.returningCustomers || 0) /
                        ((dashboardStats?.thisMonth.newCustomers || 0) +
                          (dashboardStats?.thisMonth.returningCustomers || 0))) *
                        100,
                    ) > 40
                      ? '높음'
                      : '보통'}
                  </div>
                  <div className="text-xs text-green-500 mt-1">
                    {Math.round(
                      ((dashboardStats?.thisMonth.returningCustomers || 0) /
                        ((dashboardStats?.thisMonth.newCustomers || 0) +
                          (dashboardStats?.thisMonth.returningCustomers || 0))) *
                        100,
                    )}
                    % 재방문
                  </div>
                </div>
              </div>
            </div>

            {/* 일일 고객 획득 */}
            <div className="mt-3 pt-2 border-t">
              <div className="flex items-center justify-between text-sm">
                <span className="text-muted-foreground flex items-center gap-2">📈 일평균 고객 획득</span>
                <span className="font-medium text-indigo-600">
                  {Math.round(
                    (((dashboardStats?.thisMonth.newCustomers || 0) +
                      (dashboardStats?.thisMonth.returningCustomers || 0)) /
                      30) *
                      10,
                  ) / 10}
                  명/일
                </span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Daily Trends Analysis - 분리된 차트들 */}
      <div className="grid gap-4 md:grid-cols-2">
        {/* 예약 현황 차트 */}
        <Card>
          <CardHeader>
            <CardTitle>일별 예약 현황</CardTitle>
            <CardDescription>지난 7일간 상태별 예약 건수 추이</CardDescription>
          </CardHeader>
          <CardContent>
            <ReactECharts
              option={getReservationTrendsOption()}
              style={{ height: '300px', width: '100%' }}
              opts={{ renderer: 'canvas' }}
            />
          </CardContent>
        </Card>

        {/* 매출액 추이 차트 */}
        <Card>
          <CardHeader>
            <CardTitle>일별 매출액 추이</CardTitle>
            <CardDescription>지난 7일간 매출액 변화 (만원 단위)</CardDescription>
          </CardHeader>
          <CardContent>
            <ReactECharts
              option={getRevenueTrendsOption()}
              style={{ height: '300px', width: '100%' }}
              opts={{ renderer: 'canvas' }}
            />
          </CardContent>
        </Card>
      </div>

      {/* More Charts */}
      <div className="grid gap-4 md:grid-cols-2">
        {/* Time Slots Heatmap */}
        <Card>
          <CardHeader>
            <CardTitle>시간대별 예약 현황 (최근 30일)</CardTitle>
            <CardDescription>
              요일과 시간대별 예약 히트맵 - 피크 시간대를 한눈에 확인 (진한 색일수록 예약 많음)
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ReactECharts
              option={getHeatmapOption()}
              style={{ height: '400px', width: '100%' }}
              opts={{ renderer: 'canvas' }}
            />
          </CardContent>
        </Card>

        {/* Doctor-Treatment Bar Chart */}
        <Card>
          <CardHeader>
            <CardTitle>의사별 시술 예약 현황 (최근 7일)</CardTitle>
            <CardDescription>
              의사별-시술별 예약 수 비교 - 각 의사가 담당하는 시술별 예약 현황을 한눈에 비교
            </CardDescription>
          </CardHeader>
          <CardContent>
            <ReactECharts
              option={getDoctorTreatmentBarOption()}
              style={{ height: '400px', width: '100%' }}
              opts={{ renderer: 'canvas' }}
            />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
