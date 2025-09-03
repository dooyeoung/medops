import { useAuth } from '@/context/AuthContext';
import { Navigate, Outlet } from 'react-router-dom';

interface ProtectedRouteProps {
  allowedRoles?: ('user' | 'admin')[];
}

export default function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { user, admin, isLoading, role } = useAuth();

  if (isLoading) {
    return <div className="text-center py-8">Loading authentication...</div>;
  }

  if (!user && !admin) {
    // Not authenticated, redirect to login
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && role && !allowedRoles.includes(role)) {
    // Authenticated but role not allowed, redirect to home or unauthorized page
    return <Navigate to="/" replace />;
  }

  return <Outlet />;
}
