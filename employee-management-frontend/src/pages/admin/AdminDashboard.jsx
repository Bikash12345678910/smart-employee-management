import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '../../services/api';
import StatsCard from '../../components/common/StatsCard';
import { StatusBadge, MethodBadge } from '../../components/common/Badge';
import {
  Users,
  UserCheck,
  UserX,
  Clock,
  CalendarOff,
  Percent,
  DollarSign,
  TrendingUp,
  Cpu,
  RefreshCw,
  ArrowRight,
  ShieldCheck,
  CheckCircle2
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
  Legend
} from 'recharts';
import { motion } from 'framer-motion';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();

  const fetchStats = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getDashboardStats();
      if (res.success) {
        setStats(res.data);
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  if (loading && !stats) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[60vh] space-y-3">
        <div className="w-10 h-10 border-4 border-primary-500/20 border-t-primary-600 rounded-full animate-spin" />
        <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">Loading Dashboard Metrics...</p>
      </div>
    );
  }

  const PIE_COLORS = ['#6366f1', '#10b981', '#f59e0b', '#ec4899', '#8b5cf6'];

  return (
    <div className="space-y-6 md:space-y-8">
      {/* Top Welcome & Quick Refresh */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100">
            Company Attendance & Operations
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            Real-time biometric monitoring, shifts, and payroll overview.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={fetchStats}
            className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-750 transition-colors shadow-xs flex items-center gap-1.5"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} /> Refresh Feed
          </button>

          <button
            onClick={() => navigate('/admin/attendance')}
            className="px-4 py-2 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-500/20 transition-all flex items-center gap-1.5"
          >
            Live Turnstile Punch &rarr;
          </button>
        </div>
      </div>

      {/* 6 Main Animated Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <StatsCard
          title="Total Employees"
          value={stats?.totalEmployees || 0}
          icon={Users}
          color="primary"
          onViewList={() => navigate('/admin/employees')}
        />
        <StatsCard
          title="Present Today"
          value={stats?.presentToday || 0}
          icon={UserCheck}
          color="emerald"
          onViewList={() => navigate('/admin/attendance?status=PRESENT')}
        />
        <StatsCard
          title="Late Today"
          value={stats?.lateToday || 0}
          icon={Clock}
          color="amber"
          onViewList={() => navigate('/admin/attendance?status=LATE')}
        />
        <StatsCard
          title="Half Day Today"
          value={stats?.halfDayToday || 0}
          icon={Clock}
          color="purple"
          onViewList={() => navigate('/admin/attendance?status=HALF_DAY')}
        />
        <StatsCard
          title="On Leave Today"
          value={stats?.onLeaveToday || 0}
          icon={CalendarOff}
          color="blue"
          onViewList={() => navigate('/admin/attendance?status=ON_LEAVE')}
        />
        <StatsCard
          title="Absent Today"
          value={stats?.absentToday || 0}
          icon={UserX}
          color="rose"
          onViewList={() => navigate('/admin/attendance?status=ABSENT')}
        />
      </div>

      {/* Highlights Bar: Attendance Rate, Salary Payout, Devices */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Attendance Percentage */}
        <div className="glass-card p-5 flex items-center gap-4">
          <div className="p-3 rounded-2xl bg-indigo-50 dark:bg-indigo-950/60 text-indigo-600 dark:text-indigo-400">
            <Percent className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              Today's Attendance Rate
            </div>
            <div className="text-2xl font-bold font-mono text-slate-900 dark:text-slate-50">
              {stats?.attendancePercentage || 0}%
            </div>
            <p className="text-[11px] text-emerald-600 dark:text-emerald-400 font-medium">
              Calculated across active workforce
            </p>
          </div>
        </div>

        {/* Monthly Salary Overview (Connected to Payslips Actual Payable Salary) */}
        <div
          onClick={() => navigate('/admin/payslips')}
          className="glass-card p-5 flex items-center gap-4 cursor-pointer hover:border-emerald-400 dark:hover:border-emerald-600 transition-all"
        >
          <div className="p-3 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 text-emerald-600 dark:text-emerald-400">
            <DollarSign className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              Monthly Salary Overview
            </div>
            <div className="text-2xl font-bold font-mono text-slate-900 dark:text-slate-50">
              ₹{Number(stats?.monthlySalaryPayout || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </div>
            <p className="text-[11px] text-emerald-600 dark:text-emerald-400 font-medium">
              Connected to Payslips Actual Payable Salary &rarr;
            </p>
          </div>
        </div>

        {/* Biometric Gateways */}
        <div className="glass-card p-5 flex items-center gap-4">
          <div className="p-3 rounded-2xl bg-cyan-50 dark:bg-cyan-950/60 text-cyan-600 dark:text-cyan-400">
            <Cpu className="w-6 h-6" />
          </div>
          <div>
            <div className="text-xs font-semibold text-slate-500 dark:text-slate-400">
              Hardware Biometric Sensors
            </div>
            <div className="text-2xl font-bold font-mono text-slate-900 dark:text-slate-50">
              {stats?.onlineDevices || 0} Online
            </div>
            <p className="text-[11px] text-emerald-600 dark:text-emerald-400 font-medium flex items-center gap-1">
              <CheckCircle2 className="w-3 h-3" /> ZKTeco & R307 Gateways Active
            </p>
          </div>
        </div>
      </div>

      {/* Visual Charts: 7-Day Trend */}
      <div className="glass-card p-6">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-primary-500" /> Attendance Trend (Last 7 Days)
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Daily comparison of present, late arrivals, and absences
            </p>
          </div>
          <button
            onClick={() => navigate('/admin/monthly-records')}
            className="text-xs text-primary-600 dark:text-primary-400 font-semibold hover:underline"
          >
            Monthly Matrix &rarr;
          </button>
        </div>

        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={stats?.monthlyAttendanceTrend || []}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" opacity={0.5} />
              <XAxis dataKey="day" stroke="#94a3b8" fontSize={11} />
              <YAxis stroke="#94a3b8" fontSize={11} />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#1e293b',
                  borderRadius: '12px',
                  border: 'none',
                  color: '#f8fafc',
                  fontSize: '12px'
                }}
              />
              <Legend wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
              <Bar dataKey="present" fill="#10b981" name="Present" radius={[4, 4, 0, 0]} />
              <Bar dataKey="late" fill="#f59e0b" name="Late" radius={[4, 4, 0, 0]} />
              <Bar dataKey="absent" fill="#f43f5e" name="Absent" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Lower Tables: Recent Attendance Feed & Recent Registrations */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Attendance Activity Feed */}
        <div className="glass-card p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                Recent Attendance Punches
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Live sensor logs from Face and Fingerprint scanners
              </p>
            </div>
            <button
              onClick={() => navigate('/admin/attendance')}
              className="text-xs text-primary-600 dark:text-primary-400 font-semibold hover:underline"
            >
              All Punches &rarr;
            </button>
          </div>

          <div className="space-y-3">
            {stats?.recentAttendanceActivity?.length > 0 ? (
              stats.recentAttendanceActivity.map((item) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 text-xs"
                >
                  <div className="flex items-center gap-3">
                    <img
                      src={item.profilePhotoUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100"}
                      alt={item.employeeName}
                      className="w-8 h-8 rounded-full object-cover"
                    />
                    <div>
                      <div className="font-semibold text-slate-900 dark:text-slate-100">
                        {item.employeeName}
                      </div>
                      <div className="text-[10px] text-slate-500 font-mono">
                        {item.employeeCode} • {item.departmentName}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 text-right">
                    <div className="font-mono font-bold text-slate-800 dark:text-slate-200">
                      {item.checkInTime || '-'}
                    </div>
                    <MethodBadge method={item.attendanceVia} />
                    <StatusBadge status={item.attendanceStatus} />
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-8 text-xs text-slate-400">
                No attendance punches logged yet today.
              </div>
            )}
          </div>
        </div>

        {/* Recent Registrations Table */}
        <div className="glass-card p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                Recent Employee Enrolments
              </h2>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                Newly registered staff with biometric profiles
              </p>
            </div>
            <button
              onClick={() => navigate('/admin/employees')}
              className="text-xs text-primary-600 dark:text-primary-400 font-semibold hover:underline"
            >
              Manage &rarr;
            </button>
          </div>

          <div className="space-y-3">
            {stats?.recentRegistrations?.length > 0 ? (
              stats.recentRegistrations.map((emp) => (
                <div
                  key={emp.id}
                  className="flex items-center justify-between p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-100 dark:border-slate-800 text-xs"
                >
                  <div className="flex items-center gap-3">
                    <img
                      src={emp.profilePhotoUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100"}
                      alt={emp.fullName}
                      className="w-8 h-8 rounded-full object-cover"
                    />
                    <div>
                      <div className="font-semibold text-slate-900 dark:text-slate-100">
                        {emp.fullName}
                      </div>
                      <div className="text-[10px] text-slate-500">
                        {emp.employeeCode} • {emp.designationTitle}
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <span className="font-mono text-[11px] text-slate-600 dark:text-slate-400">
                      ₹{Number(emp.agreedMonthlySalary).toLocaleString('en-IN')}/mo
                    </span>
                    <StatusBadge status={emp.employmentStatus} />
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-8 text-xs text-slate-400">
                No recent employees found.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
