import { useState, useEffect } from 'react';
import { ClipboardPlus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import {
  Dialog,
  DialogTrigger,
  DialogContent,
  DialogHeader,
  DialogClose,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/components/ui/dialog';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { followUpReservation, updateMedicalRecordNote } from '@/api/reservation';
import { getTreatmentProductsByHospital } from '@/api/treatmentProduct';
import { getBusinessHoursByHospital } from '@/api/businessHour';
import { getDoctorsByHospital, assignDoctorToReservation } from '@/api/doctor';
import { confirmReservation, cancelReservation, pendingReservation, completeReservation } from '@/api/reservation';
import MedicalRecordTimeTable from '@/components/admin/MedicalRecordTimeTable';
import ReservationForm from '@/components/common/ReservationForm';
import { Toaster, toast } from 'sonner';

interface Reservation {
  id: string;
  startTime: string;
  endTime: string;
  status: 'PENDING' | 'RESERVED' | 'CANCELED' | 'COMPLETED';
  userMemo: string;
  note: string;
  user: {
    id: string;
    name: string;
    phone: string;
  };
  treatmentProduct: {
    id: string;
    name: string;
  };
  userId: string;
  userName: string;
  treatmentProductName: string;
}

interface Service {
  id: string;
  name: string;
  duration: number;
}

interface BusinessHours {
  openTime: string; // "HH:mm"
  closeTime: string; // "HH:mm"
  lunchStartTime: string; // "HH:mm"
  lunchEndTime: string; // "HH:mm"
}

interface Props {
  reservations: Reservation[];
  isLoading: boolean;
  error: string | null;
  hospitalId: string | null;
  adminId: string;
  onReservationUpdate: () => void;
}

export default function ReservationListTable({
  reservations,
  isLoading,
  error,
  hospitalId,
  adminId,
  onReservationUpdate,
}: Props) {
  // 모달 상태
  const [isNextReservationModalOpen, setIsNextReservationModalOpen] = useState(false);
  const [currentReservationForNext, setCurrentReservationForNext] = useState<Reservation | null>(null);
  const [treatmentProducts, setTreatmentProducts] = useState<Service[]>([]);
  const [newReservationData, setNewReservationData] = useState({
    userId: '',
    treatmentProductId: '',
    startTime: '',
    endTime: '',
    note: '',
  });
  const [isNoteModalOpen, setIsNoteModalOpen] = useState(false);
  const [selectedReservationForNote, setSelectedReservationForNote] = useState<Reservation | null>(null);
  const [currentNote, setCurrentNote] = useState('');
  const [selectedStatus, setSelectedStatus] = useState<string>('');
  const [isStatusChanging, setIsStatusChanging] = useState(false);
  const [openPopoverId, setOpenPopoverId] = useState<string | null>(null);
  const [isNextReservationModalLoading, setIsNextReservationModalLoading] = useState(true);
  const [isSubmittingNextReservation, setIsSubmittingNextReservation] = useState(false);

  // 다음 예약을 위한 상태
  const [businessHours, setBusinessHours] = useState<BusinessHours | null>(null);
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(new Date());
  const [selectedTime, setSelectedTime] = useState('');

  // 담당의사 배정 관련 상태
  const [isDoctorAssignModalOpen, setIsDoctorAssignModalOpen] = useState(false);
  const [selectedReservationForDoctor, setSelectedReservationForDoctor] = useState<Reservation | null>(null);
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [selectedDoctorId, setSelectedDoctorId] = useState('');

  const getStatusBadge = (status: Reservation['status']) => {
    const variants = {
      RESERVED: 'bg-blue-500 hover:bg-blue-700 border-blue-700',
      PENDING: 'bg-gray-500 hover:bg-gray-700 border-gray-700',
      CANCELED: 'bg-red-500 hover:bg-red-700 border-red-700',
      COMPLETED: 'bg-green-500 hover:bg-green-700 border-green-700',
    } as const;

    const labels = {
      RESERVED: '접수',
      PENDING: '대기',
      CANCELED: '취소',
      COMPLETED: '완료',
    };

    return <Button className={`${variants[status]} border`}>{labels[status] || status}</Button>;
  };

  const formatTime = (isoString: string) =>
    new Date(isoString).toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });

  const formatDate = (isoString: string) =>
    new Date(isoString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'long',
    });

  // 예약을 날짜별로 그룹화
  const groupReservationsByDate = (reservations: Reservation[]) => {
    const grouped = reservations.reduce(
      (acc, reservation) => {
        const dateKey = new Date(reservation.startTime).toDateString();
        if (!acc[dateKey]) {
          acc[dateKey] = [];
        }
        acc[dateKey].push(reservation);
        return acc;
      },
      {} as Record<string, Reservation[]>,
    );

    // 날짜순으로 정렬하고 각 날짜 내에서도 시간순 정렬
    return Object.entries(grouped)
      .sort(([a], [b]) => new Date(a).getTime() - new Date(b).getTime())
      .map(([dateKey, reservations]) => ({
        date: dateKey,
        reservations: reservations.sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime()),
      }));
  };

  const groupedReservations = groupReservationsByDate(reservations);

  const handleNoteClick = (reservation: Reservation) => {
    setSelectedReservationForNote(reservation);
    setCurrentNote(reservation.note || '');
    setIsNoteModalOpen(true);
  };

  const handleNextReservationClick = async (reservation: Reservation) => {
    setCurrentReservationForNext(reservation);
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    setSelectedDate(tomorrow);

    if (hospitalId) {
      setIsNextReservationModalOpen(true);
      setIsNextReservationModalLoading(true);

      try {
        const [productsResponse, hoursResponse] = await Promise.all([
          getTreatmentProductsByHospital(hospitalId),
          getBusinessHoursByHospital(hospitalId),
        ]);

        setTreatmentProducts(
          productsResponse.body.map((product: any) => ({
            id: product.id,
            name: product.name,
            duration: product.duration || 60,
          })),
        );

        const fetchedHours = hoursResponse.body;
        setBusinessHours(fetchedHours);
        console.log(fetchedHours);
      } catch (error) {
        console.error('Failed to fetch data for next reservation:', error);
      } finally {
        setIsNextReservationModalLoading(false);
      }
    }
    setNewReservationData({
      userId: reservation.userId,
      treatmentProductId: reservation.treatmentProductId,
      startTime: '',
      endTime: '',
      note: '',
    });
    setSelectedTime('');
  };

  const handleSaveNote = async () => {
    if (!selectedReservationForNote) return;
    try {
      await updateMedicalRecordNote(
        selectedReservationForNote.id,
        selectedReservationForNote.userId,
        hospitalId,
        currentNote,
      );
      setIsNoteModalOpen(false);
    } catch (err) {
      console.error('Failed to save note:', err);
      toast.error('노트 내용 저장 실패.', {
        description: '서버 오류입니다',
      });
    }
  };

  // 상태 변경 핸들러 함수들
  const handleStatusChange = async (reservationId: string, userId: string, newStatus: string) => {
    setIsStatusChanging(true);
    try {
      if (newStatus === 'RESERVED') {
        await confirmReservation(reservationId, userId, hospitalId, adminId);
      } else if (newStatus === 'PENDING') {
        await pendingReservation(reservationId, userId, hospitalId, adminId);
      } else if (newStatus === 'CANCELED') {
        await cancelReservation(reservationId, userId, hospitalId, adminId);
      } else if (newStatus === 'COMPLETED') {
        await completeReservation(reservationId, userId, hospitalId, adminId);
      }

      // 성공 시 팝오버 닫기 및 상태 초기화
      setOpenPopoverId(null);
      setSelectedStatus('');
    } catch (error) {
      console.error('Failed to change reservation status:', error);
      toast.error('예약 상태 변경 실패', {
        description: '서버 오류입니다',
      });
    } finally {
      setIsStatusChanging(false);
    }
  };

  const getStatusText = (status: string) => {
    const statusMap = {
      PENDING: '대기',
      RESERVED: '접수',
      CANCELED: '취소',
      COMPLETED: '완료',
    };
    return statusMap[status] || status;
  };

  useEffect(() => {
    if (!selectedDate || !selectedTime || !newReservationData.treatmentProductId) {
      setNewReservationData((prev) => ({ ...prev, startTime: '', endTime: '' }));
      return;
    }

    const selectedProduct = treatmentProducts.find((p) => p.id === newReservationData.treatmentProductId);
    if (!selectedProduct) return;

    const [hour, minute] = selectedTime.split(':').map(Number);
    const startDateTime = new Date(selectedDate);
    startDateTime.setHours(hour, minute, 0, 0);

    const endDateTime = new Date(startDateTime.getTime() + selectedProduct.duration * 60 * 1000);

    setNewReservationData((prev) => ({
      ...prev,
      startTime: startDateTime.toISOString(),
      endTime: endDateTime.toISOString(),
    }));
  }, [selectedDate, selectedTime, newReservationData.treatmentProductId, treatmentProducts]);

  const handleReservationSubmit = async (data: any) => {
    setIsSubmittingNextReservation(true);
    try {
      await followUpReservation({
        userId: newReservationData.userId,
        ...data,
      });
      setIsNextReservationModalOpen(false);
    } catch (error) {
      console.error(error);
      toast.error('예약 생성 실패.', {
        description: '다음 예약 생성에 실패했습니다.',
      });
    } finally {
      setIsSubmittingNextReservation(false);
    }
  };

  const handleDoctorAssignClick = async (reservation: Reservation) => {
    setSelectedReservationForDoctor(reservation);

    if (hospitalId) {
      try {
        const doctorsResponse = await getDoctorsByHospital(hospitalId);
        setDoctors(doctorsResponse.body || []);
      } catch (error) {
        console.error(error);
      }
    }
    setIsDoctorAssignModalOpen(true);
  };

  const handleAssignDoctor = async () => {
    if (!selectedReservationForDoctor || !selectedDoctorId || !hospitalId) return;

    try {
      await assignDoctorToReservation(selectedReservationForDoctor.id, {
        userId: selectedReservationForDoctor.userId,
        hospitalId: hospitalId,
        doctorId: selectedDoctorId,
      });

      toast.success('담당 의사 배정 성공.', {
        description: '의사 배정이 완료되었습니다',
      });
      setIsDoctorAssignModalOpen(false);
      setSelectedDoctorId('');
      setSelectedReservationForDoctor(null);
    } catch (error) {
      console.error('Failed to assign doctor:', error);
      alert('담당의사 배정에 실패했습니다.');
      toast.error('담당 의사 배정 실패.', {
        description: '서버 오류입니다',
      });
    }
  };

  return (
    <>
      <Toaster expand={true} richColors position="top-center" />
      <div className="p-0 text-sm w-full border border-t rounded-xl shadow-sm">
        {isLoading ? (
          <div className="flex items-center justify-center py-8">
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
            <span className="ml-2 text-sm text-gray-500">정보를 불러오는 중...</span>
          </div>
        ) : (
          <Table className="w-full">
            <TableHeader>
              <TableRow>
                <TableHead></TableHead>
                <TableHead>고객</TableHead>
                <TableHead>요청사항</TableHead>
                <TableHead>진료 내용</TableHead>
                <TableHead>담당의사</TableHead>
                <TableHead>상태</TableHead>
                <TableHead>내부 메모</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {error ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center text-red-500">
                    {error}
                  </TableCell>
                </TableRow>
              ) : reservations.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center">
                    해당 날짜에 예약이 없습니다.
                  </TableCell>
                </TableRow>
              ) : (
                groupedReservations.map((group, groupIndex) => {
                  return (
                    <>
                      {/* 날짜 헤더 행 */}
                      <TableRow key={`date-${group.date}`} className="bg-gray-50">
                        <TableCell colSpan={7} className="text-gray-700 py-3">
                          <Dialog>
                            <DialogTrigger className="w-full h-full text-left cursor-pointer">
                              {formatDate(group.reservations[0].startTime)}
                            </DialogTrigger>
                            <DialogContent className="min-w-4xl">
                              <DialogTitle></DialogTitle>
                              <div style={{ height: '80vh' }}>
                                <MedicalRecordTimeTable
                                  hospitalId={hospitalId}
                                  selectedDate={new Date(group.reservations[0].startTime).toISOString().slice(0, 10)}
                                />
                              </div>
                            </DialogContent>
                          </Dialog>
                        </TableCell>
                      </TableRow>
                      {/* 해당 날짜의 예약들 */}
                      {group.reservations.map((reservation) => (
                        <TableRow key={reservation.id} className="cursor-pointer hover:bg-muted/50">
                          <TableCell className="font-medium pl-6">
                            {`${formatTime(reservation.startTime)} ~ ${formatTime(reservation.endTime)}`}
                          </TableCell>
                          <TableCell>{reservation.userName}</TableCell>
                          <TableCell className="max-w-xs truncate">{reservation.userMemo}</TableCell>
                          <TableCell>{reservation.treatmentProductName}</TableCell>
                          <TableCell
                            className="cursor-pointer hover:bg-muted/50"
                            onClick={() => handleDoctorAssignClick(reservation)}
                          >
                            <span className={reservation.doctorName ? '' : 'text-muted-foreground'}>
                              {reservation.doctorName || '담당의사 선택'}
                            </span>
                          </TableCell>
                          <TableCell className="flex">
                            <Popover
                              open={openPopoverId === reservation.id}
                              onOpenChange={(open) => {
                                if (open) {
                                  setOpenPopoverId(reservation.id);
                                  setSelectedStatus('');
                                } else {
                                  setOpenPopoverId(null);
                                  setSelectedStatus('');
                                }
                              }}
                            >
                              <PopoverTrigger>{getStatusBadge(reservation.status)}</PopoverTrigger>
                              <PopoverContent>
                                <div className="flex justify-between">
                                  <Select value={selectedStatus || ''} onValueChange={setSelectedStatus}>
                                    <SelectTrigger className="w-[180px]">
                                      <SelectValue placeholder="상태 변경 선택" />
                                    </SelectTrigger>
                                    <SelectContent>
                                      {/* PENDING일 때는 RESERVED, CANCELED만 선택 가능 */}
                                      {reservation.status === 'PENDING' && (
                                        <>
                                          <SelectItem value="RESERVED">접수</SelectItem>
                                          <SelectItem value="CANCELED">취소</SelectItem>
                                        </>
                                      )}
                                      {/* RESERVED일 때는 CANCELED, PENDING, COMPLETED 선택 가능 */}
                                      {reservation.status === 'RESERVED' && (
                                        <>
                                          <SelectItem value="CANCELED">취소</SelectItem>
                                          <SelectItem value="PENDING">대기</SelectItem>
                                          <SelectItem value="COMPLETED">완료</SelectItem>
                                        </>
                                      )}
                                      {/* COMPLETED일 때는 CANCELED, RESERVED만 선택 가능 */}
                                      {reservation.status === 'COMPLETED' && (
                                        <>
                                          <SelectItem value="CANCELED">취소</SelectItem>
                                          <SelectItem value="RESERVED">접수</SelectItem>
                                        </>
                                      )}
                                      {/* CANCELED일 때도 다른 상태로 변경할 수 있도록 */}
                                      {reservation.status === 'CANCELED' && (
                                        <>
                                          <SelectItem value="PENDING">대기</SelectItem>
                                          <SelectItem value="RESERVED">접수</SelectItem>
                                        </>
                                      )}
                                    </SelectContent>
                                  </Select>
                                  <Button
                                    onClick={() => {
                                      if (selectedStatus) {
                                        handleStatusChange(reservation.id, reservation.userId, selectedStatus);
                                      }
                                    }}
                                    disabled={!selectedStatus || isStatusChanging}
                                  >
                                    {isStatusChanging ? '변경중...' : '변경'}
                                  </Button>
                                </div>
                              </PopoverContent>
                            </Popover>

                            {reservation.status === 'COMPLETED' && (
                              <Button variant="outline" onClick={() => handleNextReservationClick(reservation)}>
                                <ClipboardPlus />
                              </Button>
                            )}
                          </TableCell>
                          <TableCell onClick={() => handleNoteClick(reservation)} className="">
                            {reservation.note}
                          </TableCell>
                        </TableRow>
                      ))}
                    </>
                  );
                })
              )}
            </TableBody>
          </Table>
        )}
      </div>

      {/* 다음 예약 설정 모달 */}
      <Dialog open={isNextReservationModalOpen} onOpenChange={setIsNextReservationModalOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>다음 예약 설정</DialogTitle>
            <DialogDescription>
              {currentReservationForNext
                ? `${currentReservationForNext.userName} 님의 다음 예약을 설정합니다.`
                : '새로운 예약을 설정합니다.'}
            </DialogDescription>
          </DialogHeader>
          {isNextReservationModalLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
              <span className="ml-2 text-sm text-gray-500">정보를 불러오는 중...</span>
            </div>
          ) : (
            <>
              <ReservationForm
                hospitalId={hospitalId}
                treatmentProducts={treatmentProducts}
                businessHours={businessHours}
                forAdmin={true}
                onSubmit={handleReservationSubmit}
              />
              <DialogFooter>
                <DialogClose asChild>
                  <Button type="button" variant="secondary" disabled={isSubmittingNextReservation}>
                    취소
                  </Button>
                </DialogClose>
                <Button type="submit" form="reservation-form" disabled={isSubmittingNextReservation}>
                  {isSubmittingNextReservation ? '예약 신청중...' : '예약 신청'}
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>

      {/* 노트 수정 모달 */}
      <Dialog open={isNoteModalOpen} onOpenChange={setIsNoteModalOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>내부 메모 수정</DialogTitle>
            <DialogDescription>
              {selectedReservationForNote
                ? `${selectedReservationForNote.userName} 님의 예약에 대한 내부 메모를 작성하거나 수정합니다.`
                : ''}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <Textarea
              id="internal-note"
              value={currentNote}
              onChange={(e) => setCurrentNote(e.target.value)}
              className="col-span-4"
              rows={5}
              placeholder="병원 내부에서만 확인 가능한 메모를 입력하세요."
            />
          </div>
          <DialogFooter>
            <DialogClose asChild>
              <Button type="button" variant="secondary">
                취소
              </Button>
            </DialogClose>
            <Button type="button" onClick={handleSaveNote}>
              저장
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* 담당의사 배정 모달 */}
      <Dialog open={isDoctorAssignModalOpen} onOpenChange={setIsDoctorAssignModalOpen}>
        <DialogContent className="sm:max-w-[400px]">
          <DialogHeader>
            <DialogTitle>담당의사 배정</DialogTitle>
            <DialogDescription>
              {selectedReservationForDoctor
                ? `${selectedReservationForDoctor.userName} 님의 예약에 담당의사를 배정합니다.`
                : '담당의사를 선택해주세요.'}
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="space-y-2">
              <Label htmlFor="doctor">담당의사 선택</Label>
              <Select value={selectedDoctorId} onValueChange={setSelectedDoctorId}>
                <SelectTrigger>
                  <SelectValue placeholder="담당의사를 선택하세요" />
                </SelectTrigger>
                <SelectContent>
                  {doctors.map((doctor) => (
                    <SelectItem key={doctor.id} value={doctor.id}>
                      {doctor.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">취소</Button>
            </DialogClose>
            <Button onClick={handleAssignDoctor} disabled={!selectedDoctorId}>
              배정
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
