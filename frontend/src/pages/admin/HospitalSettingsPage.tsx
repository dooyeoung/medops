import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getCurrentAdmin } from '@/api/admin';

import DoctorManagement from '@/components/admin/DoctorManagement';
import TreatmentProductManagement from '@/components/admin/TreatmentProductManagement';
import BusinessHourManagement from '@/components/admin/BusinessHourManagement';
import AdminManagement from '@/components/admin/AdminManagement';
import { Toaster } from 'sonner';

export default function HospitalSettingsPage() {
  const [hospitalId, setHospitalId] = useState<string | null>(null);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchHospitalSettings = async () => {
      try {
        const adminResponse = await getCurrentAdmin();
        setHospitalId(adminResponse.body.hospital.id);
      } catch (err: any) {
        console.log(err);
        navigate('/admin/login');
      }
    };

    fetchHospitalSettings();
  }, [navigate]);

  return (
    <div className="w-full p-4 space-y-4">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">병원 관리</h1>
          <p className="text-muted-foreground">병원 영업 시간 및 상품을 관리하세요</p>
        </div>
      </div>

      <Toaster expand={true} richColors position="top-center" />

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
