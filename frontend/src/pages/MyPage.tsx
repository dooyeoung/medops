import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { useAuth } from '@/context/AuthContext';
import { useState, useEffect } from 'react';
import { getReservationsByUserId } from '@/api/reservation';
import { formatDistanceToNow, isToday, isPast, isFuture, differenceInHours, format } from 'date-fns';
import { ko } from 'date-fns/locale';

interface UserEvent {
  id: string;
  recordId: string | null;
  hospitalId: string;
  userId: string;
  eventType: string;
  version: number;
  createdAt: string;

  payload: {
    reservationId?: string;
    userId: string;
    startTime: string;
    endTime: string;
    [key: string]: any;
  };
}

export default function MyPage() {
  const { user, isLoading: authLoading } = useAuth();
  const [userEvents, setUserEvents] = useState<UserEvent[]>([]);
  const [eventsLoading, setEventsLoading] = useState(true);
  const [eventsError, setEventsError] = useState<string | null>(null);
  const [newStatus, setNewStatus] = useState<string>('');

  const fetchUserEvents = async () => {
    setEventsLoading(true);
    setEventsError(null);
    try {
      if (!user || !user.id) return;
      const response = await getReservationsByUserId(user.id);
      setUserEvents(response.body || []);
    } catch (error: any) {
      setEventsError(error.response?.data?.result?.resultMessage || error.message || 'Failed to fetch user events.');
      console.error('Error fetching user events:', error);
    } finally {
      setEventsLoading(false);
    }
  };

  useEffect(() => {
    if (user && user.id) {
      fetchUserEvents();
    }
  }, [user]);

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
        return `${timeRange} (${hoursUntil}시간 후, ${dateDisplay})`;
      } else if (hoursUntil === 0) {
        return `${timeRange} (곧 시작, ${dateDisplay})`;
      } else {
        return `${timeRange} (완료, ${dateDisplay})`;
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

  if (authLoading) {
    return <div className="container mx-auto py-8 text-center">Loading user data...</div>;
  }

  if (!user) {
    return <div className="container mx-auto py-8 text-center">User data not found or not logged in.</div>;
  }

  return (
    <div className="w-full mt-4 mx-auto">
      <Card>
        <CardHeader>
          <CardTitle className="text-2xl">내 정보</CardTitle>
        </CardHeader>
        <CardContent className="flex justify-between">
          <div className="text-sm text-gray-500">{user.name}</div>
          <div className="text-sm text-gray-500">{user.email}</div>
        </CardContent>
      </Card>

      {eventsError && <div className="text-red-500 text-center">Error: {eventsError}</div>}

      <Card className="mt-4">
        <CardHeader>
          <CardTitle className="text-2xl">예약 내역</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {!eventsLoading && userEvents.length === 0 && !eventsError && (
            <div className="text-center text-gray-500">예약 내역이 없습니다.</div>
          )}
          {eventsLoading ? (
            <div className="flex items-center justify-center py-8">
              <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
              <span className="ml-2 text-sm text-gray-500">정보를 불러오는 중...</span>
            </div>
          ) : (
            <>
              {userEvents.map((event) => (
                <Card className="border rounded-lg text-sm py-2">
                  <CardContent>
                    <div className="flex justify-between">
                      <div>
                        {event.hospitalName} ({event.treatmentProductName}){' '}
                      </div>
                      <div>{formatReservationTime(event.startTime, event.endTime)}</div>
                    </div>
                    <div>요청사항: {event.userMemo}</div>
                  </CardContent>
                </Card>
              ))}
            </>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
