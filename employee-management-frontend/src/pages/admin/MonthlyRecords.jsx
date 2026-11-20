import React, { useState, useEffect } from 'react';
import { adminApi, downloadAuthenticatedFile } from '../../services/api';
import { exportTableToPdf } from '../../utils/pdfGenerator';
import {
  CalendarDays,
  Search,
  Download,
  Calendar,
  RefreshCw,
  Percent,
  Clock,
  Filter,
  ChevronLeft,
  ChevronRight,
  FileSpreadsheet,
  FileText
} from 'lucide-react';
import { motion } from 'framer-motion';

export default function MonthlyRecords() {
  const currentDate = new Date();
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [month, setMonth] = useState(currentDate.getMonth() + 1);
  const [year, setYear] = useState(currentDate.getFullYear());
  const [search, setSearch] = useState('');
  const [matrixData, setMatrixData] = useState(null);
  const [loading, setLoading] = useState(true);

  // Quick Daily Entry (Date + Employee + Present/Late/Half Day + Minute Counter)
  const [entryEmpId, setEntryEmpId] = useState('');
  const [entryDate, setEntryDate] = useState(new Date().toISOString().split('T')[0]);
  const [entryStatus, setEntryStatus] = useState('PRESENT');
  const [entryCheckIn, setEntryCheckIn] = useState('09:00');
  const [entryCheckOut, setEntryCheckOut] = useState('18:00');
  const [savingEntry, setSavingEntry] = useState(false);
  const [entryFeedback, setEntryFeedback] = useState(null);

  const computeEntryMinutes = () => {
    if (entryStatus === 'ABSENT' || !entryCheckIn || !entryCheckOut) return 0;
    const [inH, inM] = entryCheckIn.split(':').map(Number);
    const [outH, outM] = entryCheckOut.split(':').map(Number);
    return Math.max(0, (outH * 60 + outM) - (inH * 60 + inM));
  };

  const handleEntryStatusChange = (status) => {
    setEntryStatus(status);
    if (status === 'PRESENT') {
      setEntryCheckIn('09:00');
      setEntryCheckOut('18:00');
    } else if (status === 'LATE') {
      setEntryCheckIn('09:30');
      setEntryCheckOut('18:00');
    } else if (status === 'HALF_DAY') {
      setEntryCheckIn('09:00');
      setEntryCheckOut('13:00');
    } else if (status === 'ABSENT') {
      setEntryCheckIn('');
      setEntryCheckOut('');
    }
  };

  const handleSaveDailyEntry = async (e) => {
    e.preventDefault();
    if (!entryEmpId) {
      setEntryFeedback({ type: 'error', text: 'Please select an employee first.' });
      return;
    }
    setSavingEntry(true);
    setEntryFeedback(null);
    try {
      const mins = computeEntryMinutes();
      await adminApi.correctAttendance({
        employeeId: Number(entryEmpId),
        attendanceDate: entryDate,
        checkInTime: entryStatus === 'ABSENT' ? null : `${entryCheckIn}:00`,
        checkOutTime: entryStatus === 'ABSENT' ? null : `${entryCheckOut}:00`,
        attendanceStatus: entryStatus,
        correctionReason: `Monthly Records entry: ${entryStatus} (${mins} mins)`,
      });
      setEntryFeedback({
        type: 'success',
        text: `Saved ${entryStatus} for ${entryDate} — Counted: ${mins} mins!`,
      });
      // Sync calendar to the entered date's month/year and refresh
      const d = new Date(entryDate);
      setMonth(d.getMonth() + 1);
      setYear(d.getFullYear());
      await fetchMatrix();
    } catch (err) {
      setEntryFeedback({ type: 'error', text: err.message || 'Failed to save attendance entry.' });
    } finally {
      setSavingEntry(false);
    }
  };

  // Sync date picker with month/year
  const handleCalendarDateChange = (e) => {
    const val = e.target.value;
    if (val) {
      setSelectedDate(val);
      const d = new Date(val);
      setMonth(d.getMonth() + 1);
      setYear(d.getFullYear());
    }
  };

  const handlePrevMonth = () => {
    if (month === 1) {
      setMonth(12);
      setYear(y => y - 1);
    } else {
      setMonth(m => m - 1);
    }
  };

  const handleNextMonth = () => {
    if (month === 12) {
      setMonth(1);
      setYear(y => y + 1);
    } else {
      setMonth(m => m + 1);
    }
  };

  const fetchMatrix = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getMonthlyMatrix(month, year);
      if (res.success && res.data) {
        setMatrixData(res.data);
      }
    } catch (err) {
      console.error("Failed to load monthly matrix:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMatrix();
  }, [month, year]);

  const daysInMonth = matrixData?.daysInMonth || 30;
  const records = (matrixData?.records || []).filter(r =>
    !search ||
    r.employeeName?.toLowerCase().includes(search.toLowerCase()) ||
    r.employeeCode?.toLowerCase().includes(search.toLowerCase()) ||
    r.department?.toLowerCase().includes(search.toLowerCase())
  );

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const getStatusClass = (code) => {
    switch (code) {
      case 'P':
        return 'bg-emerald-500 text-white font-bold';
      case 'L':
        return 'bg-amber-400 text-slate-900 font-bold';
      case 'H':
        return 'bg-purple-500 text-white font-bold';
      case 'LV':
        return 'bg-blue-500 text-white font-bold';
      case 'A':
        return 'bg-rose-500 text-white font-bold';
      case 'HL':
        return 'bg-indigo-400 text-white font-bold';
      default:
        return 'text-slate-300 dark:text-slate-600';
    }
  };

  const handleExportCsv = async () => {
    try {
      await downloadAuthenticatedFile(adminApi.exportMonthlyCsvUrl(month, year), `monthly-attendance-${year}-${month}.csv`);
    } catch (err) {
      alert("Failed to export monthly CSV: " + err.message);
    }
  };

  const handleExportPdf = () => {
    const headers = ['Emp Code', 'Employee Name', 'Department', 'Total Working Minutes', 'Present Days', 'Absent Days', 'Attendance %'];
    const rows = records.map(r => {
      const totalMinutes = (r.totalHours || 0) * 60 + (r.totalMinutes || 0);
      return [
        r.employeeCode,
        r.employeeName,
        r.department,
        `${totalMinutes} mins`,
        `${r.presentDays || 0}d`,
        `${r.absentDays || 0}d`,
        `${r.attendancePercentage || 0}%`,
      ];
    });

    exportTableToPdf({
      title: 'Monthly Attendance Matrix Report',
      subtitle: `Period: ${monthNames[month - 1]} ${year} • All Working Time Displayed in Minutes`,
      dateInfo: `${monthNames[month - 1]} ${year}`,
      headers,
      rows,
      filename: `Monthly_Attendance_${year}_${month}.pdf`,
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <CalendarDays className="w-6 h-6 text-primary-500" /> Monthly Attendance Records
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            Interactive calendar period matrix with working time converted strictly to <span className="font-bold text-primary-600">Minutes</span>.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleExportCsv}
            className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-750 transition-colors shadow-xs flex items-center gap-1.5"
          >
            <FileSpreadsheet className="w-3.5 h-3.5 text-emerald-500" /> Export CSV
          </button>
          <button
            onClick={handleExportPdf}
            className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-750 transition-colors shadow-xs flex items-center gap-1.5"
          >
            <FileText className="w-3.5 h-3.5 text-rose-500" /> Export PDF
          </button>
        </div>
      </div>

      {/* Calendar Navigation & Filter Bar */}
      <div className="glass-card p-4 space-y-3">
        <div className="flex flex-col md:flex-row items-center justify-between gap-3">
          {/* Interactive Calendar Month & Year Selector */}
          <div className="flex flex-wrap items-center gap-2 w-full md:w-auto">
            {/* Prev Month */}
            <button
              onClick={handlePrevMonth}
              title="Previous Month"
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-slate-50 text-slate-700 dark:text-slate-200"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>

            {/* Current Month & Year Display Badge */}
            <div className="px-4 py-2 rounded-xl bg-primary-50 dark:bg-primary-950/60 border border-primary-200 dark:border-primary-800 text-primary-800 dark:text-primary-200 font-bold text-xs flex items-center gap-2">
              <Calendar className="w-4 h-4 text-primary-600" />
              <span>{monthNames[month - 1]} {year}</span>
            </div>

            {/* Next Month */}
            <button
              onClick={handleNextMonth}
              title="Next Month"
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-slate-50 text-slate-700 dark:text-slate-200"
            >
              <ChevronRight className="w-4 h-4" />
            </button>

            {/* Date-Picker Calendar input to jump to any date */}
            <div className="flex items-center gap-1.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 px-3 py-1.5 rounded-xl text-xs ml-1">
              <span className="text-[11px] text-slate-400 font-medium">Jump To:</span>
              <input
                type="date"
                value={selectedDate}
                onChange={handleCalendarDateChange}
                className="bg-transparent font-medium text-slate-800 dark:text-slate-100 focus:outline-none cursor-pointer"
              />
            </div>

            <button
              onClick={fetchMatrix}
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300 ml-1"
              title="Refresh Matrix"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            </button>
          </div>

          {/* Search in matrix */}
          <div className="relative w-full md:w-72">
            <Search className="w-3.5 h-3.5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Search employee or code..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-9 pr-4 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/50 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
        </div>

        {/* Direct Date Attendance Entry (Present / Late / Half Day + Auto Minute Count) */}
        <form
          onSubmit={handleSaveDailyEntry}
          className="pt-3 border-t border-slate-100 dark:border-slate-800 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-7 gap-2.5 items-end"
        >
          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-500 mb-1">Entry Date</label>
            <input
              type="date"
              required
              value={entryDate}
              onChange={(e) => setEntryDate(e.target.value)}
              className="w-full text-xs px-2.5 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
            />
          </div>

          <div className="lg:col-span-2">
            <label className="block text-[10px] font-bold uppercase text-slate-500 mb-1">Employee</label>
            <select
              required
              value={entryEmpId}
              onChange={(e) => setEntryEmpId(e.target.value)}
              className="w-full text-xs px-2.5 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
            >
              <option value="">Select Employee...</option>
              {(matrixData?.records || []).map((rec) => (
                <option key={rec.employeeId} value={rec.employeeId}>
                  {rec.employeeName} ({rec.employeeCode})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-500 mb-1">Status</label>
            <select
              value={entryStatus}
              onChange={(e) => handleEntryStatusChange(e.target.value)}
              className="w-full text-xs px-2.5 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 font-semibold"
            >
              <option value="PRESENT">Present</option>
              <option value="LATE">Late</option>
              <option value="HALF_DAY">Half Day</option>
              <option value="ABSENT">Absent</option>
            </select>
          </div>

          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-500 mb-1">Check-In</label>
            <input
              type="time"
              disabled={entryStatus === 'ABSENT'}
              value={entryCheckIn}
              onChange={(e) => setEntryCheckIn(e.target.value)}
              className="w-full text-xs px-2.5 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 disabled:opacity-40"
            />
          </div>

          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-500 mb-1">
              Check-Out ({computeEntryMinutes()} mins)
            </label>
            <input
              type="time"
              disabled={entryStatus === 'ABSENT'}
              value={entryCheckOut}
              onChange={(e) => setEntryCheckOut(e.target.value)}
              className="w-full text-xs px-2.5 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 disabled:opacity-40"
            />
          </div>

          <div>
            <button
              type="submit"
              disabled={savingEntry}
              className="w-full py-2 px-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold shadow-xs disabled:opacity-50"
            >
              {savingEntry ? 'Saving...' : `Save (${computeEntryMinutes()}m)`}
            </button>
          </div>
        </form>

        {entryFeedback && (
          <div
            className={`p-2.5 rounded-xl text-xs font-semibold ${
              entryFeedback.type === 'success'
                ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800'
                : 'bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 border border-rose-200 dark:border-rose-800'
            }`}
          >
            {entryFeedback.text}
          </div>
        )}

        {/* Legend Indicators */}
        <div className="flex flex-wrap items-center gap-3 pt-2 border-t border-slate-100 dark:border-slate-800 text-[11px] text-slate-600 dark:text-slate-400">
          <span className="font-semibold text-slate-700 dark:text-slate-300">Status Legend:</span>
          <span className="flex items-center gap-1.5"><span className="w-4 h-4 rounded text-center text-[10px] bg-emerald-500 text-white font-bold leading-4">P</span> Present</span>
          <span className="flex items-center gap-1.5"><span className="w-4 h-4 rounded text-center text-[10px] bg-amber-400 text-slate-900 font-bold leading-4">L</span> Late</span>
          <span className="flex items-center gap-1.5"><span className="w-4 h-4 rounded text-center text-[10px] bg-purple-500 text-white font-bold leading-4">H</span> Half Day</span>
          <span className="flex items-center gap-1.5"><span className="w-4 h-4 rounded text-center text-[10px] bg-blue-500 text-white font-bold leading-4">LV</span> Leave</span>
          <span className="flex items-center gap-1.5"><span className="w-4 h-4 rounded text-center text-[10px] bg-rose-500 text-white font-bold leading-4">A</span> Absent</span>
          <span className="flex items-center gap-1.5"><span className="w-4 h-4 rounded text-center text-[10px] bg-indigo-400 text-white font-bold leading-4">HL</span> Holiday</span>
        </div>
      </div>

      {/* Matrix Table with Minutes Display */}
      <div className="glass-card overflow-hidden border border-slate-200 dark:border-slate-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead className="bg-slate-50 dark:bg-slate-800/80 border-b border-slate-200 dark:border-slate-800 text-[10px] uppercase font-bold text-slate-500 dark:text-slate-400 select-none">
              <tr>
                <th scope="col" className="px-3 py-3 sticky left-0 bg-slate-50 dark:bg-slate-800 z-10 min-w-[160px]">
                  Employee
                </th>
                {Array.from({ length: daysInMonth }, (_, i) => i + 1).map((d) => (
                  <th key={d} scope="col" className="w-7 text-center py-3 px-0.5 border-l border-slate-200/50 dark:border-slate-700/50 font-mono">
                    {d}
                  </th>
                ))}
                <th scope="col" className="px-3 py-3 text-center border-l border-slate-200 dark:border-slate-700 bg-indigo-50/50 dark:bg-indigo-950/30 text-indigo-700 dark:text-indigo-300 font-bold">
                  Total Working Minutes
                </th>
                <th scope="col" className="px-3 py-3 text-center">Present</th>
                <th scope="col" className="px-3 py-3 text-center">Absent</th>
                <th scope="col" className="px-3 py-3 text-center">Rate %</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-[11px]">
              {loading ? (
                <tr>
                  <td colSpan={daysInMonth + 5} className="py-12 text-center text-slate-400">
                    <div className="w-6 h-6 border-2 border-primary-500/20 border-t-primary-600 rounded-full animate-spin mx-auto mb-2" />
                    Calculating monthly matrix minutes...
                  </td>
                </tr>
              ) : records.length === 0 ? (
                <tr>
                  <td colSpan={daysInMonth + 5} className="py-12 text-center text-slate-400">
                    No employees matching search.
                  </td>
                </tr>
              ) : (
                records.map((r) => {
                  const totalMinutes = (r.totalHours || 0) * 60 + (r.totalMinutes || 0);

                  return (
                    <tr key={r.employeeId} className="hover:bg-slate-50/70 dark:hover:bg-slate-800/40 transition-colors">
                      {/* Sticky Employee Name & Code */}
                      <td className="px-3 py-2.5 whitespace-nowrap sticky left-0 bg-white dark:bg-slate-900 z-10 shadow-xs border-r border-slate-100 dark:border-slate-800">
                        <div className="font-semibold text-slate-900 dark:text-slate-100 truncate max-w-[150px]">
                          {r.employeeName}
                        </div>
                        <div className="text-[10px] text-slate-400 font-mono">{r.employeeCode} • {r.department}</div>
                      </td>

                      {/* Days 1..31 Cells */}
                      {Array.from({ length: daysInMonth }, (_, i) => i + 1).map((d) => {
                        const code = r.daily?.[d] || '-';
                        return (
                          <td key={d} className="p-0.5 text-center border-l border-slate-100 dark:border-slate-800/60">
                            <div className={`w-5 h-5 mx-auto rounded flex items-center justify-center text-[10px] ${getStatusClass(code)}`}>
                              {code}
                            </div>
                          </td>
                        );
                      })}

                      {/* TOTAL WORKING MINUTES - Strictly formatted in Minutes */}
                      <td className="px-3 py-2.5 text-center font-mono font-bold text-indigo-700 dark:text-indigo-300 bg-indigo-50/30 dark:bg-indigo-950/20 border-l border-slate-200 dark:border-slate-700 whitespace-nowrap">
                        <span className="text-xs">{totalMinutes} mins</span>
                      </td>

                      {/* Present Days */}
                      <td className="px-2 py-2.5 text-center font-mono font-bold text-emerald-600 dark:text-emerald-400 whitespace-nowrap">
                        {r.presentDays || 0}d
                      </td>

                      {/* Absent Days */}
                      <td className="px-2 py-2.5 text-center font-mono font-bold text-rose-600 dark:text-rose-400 whitespace-nowrap">
                        {r.absentDays || 0}d
                      </td>

                      {/* Percentage */}
                      <td className="px-2 py-2.5 text-center font-mono font-bold text-primary-600 dark:text-primary-400 whitespace-nowrap">
                        {r.attendancePercentage || 0}%
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
