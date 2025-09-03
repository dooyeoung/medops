import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/button';

export default function Layout() {
  const { user, admin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="">
      <header className="p-4 border-b">
        <nav className="flex items-center justify-between">
          <Link to="/" className="text-xl font-bold">
            MedOps
          </Link>
          <div className="flex items-center space-x-4">
            {user || admin ? (
              <>
                <Link to="/hospitals" className="hover:text-blue-600">
                  병원찾기
                </Link>
                {user && (
                  <Link to="/my-page" className="hover:text-blue-600">
                    마이페이지
                  </Link>
                )}
                {admin && (
                  <Link to="/admin/schedule" className="hover:text-blue-600">
                    관리자 페이지
                  </Link>
                )}
                <Button onClick={handleLogout} variant="ghost">
                  로그아웃
                </Button>
              </>
            ) : (
              <>
                <Link to="/login" className="hover:text-blue-600">
                  로그인
                </Link>
                <Link to="/signup" className="hover:text-blue-600">
                  회원가입
                </Link>
              </>
            )}
          </div>
        </nav>
      </header>
      <main className="w-2xl mx-auto flex items-center justify-center">
        <Outlet />
      </main>
    </div>
  );
}
