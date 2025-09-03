import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getHospitalById } from '@/api/hospital';
import { getCurrentAdmin } from '@/api/admin';

import DoctorManagement from '@/components/admin/DoctorManagement';
import TreatmentProductManagement from '@/components/admin/TreatmentProductManagement';
import BusinessHourManagement from '@/components/admin/BusinessHourManagement';
import AdminManagement from '@/components/admin/AdminManagement';

interface Hospital {
  id: string;
  name: string;
  address: string;
  createdAt: string;
}

export default function HospitalSettingsPage() {
  const [hospitalId, setHospitalId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchHospitalSettings = async () => {
      setIsLoading(true);
      try {
        const adminResponse = await getCurrentAdmin();
        setHospitalId(adminResponse.body.hospital.id);
      } catch (err: any) {
        navigate('/admin/login');
      } finally {
        setIsLoading(false);
      }
    };

    fetchHospitalSettings();
  }, []);

  if (isLoading) {
    return <div className="container mx-auto py-8 text-center">Loading hospital settings...</div>;
  }

  return (
    <div className="w-full p-4 space-y-4">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">병원 관리</h1>
          <p className="text-muted-foreground">병원 영업 시간 및 상품을 관리하세요</p>
        </div>
      </div>
      {/* 영업시간 관리 */}
      <BusinessHourManagement hospitalId={hospitalId} />

      {/* 관리자 관리 */}
      <AdminManagement hospitalId={hospitalId} />

      {/* 의사 관리 */}
      <DoctorManagement hospitalId={hospitalId} />

      {/* 상품 관리 */}
      <TreatmentProductManagement hospitalId={hospitalId} />
    </div>
  );
}
