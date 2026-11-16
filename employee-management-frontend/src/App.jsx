import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';

// Layouts
import AdminLayout from './components/layout/AdminLayout';
import EmployeeLayout from './components/layout/EmployeeLayout';

// Auth Pages
import AdminLogin from './pages/auth/AdminLogin';
import EmployeeLogin from './pages/auth/EmployeeLogin';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import EmployeeManagement from './pages/admin/EmployeeManagement';
import BiometricManagement from './pages/admin/BiometricManagement';
import AttendanceManagement from './pages/admin/AttendanceManagement';
import MonthlyRecords from './pages/admin/MonthlyRecords';
import BreakManagement from './pages/admin/BreakManagement';
import PayslipManagement from './pages/admin/PayslipManagement';
import HolidayManagement from './pages/admin/HolidayManagement';
import CompanySettings from './pages/admin/CompanySettings';
import MobileBiometricSettings from './pages/admin/MobileBiometricSettings';

// Employee Pages
import EmployeeDashboard from './pages/employee/EmployeeDashboard';
import MyProfile from './pages/employee/MyProfile';
import MyAttendance from './pages/employee/MyAttendance';
import MyBreaks from './pages/employee/MyBreaks';
import MyPayslips from './pages/employee/MyPayslips';
import EmployeeHolidays from './pages/employee/EmployeeHolidays';

// Index Redirect Component based on auth status & role
function RootRedirect() {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-950 text-white">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full animate-spin" />
          <span className="text-sm font-medium text-slate-400">Loading system...</span>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/admin/login" replace />;
  }

  return isAdmin ? <Navigate to="/admin/dashboard" replace /> : <Navigate to="/employee/dashboard" replace />;
}

export default function App() {
  return (
    <Routes>
      {/* Root redirect */}
      <Route path="/" element={<RootRedirect />} />

      {/* Authentication Routes */}
      <Route path="/admin/login" element={<AdminLogin />} />
      <Route path="/employee/login" element={<EmployeeLogin />} />

      {/* Admin Panel Protected Routes */}
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboard />} />
        <Route path="employees" element={<EmployeeManagement />} />
        <Route path="biometrics" element={<BiometricManagement />} />
        <Route path="mobile-biometrics" element={<MobileBiometricSettings />} />
        <Route path="attendance" element={<AttendanceManagement />} />
        <Route path="monthly-records" element={<MonthlyRecords />} />
        <Route path="breaks" element={<BreakManagement />} />
        <Route path="payslips" element={<PayslipManagement />} />
        <Route path="holidays" element={<HolidayManagement />} />
        <Route path="settings" element={<CompanySettings />} />
      </Route>

      {/* Employee Panel Protected Routes */}
      <Route path="/employee" element={<EmployeeLayout />}>
        <Route index element={<Navigate to="/employee/dashboard" replace />} />
        <Route path="dashboard" element={<EmployeeDashboard />} />
        <Route path="profile" element={<MyProfile />} />
        <Route path="attendance" element={<MyAttendance />} />
        <Route path="breaks" element={<Navigate to="/employee/dashboard" replace />} />
        <Route path="payslips" element={<MyPayslips />} />
        <Route path="holidays" element={<EmployeeHolidays />} />
      </Route>

      {/* Catch-all Fallback */}
      <Route path="*" element={<RootRedirect />} />
    </Routes>
  );
}
