import { useState, useEffect, useRef } from 'react';
import { getReservations } from '@/api/reservation';
import { getCurrentAdmin } from '@/api/admin';
import { format } from 'date-fns';
import { toast } from 'sonner';
import { useAuth } from '@/context/AuthContext';

import MedicalRecordCalendar from '@/components/admin/MedicalRecordCalendar';
import ReservationListTable from '@/components/admin/ReservationListTable';

// 백엔드 응답에 맞춘 인터페이스
interface Reservation {
  id: string;
  startTime: string;
  endTime: string;
  status: 'PENDING' | 'RESERVED' | 'CANCELED' | 'COMPLETED';
  userMemo: string;
  note?: string; // 병원 내부용 메모 (옵셔널)
  user: {
    id: string;
    name: string;
    phone: string;
  };
  treatmentProduct: {
    id: string;
    name: string;
  };
}

export default function SchedulePage() {
  const { token } = useAuth();
  const [adminId, setAdminId] = useState<string>('');
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [hospitalId, setHospitalId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [monthlyReservationsData, setMonthlyReservationsData] = useState<Record<string, Reservation[]>>({});
  const [currentMonth, setCurrentMonth] = useState<Date>(new Date()); // 현재 보고 있는 달 추가
  const eventSourceRef = useRef<EventSource | null>(null);

  const handleDateChange = async (month: Date) => {
    setIsLoading(true);
    setError(null);
    setCurrentMonth(month); // 현재 보고 있는 달 업데이트
    try {
      if (!hospitalId) return;

      // UTC 기준으로 월의 시작일과 종료일 계산 (시간대 변환 방지)
      const startOfMonth = new Date(Date.UTC(month.getFullYear(), month.getMonth(), 1, 0, 0, 0, 0));
      const endOfMonth = new Date(Date.UTC(month.getFullYear(), month.getMonth() + 1, 0, 23, 59, 59, 999));

      const response = await getReservations(hospitalId, startOfMonth.toISOString(), endOfMonth.toISOString());
      setReservations(response.body || []);

      const fetchedReservations = response.body.map((reservation: any) => ({
        id: reservation.id.toString(),
        productId: reservation.treatmentProductId.toString(),
        startTime: new Date(reservation.startTime),
        endTime: new Date(reservation.endTime),
        patientName: reservation.userName,
        status: reservation.status,
        userName: reservation.userName,
        treatmentProductName: reservation.treatmentProductName,
        memo: reservation.memo,
        note: reservation.note,
      }));
      const groupedReservations: Record<string, Reservation[]> = {};
      fetchedReservations.forEach((res: Reservation) => {
        const dateKey = format(res.startTime, 'yyyy-MM-dd');
        if (!groupedReservations[dateKey]) {
          groupedReservations[dateKey] = [];
        }
        groupedReservations[dateKey].push(res);
      });
      setMonthlyReservationsData(groupedReservations);
    } catch (err) {
      console.error('Failed to fetch schedule data:', err);
      setError('스케줄 데이터를 가져오는 데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    const fetchAdminInfoAndInitialData = async () => {
      setIsLoading(true);
      setError(null);
      try {
        // Step 1: Get Admin and Hospital ID
        const adminResponse = await getCurrentAdmin();
        const adminHospitalId = adminResponse.body.hospital?.id;

        if (!adminHospitalId) {
          throw new Error('Hospital ID not found for the current admin.');
        }

        setHospitalId(adminHospitalId);
        setAdminId(adminResponse.body.id);

        // Step 2: Fetch reservations for the current month
        const month = new Date();
        const startOfMonth = new Date(Date.UTC(month.getFullYear(), month.getMonth(), 1, 0, 0, 0, 0));
        const endOfMonth = new Date(Date.UTC(month.getFullYear(), month.getMonth() + 1, 0, 23, 59, 59, 999));

        const response = await getReservations(adminHospitalId, startOfMonth.toISOString(), endOfMonth.toISOString());
        setReservations(response.body || []);

        const fetchedReservations = response.body.map((reservation: any) => ({
          id: reservation.id.toString(),
          productId: reservation.treatmentProductId.toString(),
          startTime: new Date(reservation.startTime),
          endTime: new Date(reservation.endTime),
          patientName: reservation.userName,
          status: reservation.status,
          userName: reservation.userName,
          treatmentProductName: reservation.treatmentProductName,
          memo: reservation.memo,
          note: reservation.note,
        }));

        const groupedReservations: Record<string, Reservation[]> = {};
        fetchedReservations.forEach((res: Reservation) => {
          const dateKey = format(res.startTime, 'yyyy-MM-dd');
          if (!groupedReservations[dateKey]) {
            groupedReservations[dateKey] = [];
          }
          groupedReservations[dateKey].push(res);
        });
        setMonthlyReservationsData(groupedReservations);
      } catch (err) {
        console.error('Failed to fetch initial data:', err);
        setError('초기 데이터를 가져오는 데 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchAdminInfoAndInitialData();
  }, []); // 의존성 배열 비움 - 컴포넌트 마운트시에만 실행

  // SSE 연결 설정
  useEffect(() => {
    if (!hospitalId || !token) return;

    // SSE 연결 설정 (토큰을 쿼리 파라미터로 전달)
    const eventSource = new EventSource(`${import.meta.env.VITE_API_URL}/admin/notifications/stream?token=${token}`, {
      withCredentials: true, // 인증 쿠키 포함
    });
    eventSourceRef.current = eventSource;

    eventSource.onopen = () => {
      console.log('SSE 연결 성공');
    };

    eventSource.addEventListener('NEW_RESERVATION', (event) => {
      try {
        const newReservationData = JSON.parse(event.data);

        // 새 예약을 현재 예약 목록에 추가
        const newReservation = {
          id: newReservationData.id.toString(),
          productId: newReservationData.treatmentProductId.toString(),
          startTime: new Date(newReservationData.startTime),
          endTime: new Date(newReservationData.endTime),
          patientName: newReservationData.userName,
          status: newReservationData.status,
          userName: newReservationData.userName,
          treatmentProductName: newReservationData.treatmentProductName,
          memo: newReservationData.userMemo, // ViewDocument의 userMemo 필드
          note: newReservationData.note,
          doctorName: newReservationData.doctorName,
        };

        // 현재 월의 예약인지 확인
        const reservationMonth = format(newReservation.startTime, 'yyyy-MM');
        const currentMonthStr = format(currentMonth, 'yyyy-MM');

        if (reservationMonth === currentMonthStr) {
          // reservations 배열에 추가
          setReservations((prev) => [newReservation, ...prev]);

          // monthlyReservationsData에도 추가
          const dateKey = format(newReservation.startTime, 'yyyy-MM-dd');
          setMonthlyReservationsData((prev) => ({
            ...prev,
            [dateKey]: [...(prev[dateKey] || []), newReservation],
          }));
        }

        // 토스트 알림
        toast.success(`새 예약이 접수되었습니다: ${newReservation.userName}님`, {
          description: `${newReservation.treatmentProductName} - ${format(newReservation.startTime, 'MM월 dd일 HH:mm')}`,
        });
      } catch (error) {
        console.error('SSE 메시지 파싱 실패:', error);
      }
    });

    eventSource.addEventListener('RESERVATION_UPDATE', (event) => {
      try {
        const updatedReservationData = JSON.parse(event.data);

        // 예약 상태 업데이트
        setReservations((prev) =>
          prev.map((reservation) =>
            reservation.id === updatedReservationData.id.toString()
              ? {
                  ...reservation,
                  status: updatedReservationData.status,
                  note: updatedReservationData.note,
                  memo: updatedReservationData.userMemo,
                  doctorName: updatedReservationData.doctorName,
                }
              : reservation,
          ),
        );

        // monthlyReservationsData도 업데이트
        setMonthlyReservationsData((prev) => {
          const newData = { ...prev };
          Object.keys(newData).forEach((dateKey) => {
            newData[dateKey] = newData[dateKey].map((reservation) =>
              reservation.id === updatedReservationData.id.toString()
                ? {
                    ...reservation,
                    status: updatedReservationData.status,
                    note: updatedReservationData.note,
                    memo: updatedReservationData.userMemo,
                    doctorName: updatedReservationData.doctorName,
                  }
                : reservation,
            );
          });
          return newData;
        });

        const statusLabels = {
          RESERVED: '접수',
          PENDING: '대기',
          CANCELED: '취소',
          COMPLETED: '완료',
        };
        toast.info(
          `예약 상태가 변경되었습니다: ${statusLabels[updatedReservationData.status] || updatedReservationData.status}`,
        );
      } catch (error) {
        console.error('SSE 메시지 파싱 실패:', error);
      }
    });

    // 담당의사 배정 이벤트 처리
    eventSource.addEventListener('DOCTOR_ASSIGN', (event) => {
      try {
        const doctorAssignData = JSON.parse(event.data);

        // 담당의사 정보 업데이트
        setReservations((prev) =>
          prev.map((reservation) =>
            reservation.id === doctorAssignData.id.toString()
              ? { ...reservation, doctorName: doctorAssignData.doctorName }
              : reservation,
          ),
        );

        // monthlyReservationsData도 업데이트
        setMonthlyReservationsData((prev) => {
          const newData = { ...prev };
          Object.keys(newData).forEach((dateKey) => {
            newData[dateKey] = newData[dateKey].map((reservation) =>
              reservation.id === doctorAssignData.id.toString()
                ? { ...reservation, doctorName: doctorAssignData.doctorName }
                : reservation,
            );
          });
          return newData;
        });

        toast.success(`담당의사가 배정되었습니다: ${doctorAssignData.doctorName || '담당의사'}`);
      } catch (error) {
        console.error('SSE 담당의사 배정 메시지 파싱 실패:', error);
      }
    });

    // Heartbeat 메시지 처리 (연결 상태 유지)
    eventSource.addEventListener('HEARTBEAT', (event) => {
      console.log('SSE heartbeat 수신:', event.data);
    });

    // 연결된 메시지 처리
    eventSource.addEventListener('CONNECTED', (event) => {
      console.log('SSE 연결 확인:', event.data);
    });

    eventSource.onerror = (error) => {
      console.error('SSE 연결 오류:', error);
      toast.error('실시간 업데이트 연결에 문제가 발생했습니다.');
    };

    // 컴포넌트 언마운트 시 SSE 연결 해제
    return () => {
      if (eventSourceRef.current) {
        eventSourceRef.current.close();
        eventSourceRef.current = null;
      }
    };
  }, [hospitalId, currentMonth, token]);

  const handleReservationUpdate = async () => {
    if (!hospitalId) return;
    // 현재 보고 있는 달로 데이터 다시 가져오기
    await handleDateChange(currentMonth);
  };

  return (
    <div className="w-full p-4">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">스케줄 관리</h1>
          <p className="text-muted-foreground">병원 예약과 일정을 관리하세요</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 w-full">
        <div className="space-y-4 min-w-0">
          {/* 월간 예약 현황 달력 */}
          <MedicalRecordCalendar
            isLoading={isLoading}
            hospitalId={hospitalId}
            callbackMonthChange={handleDateChange}
            monthlyReservationsData={monthlyReservationsData}
          />
        </div>

        {/* 예약 리스트 테이블 */}
        <div className="min-w-0">
          <ReservationListTable
            reservations={reservations}
            isLoading={isLoading}
            error={error}
            hospitalId={hospitalId}
            onReservationUpdate={handleReservationUpdate}
            adminId={adminId}
          />
        </div>
      </div>
    </div>
  );
}
