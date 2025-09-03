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

  useEffect(() => {
    if (!hospitalId) {
      setError('Hospital ID is missing.');
      setIsLoading(false);
      return;
    }

    const fetchHospitalData = async () => {
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
    try {
      await createReservation(data);
      setIsDialogOpen(false);
      fetchUserReservation();
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
    }
  };

  const formatTime = (isoString: string) =>
    new Date(isoString).toLocaleTimeString('ko-KR', {
      year: '2-digit',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });

  if (isLoading) {
    return <div className="container mx-auto py-16 text-center">Loading...</div>;
  }

  if (error) {
    return <div className="container mx-auto py-16 text-center text-red-500">{error}</div>;
  }

  if (!hospital) {
    return <div className="container mx-auto py-16 text-center">Hospital not found.</div>;
  }

  return (
    <div className="w-full mt-4">
      <Card className="pt-0">
        <CardHeader className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-t-lg py-4">
          <CardTitle className="text-3xl">{hospital.name}</CardTitle>
        </CardHeader>

        <CardContent className="">
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
                <h3 className="text-lg font-semibold text-gray-800 mb-2">운영 시간</h3>
                <div className="space-y-1 text-sm text-gray-600">
                  <div className="flex justify-between">
                    <span>평일:</span>
                    <span>오전 9:00 ~ 오후 6:00</span>
                  </div>
                  <div className="flex justify-between">
                    <span>토요일:</span>
                    <span>오전 9:00 ~ 오후 1:00</span>
                  </div>
                  <div className="flex justify-between">
                    <span>일요일/공휴일:</span>
                    <span>휴진</span>
                  </div>
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
                                  {formatTime(reservation.startTime)}
                                  {' ~ '}
                                  {formatTime(reservation.endTime)}
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
                      <Button type="submit" form="reservation-form">
                        예약 신청
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
      <div className="mt-4">
        <Button asChild variant="outline" className="">
          <Link to="/hospitals">← 병원 목록으로</Link>
        </Button>
      </div>
    </div>
  );
}
