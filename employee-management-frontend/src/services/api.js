import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000,
});

// Request interceptor to attach JWT Token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('company_auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Clear token if expired or unauthorized
      if (!window.location.pathname.includes('/login')) {
        localStorage.removeItem('company_auth_token');
        localStorage.removeItem('company_auth_user');
      }
    }
    const message = error.response?.data?.message || error.message || 'An unexpected error occurred';
    return Promise.reject(new Error(message));
  }
);

// 1. Auth APIs
export const downloadAuthenticatedFile = async (url, filename) => {
  const token = localStorage.getItem('company_auth_token');
  const response = await fetch(url, {
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
    },
  });
  if (!response.ok) {
    throw new Error('Failed to download file. Status: ' + response.statusText);
  }
  const blob = await response.blob();
  const blobUrl = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = blobUrl;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  setTimeout(() => window.URL.revokeObjectURL(blobUrl), 1000);
};

export const authApi = {
  login: (data) => api.post('/auth/login', data),
  sendOtp: (target, type) => api.post('/auth/send-otp', { target, type }),
  verifyOtp: (target, type, code) => api.post('/auth/verify-otp', { target, type, code }),
  loginWithOtp: (target, type, code) => api.post('/auth/login-with-otp', { target, type, code }),
  getCurrentUser: () => api.get('/auth/me'),
};

// 2. Admin APIs
export const adminApi = {
  getDashboardStats: () => api.get('/admin/dashboard/stats'),
  
  // Employee Management
  getEmployees: (params) => api.get('/admin/employees', { params }),
  getEmployee: (id) => api.get(`/admin/employees/${id}`),
  createEmployee: (data) => api.post('/admin/employees', data),
  updateEmployee: (id, data) => api.put(`/admin/employees/${id}`, data),
  deleteEmployee: (id) => api.delete(`/admin/employees/${id}`),
  toggleStatus: (id) => api.patch(`/admin/employees/${id}/toggle-status`),
  getDepartments: () => api.get('/admin/employees/departments'),
  getDesignations: (departmentId) => api.get('/admin/employees/designations', { params: { departmentId } }),
  exportEmployeesCsvUrl: () => `${API_BASE_URL}/admin/employees/export-csv`,

  // Biometrics
  getBiometricDevices: () => api.get('/admin/biometrics/devices'),
  createBiometricDevice: (data) => api.post('/admin/biometrics/devices', data),
  updateBiometricDevice: (id, data) => api.put(`/admin/biometrics/devices/${id}`, data),
  deleteBiometricDevice: (id) => api.delete(`/admin/biometrics/devices/${id}`),
  testBiometricDeviceConnection: (id) => api.post(`/admin/biometrics/devices/${id}/test-connection`),
  pingDevice: (id) => api.post(`/admin/biometrics/devices/${id}/ping`),
  enrollBiometric: (data) => api.post('/admin/biometrics/enroll', data),
  getBiometricStatus: (employeeId) => api.get(`/admin/biometrics/status/${employeeId}`),

  // Attendance
  getAttendance: (params) => api.get('/admin/attendance', { params }),
  getDailyPunches: (employeeId, date) => api.get(`/admin/attendance/events/${employeeId}`, { params: { date } }),
  exportAttendanceCsvUrl: (date, departmentId, status, search) =>
    `${API_BASE_URL}/admin/attendance/export-csv?${date ? 'date=' + date : ''}${departmentId ? '&departmentId=' + departmentId : ''}${status ? '&status=' + status : ''}${search ? '&search=' + encodeURIComponent(search) : ''}`,
  exportEmployeeAttendanceCsvUrl: (employeeId, month, year) =>
    `${API_BASE_URL}/admin/attendance/export-employee-csv/${employeeId}?${month ? 'month=' + month : ''}${year ? '&year=' + year : ''}`,
  punchAttendance: (data) => api.post('/admin/attendance/punch', data),
  correctAttendance: (data) => api.post('/admin/attendance/correction', data),
  markAbsent: (date) => api.post('/admin/attendance/mark-absent', null, { params: { date } }),
  getMonthlyMatrix: (month, year) => api.get('/admin/attendance/monthly-matrix', { params: { month, year } }),
  exportMonthlyCsvUrl: (month, year) => `${API_BASE_URL}/admin/attendance/export-monthly-csv?month=${month}&year=${year}`,

  // Breaks
  getBreaks: (params) => api.get('/admin/breaks', { params }),
  exportBreaksCsvUrl: (date, employeeId) => `${API_BASE_URL}/admin/breaks/export-csv?${date ? 'date=' + date : ''}${employeeId ? '&employeeId=' + employeeId : ''}`,

  // Payslips
  getPayslips: (params) => api.get('/admin/payslips', { params }),
  getSalaryCalculations: (month, year) => api.get('/admin/payslips/salary-calculations', { params: { month, year } }),
  exportPayslipsCsvUrl: (month, year) => `${API_BASE_URL}/admin/payslips/export-csv?${month ? 'month=' + month : ''}${year ? '&year=' + year : ''}`,
  generatePayslip: (data) => api.post('/admin/payslips/generate', data),
  getPayslip: (id) => api.get(`/admin/payslips/${id}`),

  // Holidays
  getHolidays: (year) => api.get('/admin/holidays', { params: { year } }),
  addHoliday: (data) => api.post('/admin/holidays', data),
  updateHoliday: (id, data) => api.put(`/admin/holidays/${id}`, data),
  deleteHoliday: (id) => api.delete(`/admin/holidays/${id}`),
  exportHolidaysCsvUrl: (year) => `${API_BASE_URL}/admin/holidays/export-csv?year=${year}`,

  // Settings
  getSettings: () => api.get('/admin/settings'),
  updateSettings: (data) => api.put('/admin/settings', data),
};

// 2.5 Mobile Biometric Settings APIs
export const mobileBiometricApi = {
  getSettings: () => api.get('/admin/mobile-biometrics/settings'),
  updateSettings: (data) => api.put('/admin/mobile-biometrics/settings', data),
  resetSettings: () => api.post('/admin/mobile-biometrics/settings/reset'),
  getDashboardSummary: () => api.get('/admin/mobile-biometrics/dashboard-summary'),
  getDevices: () => api.get('/admin/mobile-biometrics/devices'),
  registerDevice: (data) => api.post('/admin/mobile-biometrics/devices', data),
  updateDeviceStatus: (id, status) => api.put(`/admin/mobile-biometrics/devices/${id}/status?status=${status}`),
  revokeDevice: (id) => api.delete(`/admin/mobile-biometrics/devices/${id}`),
  testDeviceConnection: (id) => api.post(`/admin/mobile-biometrics/devices/${id}/test-connection`),
  getDeviceLogs: (id) => api.get(`/admin/mobile-biometrics/devices/${id}/logs`),
  getEmployeeRegistrations: () => api.get('/admin/mobile-biometrics/employee-registrations'),
  resetEmployeeBiometrics: (id) => api.post(`/admin/mobile-biometrics/employees/${id}/reset-biometrics`),
  getVerificationLogs: (type) => api.get('/admin/mobile-biometrics/verification-logs', { params: { type } }),
  getAllDeviceLogs: () => api.get('/admin/mobile-biometrics/device-logs'),
  generateEmployeeQr: (employeeId) => api.post(`/admin/mobile-biometrics/generate-qr/${employeeId}`),
  syncNow: () => api.post('/admin/mobile-biometrics/sync-now'),
  getSystemHealth: () => api.get('/admin/mobile-biometrics/system-health'),
  getActivePolicy: () => api.get('/public/mobile-biometrics/active-policy'),
};

// 3. Employee APIs
export const employeeApi = {
  getDashboardStats: () => api.get('/employee/dashboard/stats'),
  getProfile: () => api.get('/employee/profile'),
  updateProfile: (data) => api.put('/employee/profile', data),
  
  getTodayAttendance: () => api.get('/employee/attendance/today'),
  getAttendanceHistory: () => api.get('/employee/attendance/history'),
  getMyDailyPunches: (date) => api.get('/employee/attendance/events', { params: { date } }),
  exportMyAttendanceCsvUrl: (month, year) => `${API_BASE_URL}/employee/attendance/export-my-csv?${month ? 'month=' + month : ''}${year ? '&year=' + year : ''}`,
  punchAttendance: (data) => api.post('/employee/attendance/punch', data),

  breakAction: (data) => api.post('/employee/breaks/action', data),
  getMyBreaks: () => api.get('/employee/breaks/my-breaks'),

  getMyPayslips: () => api.get('/employee/payslips'),
  getMyPayslip: (id) => api.get(`/employee/payslips/${id}`),

  getHolidays: (year) => api.get('/employee/holidays', { params: { year } }),
  getBiometricStatus: () => api.get('/employee/biometrics/status'),
};

export default api;
