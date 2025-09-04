import { useState, useEffect } from 'react';
import { Card, CardHeader, CardTitle, CardContent, CardDescription } from '@/components/ui/card';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogClose } from '@/components/ui/dialog';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { getBusinessHoursByHospital, updateBusinessHour } from '@/api/businessHour';
import { Checkbox } from '@/components/ui/checkbox';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Toaster, toast } from 'sonner';

interface BusinessHour {
  id: string;
  dayOfWeek: 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';
  openTime: string;
  closeTime: string;
  breakStartTime: string;
  breakEndTime: string;
  closed: boolean;
}
interface Props {
  hospitalId;
}
export default function BusinessHourManagement({ hospitalId }: Props) {
  const [businessHours, setBusinessHours] = useState<BusinessHour[]>([]);
  const [isBusinessHourModalOpen, setIsBusinessHourModalOpen] = useState(false);
  const [editingBusinessHour, setEditingBusinessHour] = useState<BusinessHour | null>(null);

  useEffect(() => {
    fetchBusinessHours();
  }, [hospitalId]);

  const fetchBusinessHours = async () => {
    const businessHoursResponse = await getBusinessHoursByHospital(hospitalId);
    if (businessHoursResponse.result.resultCode === 200) {
      setBusinessHours(businessHoursResponse.body || []);
    } else {
      throw new Error(businessHoursResponse.result.resultMessage || 'Failed to fetch business hours.');
    }
  };

  const handleEditBusinessHour = (businessHour: BusinessHour) => {
    // 현재 설정된 영업시간을 복사해서 편집용으로 설정
    console.log('Selected business hour:', businessHour);
    setEditingBusinessHour({ ...businessHour });
    setIsBusinessHourModalOpen(true);
  };

  const handleSaveBusinessHour = async () => {
    if (!editingBusinessHour) return;

    try {
      await updateBusinessHour(editingBusinessHour.id, {
        dayOfWeek: editingBusinessHour.dayOfWeek,
        openTime: editingBusinessHour.openTime,
        closeTime: editingBusinessHour.closeTime,
        breakStartTime: editingBusinessHour.breakStartTime,
        breakEndTime: editingBusinessHour.breakEndTime,
        closed: editingBusinessHour.closed,
      });

      toast.success('영업시간 정보 변경 성공', {
        description: '영업시간 정보를 변경하였습니다',
      });
      // 성공 시 영업시간 목록 새로고침
      const businessHoursResponse = await getBusinessHoursByHospital(hospitalId);
      setBusinessHours(businessHoursResponse.body || []);

      setIsBusinessHourModalOpen(false);
      setEditingBusinessHour(null);
    } catch (error) {
      console.error('Failed to update business hour:', error);

      toast.error('영업 시간 정보 변경 실패', {
        description: '서버 오류입니다',
      });
    }
  };

  // 30분 단위 시간 옵션 생성
  const generateTimeOptions = () => {
    const times = [];
    for (let hour = 0; hour < 24; hour++) {
      for (const minute of [0, 30]) {
        const timeString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
        const displayString = `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
        times.push({ value: timeString, label: displayString });
      }
    }
    return times;
  };

  const timeOptions = generateTimeOptions();

  // 개점시간 옵션 생성 (07:00 ~ 22:00)
  const getOpenTimeOptions = () => {
    return timeOptions.filter((time) => {
      const [hour] = time.value.split(':').map(Number);
      return hour >= 7 && hour <= 22;
    });
  };

  // 시간 포맷팅 함수 (hh:mm:ss -> hh:mm)
  const formatTimeDisplay = (timeString: string) => {
    if (!timeString) return '';
    return timeString.substring(0, 5); // hh:mm:ss에서 hh:mm만 추출
  };

  // 마감시간 옵션 생성 (개점시간 이후만)
  const getCloseTimeOptions = (openTime: string) => {
    if (!openTime) return timeOptions;

    const [openHour, openMinute] = openTime.split(':').map(Number);
    const openTimeInMinutes = openHour * 60 + openMinute;

    return timeOptions.filter((time) => {
      const [hour, minute] = time.value.split(':').map(Number);
      const timeInMinutes = hour * 60 + minute;
      return timeInMinutes > openTimeInMinutes;
    });
  };

  // 휴게시간 시작 옵션 생성 (영업시간 범위 내에서만)
  const getBreakStartTimeOptions = (openTime: string, closeTime: string) => {
    if (!openTime || !closeTime) return [];

    const [openHour, openMinute] = openTime.split(':').map(Number);
    const [closeHour, closeMinute] = closeTime.split(':').map(Number);
    const openTimeInMinutes = openHour * 60 + openMinute;
    const closeTimeInMinutes = closeHour * 60 + closeMinute;

    return timeOptions.filter((time) => {
      const [hour, minute] = time.value.split(':').map(Number);
      const timeInMinutes = hour * 60 + minute;
      return timeInMinutes > openTimeInMinutes && timeInMinutes < closeTimeInMinutes;
    });
  };

  // 휴게시간 종료 옵션 생성 (휴게시간 시작 이후, 마감시간 이전)
  const getBreakEndTimeOptions = (openTime: string, closeTime: string, breakStartTime: string) => {
    if (!openTime || !closeTime || !breakStartTime) return [];

    const [startHour, startMinute] = breakStartTime.split(':').map(Number);
    const [closeHour, closeMinute] = closeTime.split(':').map(Number);
    const startTimeInMinutes = startHour * 60 + startMinute;
    const closeTimeInMinutes = closeHour * 60 + closeMinute;

    return timeOptions.filter((time) => {
      const [hour, minute] = time.value.split(':').map(Number);
      const timeInMinutes = hour * 60 + minute;
      return timeInMinutes > startTimeInMinutes && timeInMinutes < closeTimeInMinutes;
    });
  };

  return (
    <>
      <Toaster expand={true} richColors position="top-center" />

      <Card>
        <CardHeader>
          <CardTitle>영업시간 관리</CardTitle>
          <CardDescription>요일별 영업시간과 점심시간을 설정하세요.</CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>요일</TableHead>
                <TableHead>영업시간</TableHead>
                <TableHead>휴게시간</TableHead>
                <TableHead className="text-right">작업</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {businessHours
                .sort((a, b) => {
                  const days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
                  return days.indexOf(a.dayOfWeek) - days.indexOf(b.dayOfWeek);
                })
                .map((hour) => (
                  <TableRow key={hour.id}>
                    <TableCell>{hour.dayOfWeek}</TableCell>
                    {hour.closed ? (
                      <TableCell colSpan={2} className="text-center text-muted-foreground">
                        휴무일
                      </TableCell>
                    ) : (
                      <>
                        <TableCell>
                          {formatTimeDisplay(hour.openTime)} - {formatTimeDisplay(hour.closeTime)}
                        </TableCell>
                        <TableCell>
                          {formatTimeDisplay(hour.breakStartTime)} - {formatTimeDisplay(hour.breakEndTime)}
                        </TableCell>
                      </>
                    )}
                    <TableCell className="text-right space-x-2">
                      <Button variant="outline" size="sm" onClick={() => handleEditBusinessHour(hour)}>
                        수정
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
      {/* 영업시간 수정 모달 */}
      <Dialog open={isBusinessHourModalOpen} onOpenChange={setIsBusinessHourModalOpen}>
        <DialogContent className="sm:max-w-[600px]">
          <DialogHeader>
            <DialogTitle>영업시간 수정 - {editingBusinessHour?.dayOfWeek}</DialogTitle>
          </DialogHeader>

          {editingBusinessHour && (
            <div className="grid gap-6 py-4">
              <div className="flex items-center space-x-2">
                <Checkbox
                  id="closed"
                  checked={editingBusinessHour.closed}
                  onCheckedChange={(checked) =>
                    setEditingBusinessHour((prev) => (prev ? { ...prev, closed: checked === true } : prev))
                  }
                />
                <Label htmlFor="closed">휴무일</Label>
              </div>

              {!editingBusinessHour.closed && (
                <div className="space-y-4">
                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="openTime">개점 시간</Label>
                      <Select
                        value={editingBusinessHour.openTime}
                        onValueChange={(value) =>
                          setEditingBusinessHour((prev) => (prev ? { ...prev, openTime: value } : prev))
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="개점 시간 선택">
                            {formatTimeDisplay(editingBusinessHour.openTime) || '개점 시간 선택'}
                          </SelectValue>
                        </SelectTrigger>
                        <SelectContent>
                          {getOpenTimeOptions().map((time) => (
                            <SelectItem key={time.value} value={time.value}>
                              {time.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="closeTime">마감 시간</Label>
                      <Select
                        value={editingBusinessHour.closeTime}
                        onValueChange={(value) =>
                          setEditingBusinessHour((prev) => (prev ? { ...prev, closeTime: value } : prev))
                        }
                      >
                        <SelectTrigger>
                          <SelectValue placeholder="마감 시간 선택">
                            {formatTimeDisplay(editingBusinessHour.closeTime) || '마감 시간 선택'}
                          </SelectValue>
                        </SelectTrigger>
                        <SelectContent>
                          {getCloseTimeOptions(editingBusinessHour.openTime).map((time) => (
                            <SelectItem key={time.value} value={time.value}>
                              {time.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div className="space-y-2">
                      <Label htmlFor="breakStartTime">휴게시간 시작</Label>
                      <Select
                        value={editingBusinessHour.breakStartTime}
                        onValueChange={(value) =>
                          setEditingBusinessHour((prev) =>
                            prev ? { ...prev, breakStartTime: value, breakEndTime: '' } : prev,
                          )
                        }
                        disabled={!editingBusinessHour.openTime || !editingBusinessHour.closeTime}
                      >
                        <SelectTrigger>
                          <SelectValue
                            placeholder={
                              !editingBusinessHour.openTime || !editingBusinessHour.closeTime
                                ? '먼저 영업시간을 선택하세요'
                                : '휴게시간 시작 선택'
                            }
                          >
                            {formatTimeDisplay(editingBusinessHour.breakStartTime) ||
                              (!editingBusinessHour.openTime || !editingBusinessHour.closeTime
                                ? '먼저 영업시간을 선택하세요'
                                : '휴게시간 시작 선택')}
                          </SelectValue>
                        </SelectTrigger>
                        <SelectContent>
                          {getBreakStartTimeOptions(editingBusinessHour.openTime, editingBusinessHour.closeTime).map(
                            (time) => (
                              <SelectItem key={time.value} value={time.value}>
                                {time.label}
                              </SelectItem>
                            ),
                          )}
                        </SelectContent>
                      </Select>
                    </div>
                    <div className="space-y-2">
                      <Label htmlFor="breakEndTime">휴게시간 종료</Label>
                      <Select
                        value={editingBusinessHour.breakEndTime}
                        onValueChange={(value) =>
                          setEditingBusinessHour((prev) => (prev ? { ...prev, breakEndTime: value } : prev))
                        }
                        disabled={
                          !editingBusinessHour.openTime ||
                          !editingBusinessHour.closeTime ||
                          !editingBusinessHour.breakStartTime
                        }
                      >
                        <SelectTrigger>
                          <SelectValue
                            placeholder={
                              !editingBusinessHour.openTime || !editingBusinessHour.closeTime
                                ? '먼저 영업시간을 선택하세요'
                                : !editingBusinessHour.breakStartTime
                                  ? '먼저 휴게시간 시작을 선택하세요'
                                  : '휴게시간 종료 선택'
                            }
                          >
                            {formatTimeDisplay(editingBusinessHour.breakEndTime) ||
                              (!editingBusinessHour.openTime || !editingBusinessHour.closeTime
                                ? '먼저 영업시간을 선택하세요'
                                : !editingBusinessHour.breakStartTime
                                  ? '먼저 휴게시간 시작을 선택하세요'
                                  : '휴게시간 종료 선택')}
                          </SelectValue>
                        </SelectTrigger>
                        <SelectContent>
                          {getBreakEndTimeOptions(
                            editingBusinessHour.openTime,
                            editingBusinessHour.closeTime,
                            editingBusinessHour.breakStartTime,
                          ).map((time) => (
                            <SelectItem key={time.value} value={time.value}>
                              {time.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          <DialogFooter>
            <DialogClose asChild>
              <Button variant="outline">취소</Button>
            </DialogClose>
            <Button onClick={handleSaveBusinessHour}>저장</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
