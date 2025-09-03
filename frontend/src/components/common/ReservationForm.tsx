import { useState, useEffect } from 'react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';

interface Props {
  hospitalId: string;
  treatmentProducts: TreatmentProduct[];
  businessHours: BusinessHour[];
  forAdmin: boolean;
  onSubmit: (data: any) => void;
}
export default function ReservationForm({ hospitalId, treatmentProducts, businessHours, forAdmin, onSubmit }: Props) {
  const [selectedProduct, setSelectedProduct] = useState<string>('');
  const [selectedDate, setSelectedDate] = useState<string>(new Date().toISOString().split('T')[0]);
  const [allTimePoints, setAllTimePoints] = useState<string[]>([]);
  const [selectedStartTime, setSelectedStartTime] = useState<string>('');
  const [selectedEndTime, setSelectedEndTime] = useState<string>('');
  const [userMemo, setUserMemo] = useState<string>('');
  const [note, setNote] = useState<string>('');

  const dayOfWeekMap = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

  useEffect(() => {
    if (!selectedDate || businessHours.length === 0) {
      setAllTimePoints([]);
      return;
    }

    const dayName = dayOfWeekMap[new Date(selectedDate).getDay()];
    const operatingDay = businessHours.find((bh) => bh.dayOfWeek === dayName);

    if (!operatingDay || operatingDay.closed) {
      setAllTimePoints([]);
      return;
    }

    const generateAllTimePoints = () => {
      const points = [];
      const interval = 30; // 30분 간격
      let currentTime = new Date(`${selectedDate}T${operatingDay.openTime}`);
      const closeTime = new Date(`${selectedDate}T${operatingDay.closeTime}`);
      const breakStartTime = new Date(`${selectedDate}T${operatingDay.breakStartTime}`);
      const breakEndTime = new Date(`${selectedDate}T${operatingDay.breakEndTime}`);

      while (currentTime <= closeTime) {
        const isBreakTime = currentTime >= breakStartTime && currentTime < breakEndTime;
        if (!isBreakTime) {
          points.push(currentTime.toTimeString().slice(0, 5));
        }
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

  const availableStartTimes = allTimePoints.slice(0, -1);
  const availableEndTimes = selectedStartTime ? allTimePoints.filter((slot) => slot > selectedStartTime) : [];

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProduct || !selectedDate || !selectedStartTime || !selectedEndTime) {
      alert('상품, 날짜, 시작 및 종료 시간을 모두 선택해주세요.');
      return;
    }

    if (forAdmin) {
      onSubmit({
        hospitalId,
        treatmentProductId: selectedProduct,
        startTime: new Date(`${selectedDate}T${selectedStartTime}`).toISOString(),
        endTime: new Date(`${selectedDate}T${selectedEndTime}`).toISOString(),
        note,
      });
    } else {
      onSubmit({
        hospitalId,
        treatmentProductId: selectedProduct,
        startTime: new Date(`${selectedDate}T${selectedStartTime}`).toISOString(),
        endTime: new Date(`${selectedDate}T${selectedEndTime}`).toISOString(),
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
        <Input
          type="date"
          value={selectedDate}
          onChange={(e) => setSelectedDate(e.target.value)}
          min={new Date().toISOString().split('T')[0]}
        />
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
