import { useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog';
import { getReservations, getReservationById, getReservationEvents } from '@/api/reservation';
import { getBusinessHoursByHospital } from '@/api/businessHour';
import { getTreatmentProductsByHospital } from '@/api/treatmentProduct';

interface ServiceSlot {
  userName?: string;
  slotStatus: 'available' | 'booked' | 'blocked' | 'break';
  type?: string;
  slotNumber: number; // 슬롯 번호 (1, 2, 3...)
  startTime?: string; // 예약 시작 시간
  endTime?: string; // 예약 종료 시간
  appointmentId?: string; // 같은 예약을 식별하기 위한 ID
}

interface TimeSlot {
  time: string;
  services: {
    [key: string]: ServiceSlot[]; // 각 서비스별로 여러 슬롯 배열
  };
}

interface Service {
  id: string;
  name: string;
  color: string;
  duration: number;
  capacity: number; // 동시 처리 가능 인원 수
}

interface Reservation {
  id: string;
  productId: string;
  startTime: Date;
  endTime: Date;
  status: string;
  userName: string;
  treatmentProductName?: string;
  memo?: string;
  note?: string;
}

interface BusinessHour {
  id: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  openTime: string; // "HH:mm:ss"
  closeTime: string; // "HH:mm:ss"
  breakStartTime: string;
  breakEndTime: string;
  closed: boolean;
}

interface Props {
  hospitalId;
  selectedDate;
}
export default function MedicalRecordTimeTable({ hospitalId, selectedDate }: Props) {
  const slotWidth = 100;
  const boxHeight = 48;

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedReservation, setSelectedReservation] = useState<Reservation | null>(null);
  const [eventLog, setEventLog] = useState<Event[]>([]);
  const [timeSlots, setTimeSlots] = useState<string[]>([]);
  const [scheduleData, setScheduleData] = useState<TimeSlot[]>([]);
  const [services, setServices] = useState<Service[]>([]);
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [businessHours, setBusinessHours] = useState<BusinessHours[]>([]);
  const [currentTime, setCurrentTime] = useState(new Date());
  const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

  // 현재 시간을 1분마다 업데이트
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 60000); // 1분마다 업데이트

    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    const generateTimeSlots = () => {
      if (businessHours.length === 0) {
        setTimeSlots([]);
        return;
      }
      const dayOfWeek = days[new Date(selectedDate).getDay()];

      const todayBusinessHour = businessHours.find((bh) => bh.dayOfWeek === dayOfWeek);

      if (!todayBusinessHour || todayBusinessHour.closed) {
        setTimeSlots([]);
        return;
      }

      const slots = [];
      const [openHour, openMinute] = todayBusinessHour.openTime.split(':').map(Number);
      const [closeHour, closeMinute] = todayBusinessHour.closeTime.split(':').map(Number);

      const currentTime = new Date(selectedDate);
      currentTime.setHours(openHour, openMinute, 0, 0);

      const closeTime = new Date(selectedDate);
      closeTime.setHours(closeHour, closeMinute, 0, 0);

      while (currentTime.getTime() <= closeTime.getTime()) {
        slots.push(
          currentTime.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false,
          }),
        );
        currentTime.setMinutes(currentTime.getMinutes() + 30);
      }
      setTimeSlots(slots);
    };
    generateTimeSlots();
  }, [selectedDate, businessHours]);

  useEffect(() => {
    const createServiceSlots = (capacity: number): ServiceSlot[] => {
      const slots: ServiceSlot[] = [];
      for (let i = 1; i <= capacity; i++) {
        slots.push({ slotStatus: 'available', slotNumber: i });
      }
      return slots;
    };

    const dayOfWeek = days[new Date(selectedDate).getDay()];
    const todayBusinessHour = businessHours.find((bh) => bh.dayOfWeek === dayOfWeek);

    // 예약 ID별로 슬롯 인덱스를 저장하는 Map (service ID -> reservation ID -> slot index)
    const reservationSlotMap = new Map<string, Map<string, number>>();

    // 먼저 모든 예약에 대해 슬롯을 미리 할당
    services.forEach((service) => {
      if (!reservationSlotMap.has(service.id)) {
        reservationSlotMap.set(service.id, new Map<string, number>());
      }
      const serviceReservationMap = reservationSlotMap.get(service.id)!;

      // 해당 서비스의 모든 예약을 시작 시간 순으로 정렬
      const serviceReservations = reservations
        .filter((r) => r.productId === service.id)
        .filter((r) => r.status !== 'CANCELED')
        .sort((a, b) => a.startTime.getTime() - b.startTime.getTime());

      serviceReservations.forEach((reservation) => {
        let slotIndex = -1;

        // 기존에 다른 예약과 겹치지 않는 슬롯 찾기
        for (let i = 0; ; i++) {
          let hasConflict = false;

          // 같은 슬롯에 이미 할당된 다른 예약과 시간이 겹치는지 확인
          for (const [otherId, otherSlotIndex] of serviceReservationMap.entries()) {
            if (otherSlotIndex === i) {
              // 같은 슬롯에 있는 다른 예약 찾기
              const otherReservation = serviceReservations.find((r) => r.id === otherId);
              if (otherReservation) {
                // 시간 겹침 확인
                if (
                  reservation.startTime < otherReservation.endTime &&
                  reservation.endTime > otherReservation.startTime
                ) {
                  hasConflict = true;
                  break;
                }
              }
            }
          }

          if (!hasConflict) {
            slotIndex = i;
            serviceReservationMap.set(reservation.id, slotIndex);
            break;
          }
        }
      });
    });

    const newScheduleData: TimeSlot[] = timeSlots.map((time) => {
      const timeSlot: TimeSlot = { time, services: {} };
      const [hours, minutes] = time.split(':').map(Number);
      const slotTime = new Date(selectedDate);
      slotTime.setHours(hours, minutes, 0, 0);

      let isBreakTime = false;
      if (todayBusinessHour && !todayBusinessHour.closed) {
        const [breakStartHour, breakStartMinute] = todayBusinessHour.breakStartTime.split(':').map(Number);
        const [breakEndHour, breakEndMinute] = todayBusinessHour.breakEndTime.split(':').map(Number);
        const breakStartTime = new Date(selectedDate);
        breakStartTime.setHours(breakStartHour, breakStartMinute, 0, 0);
        const breakEndTime = new Date(selectedDate);
        breakEndTime.setHours(breakEndHour, breakEndMinute, 0, 0);
        isBreakTime = slotTime >= breakStartTime && slotTime < breakEndTime;
      }

      services.forEach((service) => {
        const serviceReservationMap = reservationSlotMap.get(service.id)!;

        // 해당 시간대에 있는 예약 수 계산
        const reservationsInThisTime = reservations.filter(
          (reservation) =>
            reservation.productId === service.id && slotTime >= reservation.startTime && slotTime < reservation.endTime,
        );

        // 현재 시간에 사용되는 최대 슬롯 번호 찾기
        let maxUsedSlot = service.capacity - 1;
        reservationsInThisTime.forEach((reservation) => {
          const slotIndex = serviceReservationMap.get(reservation.id);
          if (slotIndex !== undefined && slotIndex > maxUsedSlot) {
            maxUsedSlot = slotIndex;
          }
        });

        // 필요한 슬롯 수 = 기본 capacity와 실제 사용된 슬롯 수 중 큰 값
        const requiredSlots = Math.max(service.capacity, maxUsedSlot + 1);
        let serviceSlots = createServiceSlots(requiredSlots);

        if (isBreakTime) {
          serviceSlots = serviceSlots.map((s) => ({ ...s, slotStatus: 'break' }));
        } else {
          // 현재 시간에 해당하는 예약들을 미리 할당된 슬롯에 배치
          reservationsInThisTime.forEach((reservation) => {
            const slotIndex = serviceReservationMap.get(reservation.id);
            if (slotIndex !== undefined) {
              const slotData = {
                ...serviceSlots[slotIndex],
                slotStatus: 'booked' as const,
                userName: reservation.userName,
                reservationStatus: reservation.status,
                type: service.name,
                startTime: reservation.startTime.toLocaleTimeString('ko-KR', {
                  hour: '2-digit',
                  minute: '2-digit',
                  hour12: false,
                }),
                endTime: reservation.endTime.toLocaleTimeString('ko-KR', {
                  hour: '2-digit',
                  minute: '2-digit',
                  hour12: false,
                }),
                appointmentId: reservation.id,
              };
              serviceSlots[slotIndex] = slotData;
            }
          });
        }
        timeSlot.services[service.id] = serviceSlots;
      });

      return timeSlot;
    });
    setScheduleData(newScheduleData);
  }, [services, reservations, selectedDate, timeSlots, businessHours]);

  useEffect(() => {
    const fetchBusinessHours = async () => {
      if (!hospitalId) return;
      try {
        const response = await getBusinessHoursByHospital(hospitalId);
        setBusinessHours(response.body);
      } catch (error) {
        console.error('Error fetching business hours:', error);
      }
    };
    fetchBusinessHours();
  }, [hospitalId]);

  useEffect(() => {
    const fetchTreatmentProducts = async () => {
      if (!hospitalId) return;
      try {
        const response = await getTreatmentProductsByHospital(hospitalId);
        const fetchedServices = response.body.map((product: any) => ({
          id: product.id.toString(),
          name: product.name,
          color: 'bg-blue-100 text-blue-800', // Default color
          duration: product.duration,
          capacity: product.maxCapacity,
        }));
        setServices(fetchedServices);
      } catch (error) {
        console.error('Error fetching treatment products:', error);
      }
    };
    fetchTreatmentProducts();
  }, [hospitalId]);

  useEffect(() => {
    const fetchReservations = async () => {
      if (!hospitalId) return;
      try {
        const startDate = new Date(selectedDate);
        startDate.setHours(0, 0, 0, 0);
        const endDate = new Date(selectedDate);
        endDate.setHours(23, 59, 59, 999);

        const response = await getReservations(hospitalId, startDate.toISOString(), endDate.toISOString());
        const fetchedReservations = response.body.map((reservation: any) => ({
          id: reservation.id.toString(),
          productId: reservation.treatmentProductId.toString(),
          startTime: new Date(reservation.startTime),
          endTime: new Date(reservation.endTime),
          userName: reservation.userName,
          status: reservation.status,
        }));
        setReservations(fetchedReservations);
      } catch (error) {
        console.error('Error fetching reservations:', error);
      }
    };
    fetchReservations();
  }, [hospitalId, selectedDate]);

  const isStartOfAppointment = (slot: ServiceSlot, timeSlot: TimeSlot) => {
    return slot.startTime === timeSlot.time;
  };

  const getMaxSlotsForService = (serviceId: string) => {
    let maxSlots = 0;
    scheduleData.forEach((timeSlot) => {
      const slots = timeSlot.services[serviceId];
      if (slots) {
        const activeSlots = slots.filter(
          (slot) => slot.slotStatus === 'booked' || slot.slotStatus === 'blocked',
        ).length;
        maxSlots = Math.max(maxSlots, activeSlots);
      }
    });
    // Return at least 1 to ensure the column is always visible
    return Math.max(maxSlots, 1);
  };

  const getSlotContent = (serviceId: string, timeSlot: TimeSlot) => {
    const slots = timeSlot.services[serviceId];
    const service = services.find((s) => s.id === serviceId);
    if (!slots || !service) {
      return null;
    }
    const maxSlots = getMaxSlotsForService(serviceId);
    // 모든 booked 또는 blocked 슬롯을 표시하되, maxSlots와 실제 사용된 슬롯 수 중 큰 값 사용
    const bookedSlots = slots.filter((slot) => slot.slotStatus === 'booked' || slot.slotStatus === 'blocked').length;
    const slotsToShow = Math.max(maxSlots, bookedSlots);
    const activeSlots = slots.slice(0, slotsToShow);

    const firstSlot = activeSlots[0];
    if (firstSlot && firstSlot.slotStatus === 'break') {
      const dayOfWeek = days[new Date(selectedDate).getDay()];
      const todayBusinessHour = businessHours.find((bh) => bh.dayOfWeek === dayOfWeek);

      if (todayBusinessHour && !todayBusinessHour.closed) {
        const breakStartTimeString = todayBusinessHour.breakStartTime.substring(0, 5);
        if (timeSlot.time === breakStartTimeString) {
          const breakEndTimeString = todayBusinessHour.breakEndTime.substring(0, 5);
          const breakStart = new Date(`${selectedDate}T${breakStartTimeString}`);
          const breakEnd = new Date(`${selectedDate}T${breakEndTimeString}`);
          const durationMinutes = (breakEnd.getTime() - breakStart.getTime()) / (1000 * 60);
          const durationSlots = durationMinutes > 0 ? Math.ceil(durationMinutes / 30) : 1;
          const height = durationSlots * boxHeight;

          return (
            <div className="relative">
              <div
                className="absolute bg-gray-100 border border-gray-200 text-sm flex items-center justify-center"
                style={{ height: `${height}px`, width: `100%`, opacity: 1 }}
              >
                <span className="text-gray-500">쉬는 시간</span>
              </div>
              <div className="h-[48px]"></div>
            </div>
          );
        } else {
          return <div className="h-[48px]"></div>;
        }
      }
    }

    return (
      <div className="relative h-[48px]">
        {activeSlots.map((slot, index) => {
          const isStart = isStartOfAppointment(slot, timeSlot);

          // 예약이 있고 해당 시간이 시작 시간인 경우에만 렌더링
          if (slot.appointmentId && slot.startTime && slot.endTime && isStart) {
            const startTime = new Date(`${selectedDate}T${slot.startTime}`);
            const endTime = new Date(`${selectedDate}T${slot.endTime}`);
            const durationMinutes = (endTime.getTime() - startTime.getTime()) / (1000 * 60);
            const durationSlots = durationMinutes > 0 ? Math.ceil(durationMinutes / 30) : 1;
            const height = durationSlots * boxHeight; //
            var borderColor = slot.reservationStatus === 'PENDING' ? 'border-gray-300' : 'border-blue-300';
            borderColor = slot.reservationStatus === 'COMPLETED' ? 'border-green-300' : borderColor;

            return (
              <div
                key={slot.appointmentId || `${serviceId}-${timeSlot.time}-${index}`}
                className={`absolute z-10 p-2 border-2 ${borderColor} bg-white rounded text-xs flex flex-col justify-center cursor-pointer`}
                style={{
                  height: `${height}px`,
                  width: `${slotWidth}px`,
                  left: `${(slot.slotNumber - 1) * slotWidth}px`,
                  top: 0,
                  opacity: 0.8,
                }}
                onClick={async () => {
                  if (slot.appointmentId) {
                    try {
                      const reservationResponse = await getReservationById(slot.appointmentId);
                      setSelectedReservation(reservationResponse.body);

                      const eventsResponse = await getReservationEvents(slot.appointmentId);
                      setEventLog(eventsResponse.body.reverse());

                      setIsModalOpen(true);
                    } catch (error) {
                      console.error('Error fetching reservation details:', error);
                    }
                  }
                }}
              >
                <div className="font-medium truncate text-foreground">{slot.userName}</div>
                <div className="text-muted-foreground truncate">{slot.type}</div>
                <div className="text-xs truncate">
                  {slot.startTime}~{slot.endTime}
                </div>
              </div>
            );
          }
          return null;
        })}
      </div>
    );
  };

  const getEventContent = (event, index) => {
    let eventMessage = '알 수 없는 이벤트';
    let eventDetailContent = null;

    switch (event.eventType) {
      case 'ReservationCreated':
        eventMessage = '예약이 접수되었습니다.';
        eventDetailContent = (
          <div className="p-2 space-y-1">
            {event.payload?.startTime && (
              <div>
                <span className="font-medium">시작시간:</span> {new Date(event.payload.startTime).toLocaleString()}
              </div>
            )}
            {event.payload?.endTime && (
              <div>
                <span className="font-medium">종료시간:</span> {new Date(event.payload.endTime).toLocaleString()}
              </div>
            )}
            {event.payload?.memo && (
              <div>
                <span className="font-medium">접수내용:</span> {event.payload.memo}
              </div>
            )}
          </div>
        );
        break;

      case 'NoteUpdated':
        eventMessage = '상담 내용이 수정되었습니다.';
        eventDetailContent = (
          <div className="p-2 space-y-1">
            {event.payload?.note && (
              <div>
                <span className="font-medium">상담내용:</span> {event.payload.note}
              </div>
            )}
            {event.payload?.updatedBy && (
              <div>
                <span className="font-medium">수정자:</span> {event.payload.updatedBy}
              </div>
            )}
          </div>
        );
        break;

      case 'Confirmed':
        eventMessage = '예약이 확정되었습니다.';
        eventDetailContent = (
          <div className="p-2 space-y-1">
            {event.payload?.adminName && (
              <div>
                <span className="font-medium">작업자:</span> {event.payload.adminName}
              </div>
            )}
          </div>
        );
        break;

      case 'Completed':
        eventMessage = '완료 처리 되었습니다.';
        eventDetailContent = (
          <div className="p-2 space-y-1">
            {event.payload?.adminName && (
              <div>
                <span className="font-medium">작업자:</span> {event.payload.adminName}
              </div>
            )}
          </div>
        );
        break;

      case 'DoctorAssigned':
        eventMessage = '담당의사가 배정되었습니다.';
        eventDetailContent = (
          <div className="p-2 space-y-1">
            {event.payload?.doctorName && (
              <div>
                <span className="font-medium">담당의사:</span> {event.payload.doctorName}
              </div>
            )}
            {event.payload?.adminName && (
              <div>
                <span className="font-medium">배정자:</span> {event.payload.adminName}
              </div>
            )}
          </div>
        );
        break;

      case 'Canceled':
        eventMessage = '예약이 취소되었습니다.';
        eventDetailContent = (
          <div className="p-2 space-y-1">
            {event.payload?.adminName && (
              <div>
                <span className="font-medium">작업자:</span> {event.payload.adminName}
              </div>
            )}
          </div>
        );
        break;

      case 'Pending':
        eventMessage = '예약이 대기되었습니다.';
        eventDetailContent = (
          <div className="p-2 space-y-1">
            {event.payload?.adminName && (
              <div>
                <span className="font-medium">작업자:</span> {event.payload.adminName}
              </div>
            )}
          </div>
        );
        break;

      case 'Rescheduled':
        eventMessage = '예약 시간이 변경되었습니다.';
        eventDetailContent = (
          <div className="p-2 space-y-1">
            {event.payload?.originalTime && (
              <div>
                <span className="font-medium">기존시간:</span> {new Date(event.payload.originalTime).toLocaleString()}
              </div>
            )}
            {event.payload?.newTime && (
              <div>
                <span className="font-medium">변경시간:</span> {new Date(event.payload.newTime).toLocaleString()}
              </div>
            )}
            {event.payload?.reason && (
              <div>
                <span className="font-medium">변경사유:</span> {event.payload.reason}
              </div>
            )}
          </div>
        );
        break;

      default:
        eventMessage = `${event.eventType || '알 수 없는 이벤트'}`;
        eventDetailContent = (
          <div className="p-2">
            <div className="text-xs text-gray-500">
              {event.payload ? JSON.stringify(event.payload, null, 2) : '상세 정보 없음'}
            </div>
          </div>
        );
        break;
    }

    return (
      <div key={index} className="text-sm">
        <div className="flex justify-between items-start">
          <div className="font-medium text-gray-900">{eventMessage}</div>
          <div className="text-gray-500 text-xs">{new Date(event.createdAt).toLocaleString()}</div>
        </div>
        {eventDetailContent && <div className="mt-2 bg-gray-50 border rounded-md">{eventDetailContent}</div>}
      </div>
    );
  };

  // 현재 시간이 선택된 날짜와 같은 날인지 확인하고, 현재 시간의 픽셀 위치 계산
  const getCurrentTimePosition = () => {
    const selectedDateOnly = new Date(selectedDate).toDateString();
    const currentDateOnly = currentTime.toDateString();

    // 선택된 날짜가 오늘이 아니면 현재 시간선을 표시하지 않음
    if (selectedDateOnly !== currentDateOnly) {
      return null;
    }

    const currentHours = currentTime.getHours();
    const currentMinutes = currentTime.getMinutes();

    // 현재 시간이 영업시간 내에 있는지 확인
    const dayOfWeek = days[currentTime.getDay()];
    const todayBusinessHour = businessHours.find((bh) => bh.dayOfWeek === dayOfWeek);

    const [openHour, openMinute] = todayBusinessHour.openTime.split(':').map(Number);
    const [closeHour, closeMinute] = todayBusinessHour.closeTime.split(':').map(Number);

    const currentTotalMinutes = currentHours * 60 + currentMinutes;
    const openTotalMinutes = openHour * 60 + openMinute;
    const closeTotalMinutes = closeHour * 60 + closeMinute;

    // 현재 시간이 영업시간 밖이면 표시하지 않음
    if (currentTotalMinutes < openTotalMinutes || currentTotalMinutes > closeTotalMinutes) {
      return null;
    }

    // 영업 시작 시간부터 현재 시간까지의 분 차이 계산
    const minutesFromStart = currentTotalMinutes - openTotalMinutes;

    // 현재 시간이 속한 30분 슬롯과 슬롯 내 위치 계산
    const slotIndex = Math.floor(minutesFromStart / 30); // 몇 번째 슬롯인지
    const minutesIntoSlot = minutesFromStart % 30; // 해당 슬롯에서 몇 분째인지

    // 슬롯 내 픽셀 위치 계산 (30분 = 48px이므로, 1분 = 1.6px)
    const pixelsIntoSlot = (minutesIntoSlot / 30) * boxHeight;

    // 픽셀 위치 계산 (헤더 높이 + 슬롯들의 높이 + 슬롯 내 위치)
    const position = slotIndex * boxHeight + pixelsIntoSlot;
    return position;
  };

  const getCurrentTimeLine = () => {
    const currentTimePosition = getCurrentTimePosition();
    if (currentTimePosition === null) return null;

    // 전체 테이블 너비 계산
    const totalWidth =
      80 + // w-20
      services.reduce((sum, service) => {
        return sum + getMaxSlotsForService(service.id) * slotWidth;
      }, 0);

    return (
      <div
        className="absolute z-20 pointer-events-none"
        style={{
          top: `${currentTimePosition}px`,
          left: '0px',
          width: `${totalWidth}px`,
          height: '2px',
          backgroundColor: '#ef4444',
          boxShadow: '0 0 4px rgba(239, 68, 68, 0.5)',
        }}
      >
        {/* 현재 시간 표시 레이블 */}
        <div
          className="absolute text-xs font-medium text-red-500 bg-white px-1 rounded border border-red-300"
          style={{
            opacity: 0.7,
            transform: 'translateX(-50%)',
            left: '50%',
            fontSize: '10px',
            lineHeight: '14px',
          }}
        >
          {currentTime.toLocaleTimeString('ko-KR', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: false,
          })}
        </div>
      </div>
    );
  };

  return (
    <>
      {/* 타임테이블 */}
      <div className="flex flex-col h-full relative border border-1">
        {/* 테이블 헤더 */}
        <div className="flex mb-0 border-b-1">
          <div className="w-20 border-r"></div>
          {services.map((service, serviceIndex) => {
            const maxSlots = getMaxSlotsForService(service.id);
            return (
              <div
                key={service.id}
                className={`${serviceIndex < services.length - 1 ? 'border-r-1 border-r-gray-300' : ''}`}
                style={{
                  width: 'fit-content',
                  minWidth: `${maxSlots * slotWidth}px`,
                }}
              >
                <div className="h-14 relative py-0">
                  <div className="absolute inset-0 flex flex-col justify-center items-center text-center">
                    <div className="font-medium text-sm text-center">{service.name}</div>
                    <div className="text-xs text-muted-foreground text-center">동시 {service.capacity}명</div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* 테이블 내용 */}
        <div className="overflow-y-auto grow relative">
          {scheduleData.map((timeSlot) => (
            <div key={timeSlot.time} className="flex">
              {/* 테이블 시간 표시 */}
              <div className="w-20 bg-muted border-b text-sm items-center text-center">{timeSlot.time}</div>

              {/* 테이블 내용 표시 */}
              {services.map((service, serviceIndex) => {
                const maxSlots = getMaxSlotsForService(service.id);
                return (
                  <div
                    key={service.id}
                    className={`p-0 ${serviceIndex < services.length - 1 ? 'border-r-1 border-r-gray-300' : ''}`}
                    style={{
                      width: 'fit-content',
                      minWidth: `${maxSlots * slotWidth}px`,
                    }}
                  >
                    {getSlotContent(service.id, timeSlot)}
                  </div>
                );
              })}
            </div>
          ))}

          {/* 현재 시간선 */}
          {businessHours.length > 0 && getCurrentTimeLine()}
        </div>
      </div>

      {/* 예약 상세 정보 모달 */}
      <Dialog open={isModalOpen} onOpenChange={setIsModalOpen}>
        <DialogContent className="w-md">
          <DialogHeader>
            <DialogTitle>{selectedReservation?.userName}님 기록</DialogTitle>
          </DialogHeader>
          {selectedReservation && (
            <div className="mt-4 overflow-y-auto" style={{ height: '80vh' }}>
              {eventLog && eventLog.length > 0 ? (
                <div className="space-y-4">{eventLog.map((event, index) => getEventContent(event, index))}</div>
              ) : (
                <p className="text-sm text-muted-foreground">업데이트 기록이 없습니다.</p>
              )}
            </div>
          )}
        </DialogContent>
      </Dialog>
    </>
  );
}
