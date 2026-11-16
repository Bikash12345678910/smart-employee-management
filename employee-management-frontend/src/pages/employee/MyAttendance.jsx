import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  CalendarCheck,
  CalendarX,
  Clock,
  Calendar,
  CheckCircle2,
  AlertCircle,
  TrendingUp,
  RefreshCw,
  Search,
  Filter,
  ChevronLeft,
  ChevronRight,
  ShieldCheck,
  ListOrdered,
  FileSpreadsheet,
  FileText,
  X,
  Download
} from 'lucide-react';
import { employeeApi, downloadAuthenticatedFile } from '../../services/api';
import StatsCard from '../../components/common/StatsCard';
import { StatusBadge, MethodBadge } from '../../components/common/Badge';
import { exportTableToPdf } from '../../utils/pdfGenerator';

export default function MyAttendance() {
  const currentDate = new Date();
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [selectedMonth, setSelectedMonth] = useState(currentDate.getMonth() + 1);
  const [selectedYear, setSelectedYear] = useState(currentDate.getFullYear());
  const [filterStatus, setFilterStatus] = useState('ALL');

  const [todayAttendance, setTodayAttendance] = useState(null);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);

  // Multiple punch details modal for a date
  const [selectedDatePunches, setSelectedDatePunches] = useState(null);
  const [loadingPunches, setLoadingPunches] = useState(false);

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const handleCalendarDateChange = (e) => {
    const val = e.target.value;
    if (val) {
      setSelectedDate(val);
      const d = new Date(val);
      setSelectedMonth(d.getMonth() + 1);
      setSelectedYear(d.getFullYear());
    }
  };

  const handlePrevMonth = () => {
    if (selectedMonth === 1) {
      setSelectedMonth(12);
      setSelectedYear(y => y - 1);
    } else {
      setSelectedMonth(m => m - 1);
    }
  };

  const handleNextMonth = () => {
    if (selectedMonth === 12) {
      setSelectedMonth(1);
      setSelectedYear(y => y + 1);
    } else {
      setSelectedMonth(m => m + 1);
    }
  };

  const fetchData = async () => {
    setLoading(true);
    setFeedback(null);
    try {
      const [todayRes, historyRes] = await Promise.all([
        employeeApi.getTodayAttendance().catch(() => ({ data: null })),
        employeeApi.getAttendanceHistory().catch(() => ({ data: [] })),
      ]);

      if (todayRes.success) {
        setTodayAttendance(todayRes.data);
      }
      if (historyRes.success && historyRes.data) {
        setHistory(historyRes.data);
      }
    } catch (err) {
      setFeedback({ type: 'error', message: err.message || 'Failed to load attendance data' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const viewDatePunches = async (dateStr) => {
    setLoadingPunches(true);
    try {
      const res = await employeeApi.getMyDailyPunches(dateStr);
      if (res.success && res.data) {
        setSelectedDatePunches(res.data);
      }
    } catch (err) {
      alert("Error loading punch sequence: " + err.message);
    } finally {
      setLoadingPunches(false);
    }
  };

  const handlePunch = async (punchType) => {
    setActionLoading(true);
    setFeedback(null);
    try {
      const res = await employeeApi.punchAttendance({
        punchType,
        punchMethod: 'MANUAL',
      });
      if (res.success) {
        setFeedback({
          type: 'success',
          message: `Attendance ${punchType === 'CHECK_IN' ? 'Check-In' : 'Check-Out'} recorded successfully!`,
        });
        fetchData();
        if (selectedDatePunches && selectedDatePunches.date === new Date().toISOString().split('T')[0]) {
          viewDatePunches(selectedDatePunches.date);
        }
      }
    } catch (err) {
      setFeedback({ type: 'error', message: err.message || 'Punch failed.' });
    } finally {
      setActionLoading(false);
    }
  };

  // Export My Records
  const handleExportCsv = () => {
    const url = employeeApi.exportMyAttendanceCsvUrl(selectedMonth, selectedYear);
    downloadAuthenticatedFile(url, `My_Attendance_${selectedYear}_${selectedMonth}.csv`);
  };

  const handleExportPdf = () => {
    const headers = ['Date', 'Check In', 'Check Out', 'Working Minutes', 'Status', 'Method'];
    const rows = filteredHistory.map(h => {
      const mins = h.totalWorkingMinutes || (h.workingHours || 0) * 60 + (h.workingMinutes || 0);
      return [
        h.attendanceDate,
        h.checkInTime || '-',
        h.checkOutTime || '-',
        `${mins} mins`,
        h.attendanceStatus,
        h.attendanceVia || 'BIOMETRIC',
      ];
    });

    exportTableToPdf({
      title: 'My Employee Attendance Statement',
      subtitle: `Period: ${monthNames[selectedMonth - 1]} ${selectedYear} • Authorized Employee Access`,
      dateInfo: `${monthNames[selectedMonth - 1]} ${selectedYear}`,
      headers,
      rows,
      filename: `My_Attendance_${selectedYear}_${selectedMonth}.pdf`,
    });
  };

  // Filter history by month, year, status
  const filteredHistory = history.filter((item) => {
    if (!item.attendanceDate) return false;
    const dateObj = new Date(item.attendanceDate);
    const monthMatch = dateObj.getMonth() + 1 === parseInt(selectedMonth, 10);
    const yearMatch = dateObj.getFullYear() === parseInt(selectedYear, 10);
    const statusMatch = filterStatus === 'ALL' || item.attendanceStatus === filterStatus;
    return monthMatch && yearMatch && statusMatch;
  });

  // Calculate stats for current filtered month
  const totalDays = filteredHistory.length;
  const presentDays = filteredHistory.filter((h) => h.attendanceStatus === 'PRESENT' || h.attendanceStatus === 'LATE').length;
  const lateDays = filteredHistory.filter((h) => h.attendanceStatus === 'LATE').length;
  const absentDays = filteredHistory.filter((h) => h.attendanceStatus === 'ABSENT').length;
  const totalWorkingMinutes = filteredHistory.reduce(
    (sum, h) => sum + (h.totalWorkingMinutes !== undefined ? h.totalWorkingMinutes : (h.workingHours || 0) * 60 + (h.workingMinutes || 0)),
    0
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <CalendarCheck className="w-6 h-6 text-primary-500" /> My Attendance Record
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-0.5">
            View multiple daily check-in/out timestamps, total working minutes, and monthly historical statements.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={handleExportCsv}
            className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 transition-colors shadow-xs"
          >
            <FileSpreadsheet className="w-3.5 h-3.5 text-emerald-500" /> Export CSV
          </button>
          <button
            onClick={handleExportPdf}
            className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 transition-colors shadow-xs"
          >
            <FileText className="w-3.5 h-3.5 text-rose-500" /> Export PDF
          </button>
          <button
            onClick={fetchData}
            disabled={loading || actionLoading}
            className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors shadow-xs"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </button>
        </div>
      </div>

      {/* Alert / Feedback message */}
      <AnimatePresence>
        {feedback && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className={`p-4 rounded-xl flex items-start gap-3 border shadow-xs ${
              feedback.type === 'success'
                ? 'bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-200'
                : 'bg-red-50 dark:bg-red-950/40 border-red-200 dark:border-red-800 text-red-800 dark:text-red-200'
            }`}
          >
            {feedback.type === 'success' ? (
              <CheckCircle2 className="w-5 h-5 flex-shrink-0 text-emerald-600 dark:text-emerald-400 mt-0.5" />
            ) : (
              <AlertCircle className="w-5 h-5 flex-shrink-0 text-red-600 dark:text-red-400 mt-0.5" />
            )}
            <div className="flex-1 text-xs font-medium">{feedback.message}</div>
            <button
              onClick={() => setFeedback(null)}
              className="text-xs font-semibold underline ml-2 opacity-75 hover:opacity-100"
            >
              Dismiss
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Monthly Statistics Overview */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard
          title="Present Days"
          value={presentDays}
          icon={CalendarCheck}
          trend={`${totalDays > 0 ? Math.round((presentDays / totalDays) * 100) : 0}% rate`}
          trendType="neutral"
        />
        <StatsCard
          title="Late Arrivals"
          value={lateDays}
          icon={Clock}
          trend={`${lateDays} occurrences`}
          trendType={lateDays > 0 ? 'down' : 'up'}
        />
        <StatsCard
          title="Absent Days"
          value={absentDays}
          icon={CalendarX}
          trend="Deduction applies"
          trendType={absentDays > 0 ? 'down' : 'up'}
        />
        <StatsCard
          title="Total Working Minutes"
          value={`${totalWorkingMinutes.toLocaleString()} mins`}
          icon={TrendingUp}
          trend={`${Math.floor(totalWorkingMinutes / 60)}h ${totalWorkingMinutes % 60}m total`}
          trendType="up"
        />
      </div>

      {/* Attendance History Section with Calendar Picker */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-xs space-y-5">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-slate-100 dark:border-slate-800">
          <div>
            <h3 className="text-base font-bold text-slate-900 dark:text-white">
              Attendance History & Daily Sessions
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              Select date or month to review chronological check-in and check-out logs and total minutes.
            </p>
          </div>

          {/* Calendar Picker Controls */}
          <div className="flex flex-wrap items-center gap-2">
            <button
              onClick={handlePrevMonth}
              title="Previous Month"
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-slate-50 text-slate-700 dark:text-slate-200"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>

            <div className="px-3.5 py-1.5 rounded-xl bg-primary-50 dark:bg-primary-950/60 border border-primary-200 dark:border-primary-800 text-primary-800 dark:text-primary-200 font-bold text-xs flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-primary-600" />
              <span>{monthNames[selectedMonth - 1]} {selectedYear}</span>
            </div>

            <button
              onClick={handleNextMonth}
              title="Next Month"
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-slate-50 text-slate-700 dark:text-slate-200"
            >
              <ChevronRight className="w-4 h-4" />
            </button>

            {/* Calendar Date Picker Input */}
            <div className="flex items-center gap-1 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 px-2.5 py-1.5 rounded-xl text-xs">
              <span className="text-[10px] text-slate-400 font-medium">Calendar:</span>
              <input
                type="date"
                value={selectedDate}
                onChange={handleCalendarDateChange}
                className="bg-transparent font-medium text-slate-800 dark:text-slate-100 focus:outline-none cursor-pointer"
              />
            </div>

            {/* Status Filter */}
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="px-2.5 py-1.5 text-xs font-medium rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 focus:ring-2 focus:ring-primary-500"
            >
              <option value="ALL">All Statuses</option>
              <option value="PRESENT">Present</option>
              <option value="LATE">Late</option>
              <option value="HALF_DAY">Half Day</option>
              <option value="ABSENT">Absent</option>
              <option value="ON_LEAVE">On Leave</option>
              <option value="HOLIDAY">Holiday</option>
            </select>
          </div>
        </div>

        {/* History Table */}
        <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-800">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-800/60 text-[11px] font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">First Check In</th>
                <th className="px-4 py-3">Last Check Out</th>
                <th className="px-4 py-3">Total Working Minutes</th>
                <th className="px-4 py-3">Multiple Sessions</th>
                <th className="px-4 py-3">Via Method</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {filteredHistory.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-4 py-8 text-center text-slate-400">
                    No attendance records logged for {monthNames[selectedMonth - 1]} {selectedYear}.
                  </td>
                </tr>
              ) : (
                filteredHistory.map((item) => {
                  const totalMinutes = item.totalWorkingMinutes !== undefined
                    ? item.totalWorkingMinutes
                    : (item.workingHours || 0) * 60 + (item.workingMinutes || 0);

                  return (
                    <tr key={item.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors">
                      <td className="px-4 py-3 font-mono font-bold text-slate-900 dark:text-slate-100">
                        {item.attendanceDate}
                      </td>
                      <td className="px-4 py-3">
                        <StatusBadge status={item.attendanceStatus} />
                      </td>
                      <td className="px-4 py-3 font-mono font-semibold text-emerald-600">
                        {item.checkInTime || '-'}
                      </td>
                      <td className="px-4 py-3 font-mono font-semibold text-slate-700 dark:text-slate-300">
                        {item.checkOutTime || '-'}
                      </td>
                      <td className="px-4 py-3 font-mono font-bold text-indigo-600 dark:text-indigo-400">
                        {totalMinutes} mins <span className="text-[10px] text-slate-400 font-normal">({Math.floor(totalMinutes / 60)}h {totalMinutes % 60}m)</span>
                      </td>
                      <td className="px-4 py-3">
                        <button
                          onClick={() => viewDatePunches(item.attendanceDate)}
                          className="px-2.5 py-1 text-[11px] font-semibold rounded-lg bg-indigo-50 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300 hover:bg-indigo-100 transition-colors flex items-center gap-1.5"
                        >
                          <ListOrdered className="w-3.5 h-3.5" /> View Punch Sequence
                        </button>
                      </td>
                      <td className="px-4 py-3">
                        <MethodBadge method={item.attendanceVia} />
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* MULTIPLE CHECK-IN & CHECK-OUT SEQUENCE MODAL FOR EMPLOYEE */}
      {/* ========================================================================= */}
      <AnimatePresence>
        {selectedDatePunches && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="glass-card w-full max-w-xl max-h-[90vh] overflow-y-auto p-6 border border-slate-200 dark:border-slate-800 space-y-4 bg-white dark:bg-slate-900 shadow-2xl rounded-3xl"
            >
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
                <div className="flex items-center gap-2">
                  <div className="p-2 rounded-xl bg-primary-100 dark:bg-primary-950 text-primary-600">
                    <ListOrdered className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">
                      My Daily Punches: {selectedDatePunches.date}
                    </h3>
                    <p className="text-xs text-slate-500">
                      Total Working Minutes: <strong className="text-emerald-600">{selectedDatePunches.totalWorkingMinutes} mins</strong> (Break intervals excluded)
                    </p>
                  </div>
                </div>

                <button onClick={() => setSelectedDatePunches(null)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Sessions List */}
              <div className="space-y-2 text-xs">
                {selectedDatePunches.punchPairs.length === 0 ? (
                  <div className="p-6 text-center text-slate-400 border border-dashed rounded-2xl">
                    No punch entries recorded on this date.
                  </div>
                ) : (
                  <div className="border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden">
                    <table className="w-full text-left">
                      <thead className="bg-slate-100 dark:bg-slate-800 font-bold text-slate-600 dark:text-slate-300">
                        <tr>
                          <th className="px-3 py-2.5">Session #</th>
                          <th className="px-3 py-2.5">Check-In Time</th>
                          <th className="px-3 py-2.5">Check-Out Time</th>
                          <th className="px-3 py-2.5">Duration</th>
                          <th className="px-3 py-2.5">Status</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                        {selectedDatePunches.punchPairs.map((pair) => (
                          <tr key={pair.sequenceNumber} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                            <td className="px-3 py-2.5 font-bold font-mono">
                              #{pair.sequenceNumber}
                            </td>
                            <td className="px-3 py-2.5 font-mono text-emerald-600 font-bold">
                              {pair.checkInTime}
                            </td>
                            <td className="px-3 py-2.5 font-mono font-bold text-slate-700 dark:text-slate-300">
                              {pair.checkOutTime || <span className="text-amber-500 italic">In Progress</span>}
                            </td>
                            <td className="px-3 py-2.5 font-mono font-bold text-indigo-600">
                              {pair.workingMinutes} mins
                            </td>
                            <td className="px-3 py-2.5">
                              {pair.status === 'COMPLETED' ? (
                                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-emerald-100 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300">
                                  Completed
                                </span>
                              ) : (
                                <span className="px-2 py-0.5 rounded text-[10px] font-bold bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300">
                                  In Progress
                                </span>
                              )}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>

              <div className="pt-2 flex justify-end">
                <button
                  onClick={() => setSelectedDatePunches(null)}
                  className="px-4 py-2 text-xs font-semibold rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200"
                >
                  Close
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
