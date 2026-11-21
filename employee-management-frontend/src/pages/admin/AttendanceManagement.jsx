import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { adminApi, downloadAuthenticatedFile } from '../../services/api';
import { StatusBadge, MethodBadge } from '../../components/common/Badge';
import { exportTableToPdf } from '../../utils/pdfGenerator';
import {
  CalendarCheck,
  Search,
  Filter,
  Download,
  Calendar,
  Clock,
  Edit3,
  UserX,
  Plus,
  RefreshCw,
  CheckCircle,
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  FileSpreadsheet,
  FileText,
  ListOrdered,
  LogIn,
  LogOut,
  X,
  Activity,
  UserCheck
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function AttendanceManagement() {
  const [searchParams] = useSearchParams();
  const initialStatus = searchParams.get('status') || '';

  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [search, setSearch] = useState('');
  const [selectedDept, setSelectedDept] = useState('');
  const [selectedStatus, setSelectedStatus] = useState(initialStatus);

  const [departments, setDepartments] = useState([]);
  const [attendances, setAttendances] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  // Multi-punch Drawer / Modal
  const [selectedEmployeePunches, setSelectedEmployeePunches] = useState(null);
  const [punchSummaryLoading, setPunchSummaryLoading] = useState(false);
  const [punchActionSubmitting, setPunchActionSubmitting] = useState(false);

  // Correction Modal
  const [correctionRecord, setCorrectionRecord] = useState(null);
  const [corrCheckIn, setCorrCheckIn] = useState('');
  const [corrCheckOut, setCorrCheckOut] = useState('');
  const [corrStatus, setCorrStatus] = useState('PRESENT');
  const [corrReason, setCorrReason] = useState('');
  const [corrSubmitting, setCorrSubmitting] = useState(false);

  // Download menu state
  const [showDownloadMenu, setShowDownloadMenu] = useState(false);

  const fetchAttendance = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getAttendance({
        date,
        departmentId: selectedDept || undefined,
        status: selectedStatus || undefined,
        search,
        page,
        size: 15
      });
      if (res.success && res.data) {
        setAttendances(res.data.content);
        setTotalElements(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      }
    } catch (err) {
      console.error("Failed to load attendance records:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    adminApi.getDepartments().then(res => {
      if (res.success) setDepartments(res.data);
    });
  }, []);

  useEffect(() => {
    fetchAttendance();
  }, [date, search, selectedDept, selectedStatus, page]);

  const loadEmployeePunchDetails = async (empId, empName, empCode) => {
    setPunchSummaryLoading(true);
    try {
      const res = await adminApi.getDailyPunches(empId, date);
      if (res.success && res.data) {
        setSelectedEmployeePunches({
          ...res.data,
          empId,
          empName: empName || res.data.employeeName,
          empCode: empCode || res.data.employeeCode,
        });
      }
    } catch (err) {
      alert("Error loading punch details: " + err.message);
    } finally {
      setPunchSummaryLoading(false);
    }
  };

  const handleAdminRecordPunch = async (punchType) => {
    if (!selectedEmployeePunches) return;
    setPunchActionSubmitting(true);
    try {
      await adminApi.punchAttendance({
        employeeId: selectedEmployeePunches.employeeId,
        punchType: punchType,
        punchMethod: 'ADMIN_MANUAL',
      });
      // Refresh punches
      await loadEmployeePunchDetails(
        selectedEmployeePunches.employeeId,
        selectedEmployeePunches.employeeName,
        selectedEmployeePunches.employeeCode
      );
      fetchAttendance();
    } catch (err) {
      alert("Punch error: " + err.message);
    } finally {
      setPunchActionSubmitting(false);
    }
  };

  const handleMarkAbsent = async () => {
    if (!confirm(`Mark unrecorded employees as ABSENT for ${date}?`)) return;
    try {
      await adminApi.markAbsent(date);
      fetchAttendance();
      alert("Absent evaluation executed successfully.");
    } catch (e) {
      alert("Error: " + e.message);
    }
  };

  const openCorrectionModal = (item) => {
    setCorrectionRecord(item);
    setCorrCheckIn(item.checkInTime || '09:00:00');
    setCorrCheckOut(item.checkOutTime || '18:00:00');
    setCorrStatus(item.attendanceStatus || 'PRESENT');
    setCorrReason('');
  };

  const handleSaveCorrection = async (e) => {
    e.preventDefault();
    if (!corrReason.trim()) {
      alert("Correction reason is required for audit compliance.");
      return;
    }
    setCorrSubmitting(true);
    try {
      await adminApi.correctAttendance({
        attendanceId: correctionRecord.id,
        checkInTime: corrCheckIn,
        checkOutTime: corrCheckOut,
        attendanceStatus: corrStatus,
        correctionReason: corrReason
      });
      setCorrectionRecord(null);
      fetchAttendance();
    } catch (err) {
      alert("Failed to save correction: " + err.message);
    } finally {
      setCorrSubmitting(false);
    }
  };

  // ==========================================
  // EXPORT / DOWNLOAD HANDLERS
  // ==========================================
  const handleDownloadDateCsv = () => {
    const url = adminApi.exportAttendanceCsvUrl(date, selectedDept, selectedStatus, search);
    downloadAuthenticatedFile(url, `Attendance_${date}.csv`);
    setShowDownloadMenu(false);
  };

  const handleDownloadDatePdf = () => {
    const headers = [
      'Emp Code',
      'Employee Name',
      'Department',
      'Check In',
      'Check Out',
      'Working Minutes',
      'Status',
      'Via',
    ];
    const rows = attendances.map(a => {
      const mins = a.totalWorkingMinutes || (a.workingHours || 0) * 60 + (a.workingMinutes || 0);
      return [
        a.employeeCode || '-',
        a.employeeName || '-',
        a.departmentName || '-',
        a.checkInTime || '-',
        a.checkOutTime || '-',
        `${mins} mins`,
        a.attendanceStatus || '-',
        a.attendanceVia || 'BIOMETRIC',
      ];
    });

    exportTableToPdf({
      title: 'Daily Employee Attendance Report',
      subtitle: `Date: ${date} • Total Records: ${attendances.length}`,
      dateInfo: `Shift Date: ${date}`,
      headers,
      rows,
      filename: `Attendance_Report_${date}.pdf`,
    });
    setShowDownloadMenu(false);
  };

  const handleDownloadIndividualCsv = (empId, empCode) => {
    const url = adminApi.exportEmployeeAttendanceCsvUrl(empId);
    downloadAuthenticatedFile(url, `Attendance_${empCode}_AllTime.csv`);
  };

  const handleDownloadIndividualPdf = (item) => {
    const headers = ['Attendance Date', 'Check In', 'Check Out', 'Working Minutes', 'Status', 'Method'];
    const mins = item.totalWorkingMinutes || (item.workingHours || 0) * 60 + (item.workingMinutes || 0);
    const rows = [
      [
        date,
        item.checkInTime || '-',
        item.checkOutTime || '-',
        `${mins} mins (${item.workingHours || 0}h ${item.workingMinutes || 0}m)`,
        item.attendanceStatus || '-',
        item.attendanceVia || 'BIOMETRIC',
      ],
    ];

    exportTableToPdf({
      title: `Individual Attendance Record: ${item.employeeName}`,
      subtitle: `Employee Code: ${item.employeeCode} • Department: ${item.departmentName || '-'}`,
      dateInfo: `Record Date: ${date}`,
      headers,
      rows,
      filename: `Attendance_${item.employeeCode}_${date}.pdf`,
    });
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <CalendarCheck className="w-6 h-6 text-primary-500" /> Attendance Management
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            Real-time biometric attendance with multiple check-in/out sequencing and minute calculations.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {/* Download Dropdown */}
          <div className="relative">
            <button
              onClick={() => setShowDownloadMenu(!showDownloadMenu)}
              className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-750 transition-colors shadow-xs flex items-center gap-1.5"
            >
              <Download className="w-3.5 h-3.5 text-primary-500" /> Download Records
            </button>

            {showDownloadMenu && (
              <div className="absolute right-0 mt-2 w-56 rounded-2xl bg-white dark:bg-slate-900 shadow-xl border border-slate-200 dark:border-slate-800 p-2 z-30 space-y-1">
                <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400 px-3 py-1">
                  Date-wise ({date})
                </div>
                <button
                  onClick={handleDownloadDateCsv}
                  className="w-full text-left px-3 py-2 text-xs font-medium rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 flex items-center gap-2 text-slate-700 dark:text-slate-200"
                >
                  <FileSpreadsheet className="w-4 h-4 text-emerald-500" /> Export Date Records (CSV)
                </button>
                <button
                  onClick={handleDownloadDatePdf}
                  className="w-full text-left px-3 py-2 text-xs font-medium rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 flex items-center gap-2 text-slate-700 dark:text-slate-200"
                >
                  <FileText className="w-4 h-4 text-rose-500" /> Export Date Records (PDF)
                </button>
              </div>
            )}
          </div>

          <button
            onClick={handleMarkAbsent}
            className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-rose-50 dark:bg-rose-950/60 text-rose-600 dark:text-rose-300 border border-rose-200 dark:border-rose-800 hover:bg-rose-100 dark:hover:bg-rose-900/40 transition-colors flex items-center gap-1.5"
          >
            <UserX className="w-3.5 h-3.5" /> Mark Absent
          </button>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="glass-card p-4 flex flex-col lg:flex-row items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-2.5 w-full lg:w-auto">
          {/* Calendar Date Picker */}
          <div className="flex items-center gap-1.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 px-3 py-1.5 rounded-xl text-xs">
            <Calendar className="w-3.5 h-3.5 text-primary-500" />
            <input
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              className="bg-transparent font-medium text-slate-800 dark:text-slate-100 focus:outline-none"
            />
          </div>

          {/* Department Filter */}
          <select
            value={selectedDept}
            onChange={(e) => setSelectedDept(e.target.value)}
            className="text-xs font-medium px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
          >
            <option value="">All Departments</option>
            {departments.map((d) => (
              <option key={d.id} value={d.id}>{d.name}</option>
            ))}
          </select>

          {/* Status Filter */}
          <select
            value={selectedStatus}
            onChange={(e) => setSelectedStatus(e.target.value)}
            className="text-xs font-medium px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
          >
            <option value="">All Statuses</option>
            <option value="PRESENT">Present</option>
            <option value="LATE">Late</option>
            <option value="HALF_DAY">Half Day</option>
            <option value="ON_LEAVE">On Leave</option>
            <option value="ABSENT">Absent</option>
            <option value="HOLIDAY">Holiday</option>
          </select>
        </div>

        {/* Search Input */}
        <div className="relative w-full lg:w-72">
          <Search className="w-3.5 h-3.5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search employee or code..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/50 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
          />
        </div>
      </div>

      {/* Attendance Table */}
      {(() => {
        const maxPunchCols = Math.max(
          2,
          ...attendances.map((a) => (Array.isArray(a.punches) ? a.punches.length : 0))
        );
        const totalCols = 7 + maxPunchCols;

        const formatPunchTime = (eventTime) => {
          if (!eventTime) return '-';
          const s = String(eventTime);
          return s.includes('T') ? s.split('T')[1].slice(0, 8) : s.slice(0, 8);
        };

        return (
      <div className="glass-card overflow-hidden border border-slate-200 dark:border-slate-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-600 dark:text-slate-300">
            <thead className="bg-slate-50 dark:bg-slate-800/70 border-b border-slate-200 dark:border-slate-800 text-[11px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              <tr>
                <th scope="col" className="px-4 py-3.5">Registered Employee</th>
                <th scope="col" className="px-4 py-3.5">Emp ID</th>
                {Array.from({ length: maxPunchCols }).map((_, idx) => (
                  <th key={`punch-h-${idx}`} scope="col" className="px-3 py-3.5 whitespace-nowrap">
                    Punch {idx + 1} ({idx % 2 === 0 ? 'In' : 'Out'})
                  </th>
                ))}
                <th scope="col" className="px-4 py-3.5">Total Working Minutes</th>
                <th scope="col" className="px-4 py-3.5">Multiple Sessions</th>
                <th scope="col" className="px-4 py-3.5">Status</th>
                <th scope="col" className="px-4 py-3.5">Punch Method</th>
                <th scope="col" className="px-4 py-3.5 text-right">Actions & Downloads</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {loading ? (
                <tr>
                  <td colSpan={totalCols} className="px-4 py-12 text-center text-slate-400">
                    <div className="w-6 h-6 border-2 border-primary-500/20 border-t-primary-600 rounded-full animate-spin mx-auto mb-2" />
                    Loading attendance entries...
                  </td>
                </tr>
              ) : attendances.length === 0 ? (
                <tr>
                  <td colSpan={totalCols} className="px-4 py-12 text-center text-slate-400">
                    No attendance records for selected criteria.
                  </td>
                </tr>
              ) : (
                attendances.map((item) => {
                  const totalMinutes = item.totalWorkingMinutes !== undefined
                    ? item.totalWorkingMinutes
                    : (item.workingHours || 0) * 60 + (item.workingMinutes || 0);
                  const rowPunches = Array.isArray(item.punches) ? item.punches : [];

                  return (
                    <tr key={item.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors">
                      {/* Registered Employee Name */}
                      <td className="px-4 py-3.5 whitespace-nowrap">
                        <div className="flex items-center gap-2.5">
                          <img
                            src={item.profilePhotoUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100"}
                            alt={item.employeeName}
                            className="w-8 h-8 rounded-full object-cover"
                          />
                          <div>
                            <div className="font-semibold text-slate-900 dark:text-slate-100">{item.employeeName}</div>
                            <div className="text-[10px] text-slate-400">{item.departmentName}</div>
                          </div>
                        </div>
                      </td>

                      {/* Code */}
                      <td className="px-4 py-3.5 whitespace-nowrap font-mono font-bold text-primary-600 dark:text-primary-400">
                        {item.employeeCode}
                      </td>

                      {/* Dynamic One-by-One Punch Columns */}
                      {Array.from({ length: maxPunchCols }).map((_, idx) => {
                        const punch = rowPunches[idx];
                        if (punch) {
                          const isCheckIn = String(punch.eventType || '').toUpperCase() === 'CHECK_IN';
                          return (
                            <td key={`punch-${item.id}-${idx}`} className="px-3 py-3.5 whitespace-nowrap font-mono">
                              <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-lg text-[11px] font-semibold ${
                                isCheckIn
                                  ? 'bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-800'
                                  : 'bg-amber-50 dark:bg-amber-950/60 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-800'
                              }`}>
                                <span className="text-[9px] font-extrabold uppercase">{isCheckIn ? 'IN' : 'OUT'}</span>
                                {formatPunchTime(punch.eventTime)}
                              </span>
                            </td>
                          );
                        }
                        if (rowPunches.length === 0 && idx === 0 && item.checkInTime) {
                          return (
                            <td key={`punch-${item.id}-${idx}`} className="px-3 py-3.5 whitespace-nowrap font-mono">
                              <span className={item.lateMinutes > 0 ? "text-amber-600 font-semibold" : "text-emerald-600 font-semibold"}>
                                {item.checkInTime} {item.lateMinutes > 0 && `(+${item.lateMinutes}m)`}
                              </span>
                            </td>
                          );
                        }
                        if (rowPunches.length === 0 && idx === 1 && item.checkOutTime) {
                          return (
                            <td key={`punch-${item.id}-${idx}`} className="px-3 py-3.5 whitespace-nowrap font-mono">
                              <span className="font-semibold text-slate-800 dark:text-slate-200">{item.checkOutTime}</span>
                            </td>
                          );
                        }
                        return (
                          <td key={`punch-${item.id}-${idx}`} className="px-3 py-3.5 whitespace-nowrap font-mono text-slate-400">
                            -
                          </td>
                        );
                      })}

                      {/* Duration in Minutes */}
                      <td className="px-4 py-3.5 whitespace-nowrap font-mono">
                        {totalMinutes > 0 ? (
                          <div className="flex flex-col">
                            <span className="font-bold text-slate-900 dark:text-slate-100 text-xs">
                              {totalMinutes} mins
                            </span>
                            <span className="text-[10px] text-slate-400">
                              ({Math.floor(totalMinutes / 60)}h {totalMinutes % 60}m)
                            </span>
                          </div>
                        ) : (
                          <span className="text-slate-400">-</span>
                        )}
                      </td>

                      {/* Multiple Punch Indicator & Button */}
                      <td className="px-4 py-3.5 whitespace-nowrap">
                        <button
                          onClick={() => loadEmployeePunchDetails(item.employeeId, item.employeeName, item.employeeCode)}
                          className="px-2.5 py-1 text-[11px] font-semibold rounded-lg bg-indigo-50 dark:bg-indigo-950/60 text-indigo-700 dark:text-indigo-300 hover:bg-indigo-100 transition-colors flex items-center gap-1.5"
                        >
                          <ListOrdered className="w-3.5 h-3.5" /> View Punch Sequence
                        </button>
                      </td>

                      {/* Status */}
                      <td className="px-4 py-3.5 whitespace-nowrap">
                        <StatusBadge status={item.attendanceStatus} />
                        {item.isManualCorrection && (
                          <span className="ml-1 text-[9px] text-amber-500 font-bold" title={item.correctionReason}>
                            *MOD
                          </span>
                        )}
                      </td>

                      {/* Via */}
                      <td className="px-4 py-3.5 whitespace-nowrap">
                        <MethodBadge method={item.attendanceVia} />
                      </td>

                      {/* Action & Individual Downloads */}
                      <td className="px-4 py-3.5 whitespace-nowrap text-right">
                        <div className="flex items-center justify-end gap-1.5">
                          <button
                            onClick={() => handleDownloadIndividualCsv(item.employeeId, item.employeeCode)}
                            title="Download Employee CSV"
                            className="p-1.5 rounded-lg text-emerald-600 hover:bg-emerald-50 dark:hover:bg-emerald-950/60"
                          >
                            <FileSpreadsheet className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleDownloadIndividualPdf(item)}
                            title="Download Employee PDF"
                            className="p-1.5 rounded-lg text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/60"
                          >
                            <FileText className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => openCorrectionModal(item)}
                            className="px-2 py-1 text-[11px] font-semibold rounded-lg bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 text-slate-700 dark:text-slate-200 transition-colors flex items-center gap-1"
                          >
                            <Edit3 className="w-3 h-3" /> Edit
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        <div className="p-4 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between text-xs text-slate-500">
          <div>
            Total <span className="font-semibold text-slate-800 dark:text-slate-200">{totalElements}</span> records
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span className="font-mono">Page {page + 1} of {Math.max(1, totalPages)}</span>
            <button
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
        );
      })()}

      {/* ========================================================================= */}
      {/* MULTIPLE CHECK-IN & CHECK-OUT SEQUENCE MODAL */}
      {/* ========================================================================= */}
      <AnimatePresence>
        {selectedEmployeePunches && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="glass-card w-full max-w-2xl max-h-[90vh] overflow-y-auto p-6 border border-slate-200 dark:border-slate-800 space-y-5 bg-white dark:bg-slate-900 shadow-2xl rounded-3xl"
            >
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
                <div className="flex items-center gap-2">
                  <div className="p-2 rounded-xl bg-primary-100 dark:bg-primary-950 text-primary-600">
                    <ListOrdered className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">
                      Daily Attendance Punches: {selectedEmployeePunches.employeeName} ({selectedEmployeePunches.employeeCode})
                    </h3>
                    <p className="text-xs text-slate-500">
                      Date: {selectedEmployeePunches.date} • Total Working Time: <span className="font-bold text-emerald-600">{selectedEmployeePunches.totalWorkingMinutes} mins</span>
                    </p>
                  </div>
                </div>

                <button onClick={() => setSelectedEmployeePunches(null)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              {/* Status Header */}
              <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800 flex items-center justify-between text-xs">
                <div>
                  <span className="text-slate-500">Current Day Status: </span>
                  <span className={`font-bold ${
                    selectedEmployeePunches.currentStatus === 'CHECKED_IN' ? 'text-emerald-600' : 'text-slate-700 dark:text-slate-300'
                  }`}>
                    {selectedEmployeePunches.currentStatus === 'CHECKED_IN' ? '● Currently Checked In' : '● Checked Out / Off-duty'}
                  </span>
                </div>

                {/* Quick Punch Action Buttons for Admin Override */}
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => handleAdminRecordPunch('CHECK_IN')}
                    disabled={punchActionSubmitting || selectedEmployeePunches.currentStatus === 'CHECKED_IN'}
                    className="px-3 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs flex items-center gap-1 disabled:opacity-40"
                  >
                    <LogIn className="w-3.5 h-3.5" /> Check-In
                  </button>
                  <button
                    onClick={() => handleAdminRecordPunch('CHECK_OUT')}
                    disabled={punchActionSubmitting || selectedEmployeePunches.currentStatus !== 'CHECKED_IN'}
                    className="px-3 py-1.5 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-semibold text-xs flex items-center gap-1 disabled:opacity-40"
                  >
                    <LogOut className="w-3.5 h-3.5" /> Check-Out
                  </button>
                </div>
              </div>

              {/* Punch Pairs Sequence Table */}
              <div className="space-y-2">
                <h4 className="text-xs font-bold text-slate-700 dark:text-slate-300 uppercase tracking-wider">
                  Check-In & Check-Out Sessions
                </h4>
                {selectedEmployeePunches.punchPairs.length === 0 ? (
                  <div className="p-6 text-center text-xs text-slate-400 border border-dashed rounded-2xl">
                    No punch events recorded for this employee on {selectedEmployeePunches.date}.
                  </div>
                ) : (
                  <div className="border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden">
                    <table className="w-full text-xs text-left">
                      <thead className="bg-slate-100 dark:bg-slate-800 font-bold text-slate-600 dark:text-slate-300">
                        <tr>
                          <th className="px-3 py-2.5"># Sequence</th>
                          <th className="px-3 py-2.5">Check-In Time</th>
                          <th className="px-3 py-2.5">Check-Out Time</th>
                          <th className="px-3 py-2.5">Duration (Minutes)</th>
                          <th className="px-3 py-2.5">Session Status</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                        {selectedEmployeePunches.punchPairs.map((pair) => (
                          <tr key={pair.sequenceNumber} className="hover:bg-slate-50 dark:hover:bg-slate-800/50">
                            <td className="px-3 py-2.5 font-bold font-mono">
                              Session #{pair.sequenceNumber}
                            </td>
                            <td className="px-3 py-2.5 font-mono text-emerald-600 font-bold">
                              {pair.checkInTime}
                            </td>
                            <td className="px-3 py-2.5 font-mono text-slate-700 dark:text-slate-300 font-bold">
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
                                  Active (Punched In)
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

              {/* Total Calculation Note */}
              <div className="p-3.5 rounded-xl bg-indigo-50 dark:bg-indigo-950/40 border border-indigo-100 dark:border-indigo-900 text-xs text-indigo-900 dark:text-indigo-200">
                <span className="font-bold">Calculated Working Time:</span> {selectedEmployeePunches.totalWorkingMinutes} minutes
                ({Math.floor(selectedEmployeePunches.totalWorkingMinutes / 60)} hours {selectedEmployeePunches.totalWorkingMinutes % 60} mins).
                Break intervals between Check-Out and the subsequent Check-In are automatically excluded from working minutes.
              </div>

              <div className="pt-2 flex justify-end">
                <button
                  onClick={() => setSelectedEmployeePunches(null)}
                  className="px-4 py-2 text-xs font-semibold rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200"
                >
                  Close
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Manual Attendance Correction Modal */}
      {correctionRecord && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs">
          <div className="glass-card w-full max-w-md p-6 border border-slate-200 dark:border-slate-800 space-y-5 bg-white dark:bg-slate-900 shadow-2xl rounded-3xl">
            <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
              <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">
                Manual Attendance Correction
              </h3>
              <button onClick={() => setCorrectionRecord(null)} className="text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSaveCorrection} className="space-y-4 text-xs">
              <div>
                <span className="text-slate-400">Employee:</span>
                <div className="font-bold text-slate-800 dark:text-slate-100">{correctionRecord.employeeName} ({correctionRecord.employeeCode})</div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Check In Time</label>
                  <input
                    type="time"
                    step="1"
                    value={corrCheckIn}
                    onChange={(e) => setCorrCheckIn(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono"
                  />
                </div>
                <div>
                  <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Check Out Time</label>
                  <input
                    type="time"
                    step="1"
                    value={corrCheckOut}
                    onChange={(e) => setCorrCheckOut(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono"
                  />
                </div>
              </div>

              <div>
                <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Attendance Status</label>
                <select
                  value={corrStatus}
                  onChange={(e) => setCorrStatus(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                >
                  <option value="PRESENT">Present</option>
                  <option value="LATE">Late</option>
                  <option value="HALF_DAY">Half Day</option>
                  <option value="ON_LEAVE">On Leave</option>
                  <option value="ABSENT">Absent</option>
                </select>
              </div>

              <div>
                <label className="block text-slate-600 dark:text-slate-400 font-semibold mb-1">Reason for Adjustment *</label>
                <textarea
                  rows="2"
                  placeholder="e.g. Turnstile sensor timeout / network latency adjustment"
                  value={corrReason}
                  onChange={(e) => setCorrReason(e.target.value)}
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                />
              </div>

              <div className="pt-2 flex items-center justify-end gap-2 border-t border-slate-100 dark:border-slate-800">
                <button
                  type="button"
                  onClick={() => setCorrectionRecord(null)}
                  className="px-4 py-2 font-semibold rounded-xl border border-slate-200 dark:border-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={corrSubmitting}
                  className="px-4 py-2 font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white"
                >
                  {corrSubmitting ? "Saving..." : "Save Correction"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
