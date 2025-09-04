import { useState, useEffect, useRef, useCallback } from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Calendar } from '@/components/ui/calendar';
import { ko } from 'date-fns/locale';
import { format } from 'date-fns';
import { Dialog, DialogContent, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import MedicalRecordTimeTable from '@/components/admin/MedicalRecordTimeTable';

interface Props {
  hospitalId: string;
  callbackMonthChange: (month: Date) => void;
  monthlyReservationsData: Record<string, Reservation[]>;
}
export default function MedicalRecordCalendar({ hospitalId, callbackMonthChange, monthlyReservationsData }: Props) {
  const [month, setMonth] = useState<Date>(new Date());
  const [cellSize, setCellSize] = useState(80); // 더 작은 초기값으로 설정
  const [isCalculating, setIsCalculating] = useState(true); // 계산 중 상태
  const containerRef = useRef<HTMLDivElement>(null);

  // 창 크기 변화 감지 및 셀 크기 계산
  const calculateCellSize = useCallback(() => {
    if (!containerRef.current) return;

    setIsCalculating(true); // 계산 시작

    const containerWidth = containerRef.current.offsetWidth;
    const containerClientWidth = containerRef.current.clientWidth;

    // 실제 사용 가능한 너비 계산
    const actualWidth = containerClientWidth || containerWidth;
    const padding = 24; // 카드 패딩
    const availableWidth = actualWidth - padding;
    const calculatedSize = Math.floor(availableWidth / 7);

    // 최소/최대 크기 제한
    const minSize = 50;
    const maxSize = 100;
    const finalSize = Math.max(minSize, Math.min(maxSize, calculatedSize));

    setCellSize(finalSize);
    setIsCalculating(false); // 계산 완료
  }, []);

  useEffect(() => {
    // 초기 계산 (즉시 실행)
    calculateCellSize();

    let rafId: number;
    let resizeTimeout: NodeJS.Timeout;

    const handleResize = () => {
      // 즉시 반응 - requestAnimationFrame 사용
      if (rafId) {
        cancelAnimationFrame(rafId);
      }

      rafId = requestAnimationFrame(() => {
        setIsCalculating(true);
        calculateCellSize();
      });

      // 추가 정리를 위한 디바운싱 (매우 짧게)
      clearTimeout(resizeTimeout);
      resizeTimeout = setTimeout(() => {
        calculateCellSize(); // 최종 정리
      }, 16); // 1프레임 시간
    };

    window.addEventListener('resize', handleResize, { passive: true });
    return () => {
      if (rafId) {
        cancelAnimationFrame(rafId);
      }
      clearTimeout(resizeTimeout);
      window.removeEventListener('resize', handleResize);
    };
  }, [calculateCellSize]);

  return (
    <Card ref={containerRef} className="overflow-hidden ">
      <CardContent className="flex justify-center p-0 overflow-hidden">
        <Calendar
          mode="single"
          month={month}
          onMonthChange={(month) => {
            setMonth(month);
            callbackMonthChange(month);
          }}
          selected={null}
          onSelect={() => {}}
          initialFocus
          locale={ko}
          classNames={{
            today: `border-gray-500 border-t-2`, // Add a border to today's date
            day: `border-t`,
            weekday: `text-muted-foreground rounded-md flex-1 font-normal text-sm select-none text-left pl-1`,
          }}
          components={{
            DayButton: ({ day, modifiers, ...props }) => {
              const date = day.date; // Access the date from the 'day' object
              const dateKey = format(date, 'yyyy-MM-dd');
              const reservationsForDay = monthlyReservationsData[dateKey] || [];
              const withoutCanceledReservation = reservationsForDay.filter((r) => r.status !== 'CANCELED');
              const hasReservations = withoutCanceledReservation.length > 0;
              const isWeekend = date.getDay() === 0 || date.getDay() === 6; // Sunday is 0, Saturday

              return (
                <div
                  className="flex flex-col h-full overflow-hidden"
                  style={{
                    width: `${cellSize}px`,
                    height: `100px`,
                    minWidth: `${cellSize}px`, // 최소 너비 고정
                    minHeight: `100px`, // 최소 높이 고정
                    transition: isCalculating ? 'none' : 'width 0.1s ease-out, height 0.1s ease-out', // 크기 변화만 트랜지션
                    willChange: isCalculating ? 'width, height' : 'auto', // GPU 가속 최적화
                  }}
                >
                  {/* 날짜 숫자 행 */}
                  <div className="flex-shrink-0 p-1 overflow-hidden">
                    <div className="flex items-center justify-between w-full">
                      <span
                        className={`text-sm font-medium truncate ${modifiers.outside ? 'text-gray-300' : ''} ${isWeekend ? 'text-red-500' : ''}`}
                      >
                        {format(date, 'd')}
                      </span>
                      {hasReservations && (
                        <span className="text-xs text-gray-400 ml-1 flex-shrink-0">
                          {withoutCanceledReservation.length}
                        </span>
                      )}
                    </div>
                  </div>

                  {/* 예약 정보 행 */}
                  <div className="flex-1 px-1 pb-1 overflow-hidden">
                    <Dialog>
                      <DialogTrigger className="w-full h-full">
                        {hasReservations ? (
                          <div className="cursor-pointer flex flex-col text-xs space-y-0.5 w-full h-full overflow-hidden">
                            {reservationsForDay.slice(0, 2).map((res) => {
                              let bgColor = res.status === 'PENDING' ? 'bg-gray-200' : 'bg-blue-100';
                              let borderColor = res.status === 'PENDING' ? 'border-gray-300' : 'border-blue-200';
                              let textColor = res.status === 'PENDING' ? 'text-gray-800' : 'text-blue-800';

                              bgColor = res.status === 'CANCELED' ? 'bg-red-200' : bgColor;
                              borderColor = res.status === 'CANCELED' ? 'border-red-300' : borderColor;
                              textColor = res.status === 'CANCELED' ? 'text-red-800' : textColor;

                              bgColor = res.status === 'COMPLETED' ? 'bg-green-200' : bgColor;
                              borderColor = res.status === 'COMPLETED' ? 'border-green-300' : borderColor;
                              textColor = res.status === 'COMPLETED' ? 'text-green-800' : textColor;

                              return (
                                <div
                                  key={res.id}
                                  className={`${bgColor} ${textColor} rounded-md px-1 py-0.5 text-xs border ${borderColor} w-full overflow-hidden flex-shrink-0`}
                                  title={`${res.patientName} (${format(res.startTime, 'HH:mm')})`}
                                  style={{
                                    minHeight: '18px',
                                    maxHeight: '18px',
                                  }}
                                >
                                  <div className="truncate flex items-center justify-between w-full">
                                    <span className="truncate flex-1 min-w-0 text-[10px]">
                                      {res.patientName.length > 3
                                        ? `${res.patientName.slice(0, 3)}...`
                                        : res.patientName}
                                    </span>
                                    <span className="flex-shrink-0 text-[9px] ml-1">
                                      {format(res.startTime, 'HH:mm')}
                                    </span>
                                  </div>
                                </div>
                              );
                            })}
                            {reservationsForDay.length > 2 && (
                              <div className="text-center text-muted-foreground text-[10px] mt-1">
                                +{reservationsForDay.length - 2} 더보기
                              </div>
                            )}
                          </div>
                        ) : (
                          <div className="w-full h-full"></div>
                        )}
                      </DialogTrigger>
                      <DialogContent className="min-w-4xl">
                        <DialogTitle></DialogTitle>
                        <div style={{ height: '80vh' }}>
                          <MedicalRecordTimeTable hospitalId={hospitalId} selectedDate={dateKey} />
                        </div>
                      </DialogContent>
                    </Dialog>
                  </div>
                </div>
              );
            },
          }}
        />
      </CardContent>
    </Card>
  );
}
