import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogClose,
} from '@/components/ui/dialog';
import { getHospitalById } from '@/api/hospital';
import { getTreatmentProductsByHospital } from '@/api/treatmentProduct';
import { getBusinessHoursByHospital } from '@/api/businessHour';
import { createReservation } from '@/api/reservation';
import ReservationForm from '@/components/common/ReservationForm';
import { Toaster, toast } from 'sonner';
import { formatDistanceToNow, isToday, isPast, isFuture, differenceInHours, format } from 'date-fns';
import { ko } from 'date-fns/locale';

interface BusinessHour {
  id: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  openTime: string;
  closeTime: string;
  breakStartTime: string;
  breakEndTime: string;
  closed: boolean;
}

// Mock data and functions remain the same
interface TreatmentProduct {
  id: string;
  name: string;
}

import { getReservationsForUserInHospital } from '@/api/reservation';
import { useAuth } from '@/context/AuthContext';

interface Hospital {
  id: string;
  name: string;
  address: string;
  createdAt: string;
}

export default function HospitalDetailPage() {
  const { id: hospitalId } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();

  const [hospital, setHospital] = useState<Hospital | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [treatmentProducts, setTreatmentProducts] = useState<TreatmentProduct[]>([]);
  const [businessHours, setBusinessHours] = useState<BusinessHour[]>([]);
  const [userReservations, setUserReservations] = useState<any[]>([]);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    const fetchHospitalData = async () => {
      if (!hospitalId) return;

      setIsLoading(true);

      try {
        const [hospitalResponse, productsResponse, businessHoursResponse] = await Promise.all([
          getHospitalById(hospitalId),
          getTreatmentProductsByHospital(hospitalId),
          getBusinessHoursByHospital(hospitalId),
        ]);

        if (hospitalResponse.result.resultCode === 200) {
          setHospital(hospitalResponse.body);
        } else {
          setError(hospitalResponse.result.resultMessage || 'Failed to fetch hospital details.');
        }
        setTreatmentProducts(productsResponse.body || []);
        setBusinessHours(businessHoursResponse.body || []);

        if (user && user.id) {
          fetchUserReservation();
        }
      } catch (err: any) {
        if (err.response?.status === 401) {
          alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
          navigate('/login');
          return;
        }
        setError('An error occurred while fetching hospital data.');
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchHospitalData();
  }, [hospitalId, user, navigate]);

  const fetchUserReservation = async () => {
    const reservationsResponse = await getReservationsForUserInHospital(user.id, hospitalId);
    if (reservationsResponse.result.resultCode === 200) {
      setUserReservations(reservationsResponse.body || []);
    }
  };

  const handleReservationSubmit = async (data: any) => {
    setIsSubmitting(true);
    try {
      await createReservation(data);
      setIsDialogOpen(false);
      fetchUserReservation();
      toast.success('예약 접수', {
        description: '예약을 접수하였습니다',
      });
    } catch (error: any) {
      if (error.response?.status === 401) {
        alert('로그인이 필요합니다. 로그인 페이지로 이동합니다.');
        navigate('/login');
      } else {
        console.error('Reservation failed:', error);
        const errorMessage =
          error.response?.data?.result.resultDescription || '예약에 실패했습니다. 다시 시도해주세요.';
        alert(errorMessage);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  // 사용자 친화적인 시간 표시
  const formatReservationTime = (startTimeIso: string, endTimeIso: string) => {
    const startTime = new Date(startTimeIso);
    const endTime = new Date(endTimeIso);
    const now = new Date();

    // 시간 표시 (09:30 - 10:00)
    const timeRange = `${format(startTime, 'HH:mm')} - ${format(endTime, 'HH:mm')}`;
    // 날짜 표시 (M월 d일)
    const dateDisplay = format(startTime, 'M월 d일', { locale: ko });

    if (isToday(startTime)) {
      const hoursUntil = differenceInHours(startTime, now);
      if (hoursUntil > 0) {
        return `오늘 ${timeRange} (${hoursUntil}시간 후, ${dateDisplay})`;
      } else if (hoursUntil === 0) {
        return `오늘 ${timeRange} (곧 시작, ${dateDisplay})`;
      } else {
        return `오늘 ${timeRange} (완료, ${dateDisplay})`;
      }
    }

    if (isPast(startTime)) {
      const distance = formatDistanceToNow(startTime, { locale: ko, addSuffix: false });
      return `${timeRange} (${distance} 전, ${dateDisplay})`;
    }

    if (isFuture(startTime)) {
      const distance = formatDistanceToNow(startTime, { locale: ko, addSuffix: false });
      return `${timeRange} (${distance} 후, ${dateDisplay})`;
    }

    return `${timeRange} (${dateDisplay})`;
  };

  // 운영시간 포맷팅 함수
  const formatBusinessHours = () => {
    const dayNames = {
      MONDAY: '월요일',
      TUESDAY: '화요일',
      WEDNESDAY: '수요일',
      THURSDAY: '목요일',
      FRIDAY: '금요일',
      SATURDAY: '토요일',
      SUNDAY: '일요일',
    };

    const formatTime = (timeString: string) => {
      if (!timeString) return '';
      const [hour, minute] = timeString.split(':');
      const hourNum = parseInt(hour);
      if (hourNum === 0) return '오전 12:' + minute;
      if (hourNum < 12) return '오전 ' + hourNum + ':' + minute;
      if (hourNum === 12) return '오후 12:' + minute;
      return '오후 ' + (hourNum - 12) + ':' + minute;
    };

    return businessHours
      .sort((a, b) => {
        const dayOrder = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
        return dayOrder.indexOf(a.dayOfWeek) - dayOrder.indexOf(b.dayOfWeek);
      })
      .map((hour) => ({
        day: dayNames[hour.dayOfWeek],
        content: hour.closed
          ? '휴무'
          : `${formatTime(hour.openTime)} ~ ${formatTime(hour.closeTime)}${
              hour.breakStartTime && hour.breakEndTime
                ? ` (휴게: ${formatTime(hour.breakStartTime)} ~ ${formatTime(hour.breakEndTime)})`
                : ''
            }`,
      }));
  };

  if (error) {
    return <div className="container mx-auto py-16 text-center text-red-500">{error}</div>;
  }

  return (
    <div className="w-full">
      <Toaster expand={true} richColors position="top-center" />
      <Card className="pt-0 my-4">
        <CardHeader className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-t-lg flex items-center justify-between">
          <div className="">
            <Link to="/hospitals">← 목록으로</Link>
          </div>
          <CardTitle className="text-3xl  py-4">{isLoading ? ' ' : hospital.name}</CardTitle>
        </CardHeader>

        <CardContent className="">
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
              <span className="ml-2 text-sm text-gray-500">정보를 불러오는 중...</span>
            </div>
          ) : (
            <div className="">
              <div className="space-y-6">
                <div className="p-4 rounded-lg border">
                  <h3 className="text-lg font-semibold text-gray-800 mb-2">병원 정보</h3>
                  <div className="space-y-1 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-600">주소:</span>
                      <span className="font-medium text-right">{hospital.address}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600">등록일:</span>
                      <span className="font-medium">{new Date(hospital.createdAt).toLocaleDateString('ko-KR')}</span>
                    </div>
                  </div>
                </div>

                <div className="p-4 rounded-lg border">
                  <h3 className="text-lg font-semibold text-gray-800 mb-2">제공 서비스</h3>
                  <div className="space-y-2 text-sm">
                    {treatmentProducts.length > 0 ? (
                      treatmentProducts.map((product) => (
                        <div
                          key={product.id}
                          className="flex justify-between items-center py-2 border-b border-gray-100 last:border-b-0"
                        >
                          <div>
                            <div className="font-medium text-gray-800">{product.name}</div>
                          </div>
                          <div className="text-right">
                            <div className="font-semibold text-blue-600">{product.price?.toLocaleString()}원</div>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="text-center text-gray-500 py-4">서비스 정보를 불러오는 중...</div>
                    )}
                  </div>
                </div>

                <div className="p-4 rounded-lg border">
                  <h3 className="text-lg font-semibold text-gray-800 mb-2">운영 시간</h3>
                  <div className="space-y-1 text-sm text-gray-600">
                    {businessHours.length > 0 ? (
                      formatBusinessHours().map((businessHour, index) => (
                        <div key={index} className="flex justify-between">
                          <span>{businessHour.day}:</span>
                          <span>{businessHour.content}</span>
                        </div>
                      ))
                    ) : (
                      <div className="text-center text-gray-500">운영시간 정보를 불러오는 중...</div>
                    )}
                  </div>
                </div>

                <div className="p-4 rounded-lg border">
                  {user && (
                    <div>
                      <h3 className="text-lg font-semibold text-gray-800 mb-2">예약 내역</h3>
                      <div>
                        {userReservations.length > 0 ? (
                          <div className="space-y-4">
                            {userReservations.map((reservation) => (
                              <div className="space-y-1 text-sm text-gray-600" key={reservation.id}>
                                <div className="flex justify-between">
                                  <span className="font-medium text-right">{reservation.treatmentProductName}</span>
                                  <span className="font-medium text-right">
                                    {formatReservationTime(reservation.startTime, reservation.endTime)}
                                  </span>
                                </div>
                              </div>
                            ))}
                          </div>
                        ) : (
                          <p>이 병원에 대한 예약 내역이 없습니다.</p>
                        )}
                      </div>
                    </div>
                  )}
                </div>

                <div className="text-center">
                  <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
                    <DialogTrigger asChild>
                      <Button size="lg">예약 시작하기</Button>
                    </DialogTrigger>
                    <DialogContent className="sm:max-w-[425px]">
                      <DialogHeader>
                        <DialogTitle>예약 신청</DialogTitle>
                      </DialogHeader>
                      <ReservationForm
                        hospitalId={hospital.id}
                        treatmentProducts={treatmentProducts}
                        businessHours={businessHours}
                        forAdmin={false}
                        onSubmit={handleReservationSubmit}
                      />
                      <DialogFooter>
                        <DialogClose asChild>
                          <Button type="button" variant="secondary">
                            취소
                          </Button>
                        </DialogClose>
                        <Button type="submit" form="reservation-form" disabled={isSubmitting}>
                          {isSubmitting ? '예약 신청 중...' : '예약 신청'}
                        </Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </div>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
