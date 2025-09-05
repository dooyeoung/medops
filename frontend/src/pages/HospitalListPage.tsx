import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Link } from 'react-router-dom';
import { getHospitals } from '@/api/hospital';
import { useAuth } from '@/context/AuthContext';

interface Hospital {
  id: string;
  name: string;
  address: string;
  createdAt: string;
  treatmentProducts: TreatmentProduct[];
}

interface TreatmentProduct {
  id: string;
  name: string;
  description: string;
  maxCapacity: number;
  price: number;
}

export default function HospitalListPage() {
  const [hospitals, setHospitals] = useState<Hospital[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const {user, isLoading: authLoading } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchHospitals = async () => {
      if (!user) {
        setError('Authentication required to fetch hospital data.');
        navigate('/login')
        setIsLoading(false);
        return;
      }
      try {
        const [response] = await Promise.all([getHospitals(), new Promise((resolve) => setTimeout(resolve, 300))]);

        if (response.result.resultCode === 200) {
          setHospitals(response.body);
        } else {
          setError(response.result.resultMessage || 'Failed to fetch hospitals.');
        }
      } catch (err) {
        setError('An error occurred while fetching hospitals. ' + err);
        console.error(err);
      } finally {
        setIsLoading(false);
      }
    };

    if (!authLoading) {
      fetchHospitals();
    }
  }, [user, authLoading]);

  if (isLoading) {
    return (
      <div className="w-full mx-auto mt-4">
        <div className="animate-pulse">
          <div className="h-16 bg-gray-300 rounded w-full mx-auto"></div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"></div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-background">
        <div className="container mx-auto py-16 text-center">
          <div className="text-red-600 text-xl mb-4">오류가 발생했습니다</div>
          <p className="text-gray-600 mb-6">{error}</p>
          <Button onClick={() => window.location.reload()} variant="outline">
            다시 시도
          </Button>
          <Button asChild variant="outline">
            <Link to="/">첫 화면으로</Link>
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full mx-auto mt-4">
      <div className="">
        {hospitals.length === 0 ? (
          <div className="col-span-full text-center py-16">
            <div className="text-2xl">🏥</div>
            <h3 className="text-xl font-semibold text-gray-700 mb-2">등록된 병원이 없습니다</h3>
            <p className="text-gray-500 mb-2">새로운 병원이 등록될 때까지 기다려주세요</p>
          </div>
        ) : (
          hospitals.map((hospital) => {
            return (
              <Link to={`/hospitals/${hospital.id}`} key={hospital.id} className="group">
                <Card className="mb-4 hover:shadow-md transition-all duration-300 group-hover:scale-102">
                  <CardContent>
                    <div className="flex justify-between">
                      <div className="font-bold text-gray-900 transition-colors">🏥 {hospital.name}</div>
                      <div className="text-gray-600 ">📍 {hospital.address}</div>
                    </div>
                    <div className="flex space-x-1">
                      {hospital.treatmentProducts.map((product) => {
                        return <Badge>{product.name}</Badge>;
                      })}
                    </div>
                  </CardContent>
                </Card>
              </Link>
            );
          })
        )}
      </div>
    </div>
  );
}
