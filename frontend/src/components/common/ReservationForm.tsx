import { useState, useEffect } from 'react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { Button } from '@/components/ui/button';
import { format } from 'date-fns';
import { ko } from 'date-fns/locale';
import { CalendarIcon } from 'lucide-react';
import { cn } from '@/lib/utils';

interface Props {
  hospitalId: string;
  treatmentProducts: TreatmentProduct[];
  businessHours: BusinessHour[];
  forAdmin: boolean;
  onSubmit: (data: any) => void;
  initialValues?: {
    selectedProduct?: string;
    selectedDate?: Date;
    selectedStartTime?: string;
    selectedEndTime?: string;
    userMemo?: string;
    note?: string;
  };
  onFormChange?: (formData: any) => void;
}
export default function ReservationForm({
  hospitalId,
  treatmentProducts,
  businessHours,
  forAdmin,
  onSubmit,
  initialValues,
  onFormChange,
}: Props) {
  const [selectedProduct, setSelectedProduct] = useState<string>(initialValues?.selectedProduct || '');
  const [selectedDate, setSelectedDate] = useState<Date | undefined>(initialValues?.selectedDate || new Date());
  const [allTimePoints, setAllTimePoints] = useState<string[]>([]);
  const [selectedStartTime, setSelectedStartTime] = useState<string>(initialValues?.selectedStartTime || '');
  const [selectedEndTime, setSelectedEndTime] = useState<string>(initialValues?.selectedEndTime || '');
  const [userMemo, setUserMemo] = useState<string>(initialValues?.userMemo || '');
  const [note, setNote] = useState<string>(initialValues?.note || '');
  const [isCalendarOpen, setIsCalendarOpen] = useState(false);

  const dayOfWeekMap = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

  // 특정 날짜가 휴무일인지 확인
  const isDateDisabled = (date: Date) => {
    const dayName = dayOfWeekMap[date.getDay()];
    const operatingDay = businessHours.find((bh) => bh.dayOfWeek === dayName);
    return operatingDay?.closed || false;
  };

  // 날짜 선택 핸들러 (선택 후 캘린더 닫기)
  const handleDateSelect = (date: Date | undefined) => {
    setSelectedDate(date);
    setIsCalendarOpen(false);
  };

  useEffect(() => {
    if (!selectedDate || businessHours.length === 0) {
      setAllTimePoints([]);
      return;
    }

    const dayName = dayOfWeekMap[selectedDate.getDay()];
    const operatingDay = businessHours.find((bh) => bh.dayOfWeek === dayName);

    if (!operatingDay || operatingDay.closed) {
      setAllTimePoints([]);
      return;
    }

    const generateAllTimePoints = () => {
      const points = [];
      const interval = 30; // 30분 간격

      const dateString = selectedDate.toISOString().split('T')[0];
      const currentTime = new Date(`${dateString}T${operatingDay.openTime}`);
      const closeTime = new Date(`${dateString}T${operatingDay.closeTime}`);
      const breakStartTime = operatingDay.breakStartTime
        ? new Date(`${dateString}T${operatingDay.breakStartTime}`)
        : null;
      const breakEndTime = operatingDay.breakEndTime ? new Date(`${dateString}T${operatingDay.breakEndTime}`) : null;

      while (currentTime <= closeTime) {
        const isBreakTime =
          breakStartTime && breakEndTime && currentTime >= breakStartTime && currentTime < breakEndTime;
        points.push(currentTime.toTimeString().slice(0, 5));
        currentTime.setMinutes(currentTime.getMinutes() + interval);
      }
      return points;
    };

    setAllTimePoints(generateAllTimePoints());
    setSelectedStartTime('');
    setSelectedEndTime('');
  }, [selectedDate, businessHours]);

  useEffect(() => {
    setSelectedEndTime('');
  }, [selectedStartTime]);

  // 폼 데이터 변경 시 부모 컴포넌트에 알림
  useEffect(() => {
    if (onFormChange) {
      onFormChange({
        selectedProduct,
        selectedDate,
        selectedStartTime,
        selectedEndTime,
        userMemo,
        note,
      });
    }
  }, [selectedProduct, selectedDate, selectedStartTime, selectedEndTime, userMemo, note, onFormChange]);

  const availableStartTimes = allTimePoints.slice(0, -1);
  const availableEndTimes = selectedStartTime ? allTimePoints.filter((slot) => slot > selectedStartTime) : [];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProduct || !selectedDate || !selectedStartTime || !selectedEndTime) {
      alert('상품, 날짜, 시작 및 종료 시간을 모두 선택해주세요.');
      return;
    }

    // 한국 시간대로 날짜를 포맷 (UTC 변환 방지)
    const year = selectedDate.getFullYear();
    const month = String(selectedDate.getMonth() + 1).padStart(2, '0');
    const day = String(selectedDate.getDate()).padStart(2, '0');
    const dateString = `${year}-${month}-${day}`;

    const startTimeISO = `${dateString}T${selectedStartTime}:00.000+09:00`;
    const endTimeISO = `${dateString}T${selectedEndTime}:00.000+09:00`;

    if (forAdmin) {
      onSubmit({
        hospitalId,
        treatmentProductId: selectedProduct,
        startTime: startTimeISO,
        endTime: endTimeISO,
        note,
      });
    } else {
      onSubmit({
        hospitalId,
        treatmentProductId: selectedProduct,
        startTime: startTimeISO,
        endTime: endTimeISO,
        userMemo,
      });
    }
  };

  return (
    <form id="reservation-form" onSubmit={handleSubmit} className="grid gap-4 py-4">
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">상담/진료 선택</label>
        <Select onValueChange={setSelectedProduct} value={selectedProduct}>
          <SelectTrigger>
            <SelectValue placeholder="예약할 상품을 선택하세요" />
          </SelectTrigger>
          <SelectContent>
            {treatmentProducts.map((p) => (
              <SelectItem key={p.id} value={p.id}>
                {p.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-700 mb-1">날짜 선택</label>
        <Popover open={isCalendarOpen} onOpenChange={setIsCalendarOpen}>
          <PopoverTrigger asChild>
            <Button
              variant={'outline'}
              className={cn('w-full justify-start text-left font-normal', !selectedDate && 'text-muted-foreground')}
            >
              <CalendarIcon className="mr-2 h-4 w-4" />
              {selectedDate ? (
                format(selectedDate, 'yyyy년 M월 d일 (EEE)', { locale: ko })
              ) : (
                <span>날짜를 선택하세요</span>
              )}
            </Button>
          </PopoverTrigger>
          <PopoverContent className="w-auto p-0">
            <Calendar
              mode="single"
              selected={selectedDate}
              onSelect={handleDateSelect}
              disabled={(date) => date < new Date() || isDateDisabled(date)}
              locale={ko}
              initialFocus
            />
          </PopoverContent>
        </Popover>
      </div>
      <div className="grid grid-cols-2 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">시작 시간</label>
          <Select
            onValueChange={setSelectedStartTime}
            value={selectedStartTime}
            disabled={availableStartTimes.length === 0}
          >
            <SelectTrigger>
              <SelectValue placeholder={availableStartTimes.length > 0 ? '선택' : '예약 가능한 시간 없음'} />
            </SelectTrigger>
            <SelectContent>
              {availableStartTimes.length > 0 ? (
                availableStartTimes.map((time) => (
                  <SelectItem key={time} value={time}>
                    {time}
                  </SelectItem>
                ))
              ) : (
                <SelectItem value="no-slots" disabled>
                  시간 없음
                </SelectItem>
              )}
            </SelectContent>
          </Select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">종료 시간</label>
          <Select
            onValueChange={setSelectedEndTime}
            value={selectedEndTime}
            disabled={!selectedStartTime || availableEndTimes.length === 0}
          >
            <SelectTrigger>
              <SelectValue placeholder={availableEndTimes.length > 0 ? '선택' : '선택'} />
            </SelectTrigger>
            <SelectContent>
              {availableEndTimes.length > 0 ? (
                availableEndTimes.map((time) => (
                  <SelectItem key={time} value={time}>
                    {time}
                  </SelectItem>
                ))
              ) : (
                <SelectItem value="no-slots" disabled>
                  시간 없음
                </SelectItem>
              )}
            </SelectContent>
          </Select>
        </div>
      </div>
      {!forAdmin && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">메모 (선택)</label>
          <Textarea
            placeholder="의사에게 전달할 특별한 요청사항이 있나요?"
            value={userMemo}
            onChange={(e) => setUserMemo(e.target.value)}
          />
        </div>
      )}
      {forAdmin && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">관리자 노트</label>
          <Textarea placeholder="진료 내용" value={note} onChange={(e) => setNote(e.target.value)} />
        </div>
      )}
    </form>
  );
}
