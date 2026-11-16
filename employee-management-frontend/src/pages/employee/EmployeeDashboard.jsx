import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { employeeApi } from '../../services/api';
import StatsCard from '../../components/common/StatsCard';
import { StatusBadge, MethodBadge } from '../../components/common/Badge';
import FaceScannerModal from '../../components/biometrics/FaceScannerModal';
import FingerprintScannerModal from '../../components/biometrics/FingerprintScannerModal';
import {
  CalendarCheck,
  CalendarX,
  Clock,
  CalendarOff,
  User,
  Coffee,
  Calendar,
  CheckCircle2,
  AlertCircle,
  Camera,
  Fingerprint,
  Lock,
  ArrowRight
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function EmployeeDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [punching, setPunching] = useState(false);
  const [breakLoading, setBreakLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Modals
  const [showFaceModal, setShowFaceModal] = useState(false);
  const [showFpModal, setShowFpModal] = useState(false);
  const [pendingPunchType, setPendingPunchType] = useState('CHECK_IN');

  const navigate = useNavigate();

  const fetchStats = async () => {
    setLoading(true);
    try {
      const res = await employeeApi.getDashboardStats();
      if (res.success && res.data) {
        setStats(res.data);
      }
    } catch (err) {
      console.error("Failed to load employee dashboard stats:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const triggerPunchWithBiometric = (type, method) => {
    setPendingPunchType(type);
    if (method === 'FACE') {
      setShowFaceModal(true);
    } else {
      setShowFpModal(true);
    }
  };

  const handleFaceCaptured = async (base64) => {
    executePunch(pendingPunchType, 'FACE', base64);
  };

  const handleFingerprintScanned = async (fpData) => {
    executePunch(pendingPunchType, 'FINGERPRINT', fpData.template, fpData.deviceId);
  };

  const executePunch = async (punchType, punchMethod, biometricData = null, deviceId = null) => {
    setPunching(true);
    setFeedback(null);
    try {
      const res = await employeeApi.punchAttendance({
        punchType,
        punchMethod,
        biometricData,
        deviceId
      });
      if (res.success) {
        setFeedback({
          type: 'success',
          text: `Attendance ${punchType === 'CHECK_IN' ? 'Check-In' : 'Check-Out'} successful via ${punchMethod}!`
        });
        fetchStats();
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.message });
    } finally {
      setPunching(false);
    }
  };

  const handleBreakAction = async (action) => {
    setBreakLoading(true);
    try {
      const res = await employeeApi.breakAction({ action });
      if (res.success) {
        setFeedback({
          type: 'success',
          text: `Break ${action === 'START' ? 'started' : 'ended'} successfully.`
        });
        fetchStats();
      }
    } catch (err) {
      setFeedback({ type: 'error', text: err.message });
    } finally {
      setBreakLoading(false);
    }
  };

  if (loading && !stats) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[50vh] space-y-3">
        <div className="w-8 h-8 border-3 border-emerald-500/20 border-t-emerald-600 rounded-full animate-spin" />
        <p className="text-xs text-slate-400">Loading your attendance workspace...</p>
      </div>
    );
  }

  const isCheckedIn = !!stats?.checkInTime;
  const isCheckedOut = !!stats?.checkOutTime;

  return (
    <div className="space-y-6 md:space-y-8">
      {/* Welcome Banner Card */}
      <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 bg-gradient-to-r from-primary-900/10 via-slate-900/5 to-emerald-900/10">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-5">
          <div className="flex items-center gap-4 text-center sm:text-left">
            <img
              src={stats?.profilePhotoUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200"}
              alt={stats?.fullName}
              className="w-16 h-16 rounded-2xl object-cover ring-2 ring-emerald-500/30 shadow-md"
            />
            <div>
              <div className="flex items-center gap-2 justify-center sm:justify-start">
                <h1 className="text-lg sm:text-xl font-bold text-slate-900 dark:text-slate-100">
                  Welcome back, {stats?.fullName || user?.employeeName}!
                </h1>
                <StatusBadge status={stats?.todayStatus} />
              </div>
              <p className="text-[11px] text-slate-500 font-mono mt-0.5">
                Staff ID: {stats?.employeeCode} • {new Date().toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}
              </p>
            </div>
          </div>

          {/* Quick Punch Terminal */}
          <div className="flex flex-wrap items-center justify-center gap-2 w-full sm:w-auto">
            {!isCheckedIn ? (
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  disabled={punching}
                  onClick={() => executePunch('CHECK_IN', 'MANUAL')}
                  className="px-5 py-2.5 rounded-xl font-semibold text-xs text-white bg-emerald-600 hover:bg-emerald-700 shadow-md shadow-emerald-600/20 transition-all flex items-center gap-1.5"
                >
                  <Clock className="w-4 h-4" /> Check-In
                </button>
              </div>
            ) : !isCheckedOut ? (
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  disabled={punching}
                  onClick={() => executePunch('CHECK_OUT', 'MANUAL')}
                  className="px-5 py-2.5 rounded-xl font-semibold text-xs text-white bg-rose-600 hover:bg-rose-700 shadow-md shadow-rose-600/20 transition-all flex items-center gap-1.5"
                >
                  <Clock className="w-4 h-4" /> Check-Out
                </button>
              </div>
            ) : (
              <div className="px-4 py-2 rounded-xl bg-emerald-50 dark:bg-emerald-950/80 text-emerald-600 dark:text-emerald-400 text-xs font-semibold flex items-center gap-1.5 border border-emerald-200 dark:border-emerald-800">
                <CheckCircle2 className="w-4 h-4" /> Shift Completed Today
              </div>
            )}
          </div>
        </div>
      </div>

      {feedback && (
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          className={`p-4 rounded-2xl text-xs flex items-center justify-between gap-2 border ${
            feedback.type === 'success'
              ? 'bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800'
              : 'bg-rose-50 dark:bg-rose-950/60 text-rose-700 dark:text-rose-300 border-rose-200 dark:border-rose-800'
          }`}
        >
          <div className="flex items-center gap-2">
            {feedback.type === 'success' ? <CheckCircle2 className="w-4 h-4" /> : <AlertCircle className="w-4 h-4" />}
            <span>{feedback.text}</span>
          </div>
          <button onClick={() => setFeedback(null)} className="text-slate-400 hover:text-slate-600">
            &times;
          </button>
        </motion.div>
      )}

      {/* Today's Punch & Break Status Card */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Check In Time */}
        <div className="glass-card p-4 flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-emerald-50 dark:bg-emerald-950 text-emerald-600">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <div className="text-[11px] font-semibold text-slate-500">Today Check-In</div>
            <div className="font-mono text-base font-bold text-slate-900 dark:text-slate-100">
              {stats?.checkInTime || 'Not checked in'}
            </div>
          </div>
        </div>

        {/* Check Out Time */}
        <div className="glass-card p-4 flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-rose-50 dark:bg-rose-950 text-rose-600">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <div className="text-[11px] font-semibold text-slate-500">Today Check-Out</div>
            <div className="font-mono text-base font-bold text-slate-900 dark:text-slate-100">
              {stats?.checkOutTime || (isCheckedIn ? 'Shift In Progress' : 'Pending')}
            </div>
          </div>
        </div>

        {/* Total Working Hours */}
        <div className="glass-card p-4 flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-primary-50 dark:bg-primary-950 text-primary-600">
            <CalendarCheck className="w-5 h-5" />
          </div>
          <div>
            <div className="text-[11px] font-semibold text-slate-500">Worked Today</div>
            <div className="font-mono text-base font-bold text-slate-900 dark:text-slate-100">
              {stats?.todayWorkingHours || 0}h {stats?.todayWorkingMinutes || 0}m
            </div>
          </div>
        </div>

        {/* Total Working Minutes Today */}
        <div className="glass-card p-4 flex items-center gap-3">
          <div className="p-2.5 rounded-xl bg-amber-50 dark:bg-amber-950 text-amber-600">
            <Clock className="w-5 h-5" />
          </div>
          <div>
            <div className="text-[11px] font-semibold text-slate-500">Total Working Minutes (Today)</div>
            <div className="font-mono text-base font-bold text-slate-900 dark:text-slate-100">
              {((stats?.todayWorkingHours || 0) * 60 + (stats?.todayWorkingMinutes || 0))} Minutes
            </div>
          </div>
        </div>
      </div>

      {/* 4 Animated Monthly Metric Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard
          title="Present Days (Month)"
          value={stats?.presentDays || 0}
          icon={CalendarCheck}
          color="emerald"
          subtitle="Days on duty"
          onViewList={() => navigate('/employee/attendance')}
          buttonLabel="View History"
        />
        <StatsCard
          title="Absent Days (Month)"
          value={stats?.absentDays || 0}
          icon={CalendarX}
          color="rose"
          subtitle="Days absent"
          onViewList={() => navigate('/employee/attendance')}
          buttonLabel="View History"
        />
        <StatsCard
          title="Leave Days (Month)"
          value={stats?.leaveDays || 0}
          icon={CalendarOff}
          color="blue"
          subtitle="Excused leaves"
          onViewList={() => navigate('/employee/attendance')}
          buttonLabel="View History"
        />
        <StatsCard
          title="Total Working Minutes"
          value={Math.round((stats?.totalWorkingHoursMonth || 0) * 60)}
          icon={Clock}
          color="primary"
          subtitle={`${Math.round(stats?.totalWorkingHoursMonth || 0)} hrs (${stats?.attendancePercentage || 0}% Monthly)`}
          onViewList={() => navigate('/employee/attendance')}
          buttonLabel="View Details"
        />
      </div>

      {/* Lower Grids: Recent Punches & Upcoming Holidays */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Attendance */}
        <div className="glass-card p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                My Recent Attendance Logs
              </h2>
              <p className="text-xs text-slate-500">Your latest biometric check-in/out records</p>
            </div>
            <button
              onClick={() => navigate('/employee/attendance')}
              className="text-xs text-primary-600 dark:text-primary-400 font-semibold hover:underline"
            >
              Full History &rarr;
            </button>
          </div>

          <div className="space-y-2.5">
            {stats?.recentAttendance?.length > 0 ? (
              stats.recentAttendance.map(item => (
                <div
                  key={item.id}
                  className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 flex items-center justify-between text-xs"
                >
                  <div>
                    <div className="font-semibold text-slate-900 dark:text-slate-100 font-mono">
                      {item.attendanceDate}
                    </div>
                    <div className="text-[10px] text-slate-400">
                      In: {item.checkInTime || '-'} • Out: {item.checkOutTime || '-'}
                    </div>
                  </div>

                  <div className="flex items-center gap-2">
                    <MethodBadge method={item.attendanceVia} />
                    <StatusBadge status={item.attendanceStatus} />
                  </div>
                </div>
              ))
            ) : (
              <div className="text-center py-8 text-xs text-slate-400">
                No past attendance records found.
              </div>
            )}
          </div>
        </div>

        {/* Upcoming Holidays */}
        <div className="glass-card p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100">
                Upcoming Company Holidays
              </h2>
              <p className="text-xs text-slate-500">Paid calendar breaks scheduled for your branch</p>
            </div>
            <button
              onClick={() => navigate('/employee/holidays')}
              className="text-xs text-primary-600 dark:text-primary-400 font-semibold hover:underline"
            >
              Calendar &rarr;
            </button>
          </div>

          <div className="space-y-2.5">
            {stats?.upcomingHolidays?.length > 0 ? (
              stats.upcomingHolidays.map(h => (
                <div
                  key={h.id}
                  className="p-3 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 flex items-center justify-between text-xs"
                >
                  <div className="flex items-center gap-3">
                    <div className="p-2 rounded-xl bg-indigo-50 dark:bg-indigo-950 text-indigo-600">
                      <Calendar className="w-4 h-4" />
                    </div>
                    <div>
                      <div className="font-semibold text-slate-900 dark:text-slate-100">
                        {h.holidayName}
                      </div>
                      <div className="text-[10px] text-slate-400">
                        {h.holidayDay} • {h.description}
                      </div>
                    </div>
                  </div>

                  <span className="font-mono font-bold text-indigo-600 dark:text-indigo-400 text-xs">
                    {h.holidayDate}
                  </span>
                </div>
              ))
            ) : (
              <div className="text-center py-8 text-xs text-slate-400">
                No upcoming holidays for the rest of this period.
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Sub-modals for Biometrics */}
      <FaceScannerModal
        isOpen={showFaceModal}
        onClose={() => setShowFaceModal(false)}
        onCapture={handleFaceCaptured}
        title="Face Biometric Punch"
        subtitle="Verifying facial landmarks for attendance entry"
      />

      <FingerprintScannerModal
        isOpen={showFpModal}
        onClose={() => setShowFpModal(false)}
        onScanComplete={handleFingerprintScanned}
        title="Fingerprint Sensor Punch"
      />
    </div>
  );
}
