import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';
import { useAuth } from '@/context/AuthContext';
import { useState, useEffect } from 'react';
import { getReservationsByUserId } from '@/api/reservation';

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

  const formatTime = (isoString: string) =>
    new Date(isoString).toLocaleTimeString('ko-KR', {
      year: '2-digit',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });

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

      {eventsLoading && <div className="text-center">Loading events...</div>}
      {eventsError && <div className="text-red-500 text-center">Error: {eventsError}</div>}

      <Card className="mt-4">
        <CardHeader>
          <CardTitle className="text-2xl">예약 내역</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {!eventsLoading && userEvents.length === 0 && !eventsError && (
            <div className="text-center text-gray-500">예약 내역이 없습니다.</div>
          )}
          {!eventsLoading && userEvents.length > 0 && (
            <>
              {userEvents.map((event) => (
                <Card className="border rounded-lg text-sm">
                  <CardContent>
                    <div className="flex justify-between">
                      <div>
                        {event.hospitalName} ({event.treatmentProductName}){' '}
                      </div>
                      <div>
                        {formatTime(event.startTime)}~{formatTime(event.endTime)}
                      </div>
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
