import { useState, useEffect } from 'react';
import { getReservations } from '@/api/reservation';
import { getCurrentAdmin } from '@/api/admin';
import { format } from 'date-fns';

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
  const [adminId, setAdminId] = useState<string>('');
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [hospitalId, setHospitalId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [monthlyReservationsData, setMonthlyReservationsData] = useState<Record<string, Reservation[]>>({});
  const [currentMonth, setCurrentMonth] = useState<Date>(new Date()); // 현재 보고 있는 달 추가

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
        console.log(adminResponse);
        const adminHospitalId = adminResponse.body.hospital?.id;

        if (!adminHospitalId) {
          throw new Error('Hospital ID not found for the current admin.');
        }

        setHospitalId(adminHospitalId);
        setAdminId(adminResponse.body.id);

        // Step 2: Fetch reservations for the current month using the new handleDateChange
        await handleDateChange(new Date()); // Fetch for current month initially
      } catch (err) {
        console.error('Failed to fetch initial data:', err);
        setError('초기 데이터를 가져오는 데 실패했습니다.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchAdminInfoAndInitialData();
  }, [hospitalId]);

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
