import { Outlet, useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import AdminSidebar from './AdminSidebar';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';
import { Settings } from 'lucide-react';
import { getCurrentAdmin } from '@/api/admin';

export default function AdminLayout() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [hospitalName, setHospitalName] = useState<string>('');

  useEffect(() => {
    const fetchHospitalInfo = async () => {
      try {
        const adminResponse = await getCurrentAdmin();
        if (adminResponse.body.hospital) {
          setHospitalName(adminResponse.body.hospital.name);
        }
      } catch (error) {
        console.error('Failed to fetch hospital info:', error);
      }
    };

    fetchHospitalInfo();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/admin/login');
  };

  const handleAccountSettings = () => {
    navigate('/admin/account-settings');
  };

  return (
    <div className="flex min-h-screen w-full">
      <AdminSidebar />
      <main className="flex-1 flex flex-col">
        <header className="w-full p-4 border-b flex justify-between items-center">
          <div className="flex items-center">
            <h1 className="text-xl font-semibold text-gray-800">{hospitalName}</h1>
          </div>
          <div className="flex items-center gap-2">
            <Button onClick={handleAccountSettings} variant="ghost" size="sm">
              <Settings className="h-4 w-4 mr-2" />
              계정 설정
            </Button>
            <Button onClick={handleLogout} variant="ghost">
              로그아웃
            </Button>
          </div>
        </header>
        <div className="">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
