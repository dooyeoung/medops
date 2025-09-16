import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/layout/Layout';

import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import HospitalListPage from './pages/HospitalListPage';
import HospitalDetailPage from './pages/HospitalDetailPage';
import MyPage from './pages/MyPage';

// Admin Imports
import AdminLoginPage from './pages/admin/AdminLoginPage';
import AdminRegisterPage from './pages/admin/AdminRegisterPage';
import AdminVerifyInvitationPage from './pages/admin/AdminVerifyInvitationPage';
import AdminSetupAccountPage from './pages/admin/AdminSetupAccountPage';
import AdminLayout from './components/admin/AdminLayout';
import HospitalSettingsPage from './pages/admin/HospitalSettingsPage';
import AccountSettingsPage from './pages/admin/AccountSettingsPage';
import SchedulePage from './pages/admin/SchedulePage';
import DashboardPage from './pages/admin/DashboardPage';

// Auth Imports
import ProtectedRoute from './components/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes with the main layout */}
        <Route path="/" element={<Layout />}>
          <Route index element={<Navigate to="/login" replace />} />
          <Route path="login" element={<LoginPage />} />
          <Route path="signup" element={<SignupPage />} />
          <Route path="admin/login" element={<AdminLoginPage />} />
          <Route path="admin/register" element={<AdminRegisterPage />} />
          <Route path="admin/verify-invitation" element={<AdminVerifyInvitationPage />} />
          <Route path="admin/setup-account" element={<AdminSetupAccountPage />} />
          <Route path="hospitals" element={<HospitalListPage />} />

          {/* Protected user routes */}
          <Route element={<ProtectedRoute allowedRoles={['user']} />}>
            <Route path="hospitals/:id" element={<HospitalDetailPage />} />
            <Route path="my-page" element={<MyPage />} />
          </Route>
        </Route>

        {/* Admin routes */}
        <Route element={<ProtectedRoute allowedRoles={['admin']} />}>
          <Route path="/admin" element={<AdminLayout />}>
            <Route index element={<DashboardPage />} />
            <Route path="dashboard" element={<DashboardPage />} />
            <Route path="schedule" element={<SchedulePage />} />
            <Route path="hospital-settings" element={<HospitalSettingsPage />} />
            <Route path="account-settings" element={<AccountSettingsPage />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
