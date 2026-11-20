import React, { useState, useEffect } from 'react';
import { adminApi, downloadAuthenticatedFile } from '../../services/api';
import { exportTableToPdf } from '../../utils/pdfGenerator';
import {
  Calendar,
  Plus,
  Download,
  Edit2,
  Trash2,
  Search,
  X,
  CheckCircle,
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  FileSpreadsheet,
  FileText,
  Star
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function HolidayManagement() {
  const currentDate = new Date();
  const [currentMonth, setCurrentMonth] = useState(currentDate.getMonth()); // 0-11
  const [currentYear, setCurrentYear] = useState(currentDate.getFullYear());
  const [holidays, setHolidays] = useState([]);
  const [weeklyOffDays, setWeeklyOffDays] = useState('SUNDAY');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);

  const isConfiguredWeeklyOff = (dayOfWeekIndex) => {
    if (weeklyOffDays === 'SATURDAY_SUNDAY') return dayOfWeekIndex === 0 || dayOfWeekIndex === 6;
    if (weeklyOffDays === 'FRIDAY') return dayOfWeekIndex === 5;
    return dayOfWeekIndex === 0;
  };

  // Selected date on calendar
  const [selectedCalendarDate, setSelectedCalendarDate] = useState(null);

  // Add / Edit Modal
  const [showModal, setShowModal] = useState(false);
  const [editId, setEditId] = useState(null);
  const [holidayName, setHolidayName] = useState('');
  const [holidayDate, setHolidayDate] = useState('');
  const [description, setDescription] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  const fetchHolidays = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getHolidays(currentYear);
      if (res.success && res.data) {
        setHolidays(res.data);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHolidays();
    adminApi.getSettings().then(res => {
      if (res.success && res.data?.weeklyOffDays) {
        setWeeklyOffDays(res.data.weeklyOffDays);
      }
    }).catch(() => {});
  }, [currentYear]);

  const handlePrevMonth = () => {
    if (currentMonth === 0) {
      setCurrentMonth(11);
      setCurrentYear(y => y - 1);
    } else {
      setCurrentMonth(m => m - 1);
    }
  };

  const handleNextMonth = () => {
    if (currentMonth === 11) {
      setCurrentMonth(0);
      setCurrentYear(y => y + 1);
    } else {
      setCurrentMonth(m => m + 1);
    }
  };

  const handleOpenAdd = (dateStr) => {
    setEditId(null);
    setHolidayName('');
    const defaultDate = dateStr || `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-01`;
    setHolidayDate(defaultDate);
    setDescription('');
    setErrorMsg('');
    setShowModal(true);
  };

  const handleOpenEdit = (h) => {
    setEditId(h.id);
    setHolidayName(h.holidayName);
    setHolidayDate(h.holidayDate);
    setDescription(h.description || '');
    setErrorMsg('');
    setShowModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!holidayName.trim() || !holidayDate) {
      setErrorMsg('Please enter both holiday name and date.');
      return;
    }

    // Duplicate check
    const existing = holidays.find(h => h.holidayDate === holidayDate && h.id !== editId);
    if (existing) {
      setErrorMsg(`A holiday ("${existing.holidayName}") is already scheduled for ${holidayDate}.`);
      return;
    }

    setErrorMsg('');
    setSubmitting(true);
    try {
      if (editId) {
        await adminApi.updateHoliday(editId, {
          holidayName,
          holidayDate,
          description
        });
      } else {
        await adminApi.addHoliday({
          holidayName,
          holidayDate,
          description,
          year: new Date(holidayDate).getFullYear()
        });
      }
      setShowModal(false);
      fetchHolidays();
    } catch (err) {
      setErrorMsg("Error saving holiday: " + err.message);
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id, name) => {
    if (!confirm(`Are you sure you want to remove the holiday "${name}"?`)) return;
    try {
      await adminApi.deleteHoliday(id);
      fetchHolidays();
    } catch (err) {
      alert("Error: " + err.message);
    }
  };

  const handleExportCsv = async () => {
    try {
      await downloadAuthenticatedFile(adminApi.exportHolidaysCsvUrl(currentYear), `holidays-${currentYear}.csv`);
    } catch (err) {
      alert("Failed to export holidays list: " + err.message);
    }
  };

  const handleExportPdf = () => {
    const headers = ['Holiday Date', 'Day of Week', 'Holiday Name', 'Description'];
    const rows = holidays.map(h => [
      h.holidayDate,
      h.dayOfWeek || new Date(h.holidayDate).toLocaleDateString('en-US', { weekday: 'long' }),
      h.holidayName,
      h.description || '-',
    ]);

    exportTableToPdf({
      title: 'Company Official Holiday Calendar',
      subtitle: `Year: ${currentYear} • Total Holidays: ${holidays.length}`,
      dateInfo: `Calendar Year ${currentYear}`,
      headers,
      rows,
      filename: `Company_Holidays_${currentYear}.pdf`,
    });
  };

  // Calendar calculations
  const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay(); // 0 = Sun
  const totalDaysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

  // Map holidays by date string 'YYYY-MM-DD'
  const holidayMap = {};
  holidays.forEach(h => {
    holidayMap[h.holidayDate] = h;
  });

  const filteredHolidays = holidays.filter(h =>
    !search ||
    h.holidayName.toLowerCase().includes(search.toLowerCase()) ||
    h.description?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Calendar className="w-6 h-6 text-primary-500" /> Company Holiday Calendar
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            Interactive monthly & yearly calendar with automated attendance holiday marking.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={handleExportCsv}
            className="px-3 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 transition-colors shadow-xs flex items-center gap-1.5"
          >
            <FileSpreadsheet className="w-3.5 h-3.5 text-emerald-500" /> Export CSV
          </button>
          <button
            onClick={handleExportPdf}
            className="px-3 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 transition-colors shadow-xs flex items-center gap-1.5"
          >
            <FileText className="w-3.5 h-3.5 text-rose-500" /> Export PDF
          </button>
          <button
            onClick={() => handleOpenAdd()}
            className="px-4 py-2 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-500/20 transition-all flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" /> Add Holiday
          </button>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* SECTION 1: FULL INTERACTIVE CALENDAR INTERFACE */}
      {/* ========================================================================= */}
      <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 space-y-4">
        {/* Calendar Month & Year Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-100 dark:border-slate-800">
          <div className="flex items-center gap-2">
            <button
              onClick={handlePrevMonth}
              title="Previous Month"
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <div className="px-4 py-2 rounded-xl bg-primary-50 dark:bg-primary-950/60 border border-primary-200 dark:border-primary-800 text-primary-800 dark:text-primary-200 font-bold text-sm">
              {monthNames[currentMonth]} {currentYear}
            </div>
            <button
              onClick={handleNextMonth}
              title="Next Month"
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 text-slate-700 dark:text-slate-200"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>

          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-500 font-medium">Select Year:</span>
            <select
              value={currentYear}
              onChange={(e) => setCurrentYear(Number(e.target.value))}
              className="text-xs font-semibold px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100"
            >
              {[2024, 2025, 2026, 2027, 2028].map(y => (
                <option key={y} value={y}>{y}</option>
              ))}
            </select>
          </div>
        </div>

        {/* Days of Week Header */}
        <div className="grid grid-cols-7 gap-1.5 text-center text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 py-1">
          {daysOfWeek.map((day, idx) => (
            <div key={day} className={`py-1 ${isConfiguredWeeklyOff(idx) ? 'text-rose-500' : ''}`}>
              {day} {isConfiguredWeeklyOff(idx) ? '(Off)' : ''}
            </div>
          ))}
        </div>

        {/* Calendar Days Grid */}
        <div className="grid grid-cols-7 gap-1.5">
          {/* Empty cells before month start */}
          {Array.from({ length: firstDayOfMonth }).map((_, i) => (
            <div key={`empty-${i}`} className="min-h-[85px] p-1.5 rounded-xl bg-slate-50/50 dark:bg-slate-900/30 border border-transparent" />
          ))}

          {/* Month Day Cells */}
          {Array.from({ length: totalDaysInMonth }).map((_, i) => {
            const dayNum = i + 1;
            const dateStr = `${currentYear}-${String(currentMonth + 1).padStart(2, '0')}-${String(dayNum).padStart(2, '0')}`;
            const holiday = holidayMap[dateStr];
            const isWeeklyOff = isConfiguredWeeklyOff(new Date(currentYear, currentMonth, dayNum).getDay());

            return (
              <div
                key={dayNum}
                onClick={() => {
                  setSelectedCalendarDate(dateStr);
                  if (holiday) {
                    handleOpenEdit(holiday);
                  } else {
                    handleOpenAdd(dateStr);
                  }
                }}
                className={`min-h-[85px] p-2 rounded-xl border transition-all cursor-pointer flex flex-col justify-between ${
                  holiday
                    ? 'border-indigo-400 dark:border-indigo-600 bg-indigo-50/80 dark:bg-indigo-950/60 shadow-xs'
                    : isWeeklyOff
                    ? 'border-rose-200/70 dark:border-rose-900/50 bg-rose-50/50 dark:bg-rose-950/20'
                    : 'border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 hover:border-primary-400 dark:hover:border-primary-600'
                }`}
              >
                <div className="flex items-center justify-between">
                  <span className={`text-xs font-bold font-mono ${
                    holiday ? 'text-indigo-700 dark:text-indigo-300' : isWeeklyOff ? 'text-rose-500' : 'text-slate-800 dark:text-slate-200'
                  }`}>
                    {dayNum}
                  </span>
                  {holiday && (
                    <Star className="w-3.5 h-3.5 fill-indigo-500 text-indigo-500" />
                  )}
                </div>

                {holiday ? (
                  <div className="mt-1 p-1 rounded-lg bg-indigo-100 dark:bg-indigo-900/80 text-[10px] font-bold text-indigo-900 dark:text-indigo-100 truncate" title={holiday.holidayName}>
                    {holiday.holidayName}
                  </div>
                ) : isWeeklyOff ? (
                  <div className="text-[10px] text-rose-500 font-medium italic">Weekly Off</div>
                ) : (
                  <div className="text-[10px] text-slate-300 dark:text-slate-600 opacity-0 hover:opacity-100 text-center">
                    + Add
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* ========================================================================= */}
      {/* SECTION 2: HOLIDAYS LIST TABLE */}
      {/* ========================================================================= */}
      <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100">
              Scheduled Holidays for Year {currentYear} ({filteredHolidays.length})
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Holidays automatically exempt attendance penalty calculation and mark status as HOLIDAY.
            </p>
          </div>

          <div className="relative w-full sm:w-64">
            <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-slate-400" />
            <input
              type="text"
              placeholder="Search holidays..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-8 pr-3 py-1.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
            />
          </div>
        </div>

        <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-800">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-800 text-slate-600 dark:text-slate-300 font-bold uppercase text-[11px] border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="px-4 py-3">Holiday Date</th>
                <th className="px-4 py-3">Day of Week</th>
                <th className="px-4 py-3">Holiday Name</th>
                <th className="px-4 py-3">Description</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {filteredHolidays.length === 0 ? (
                <tr>
                  <td colSpan="5" className="px-4 py-8 text-center text-slate-400">
                    No holidays configured for {currentYear}. Click "+ Add Holiday" to add one.
                  </td>
                </tr>
              ) : (
                filteredHolidays.map((h) => (
                  <tr key={h.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors">
                    <td className="px-4 py-3 font-mono font-bold text-slate-900 dark:text-slate-100">
                      {h.holidayDate}
                    </td>
                    <td className="px-4 py-3 text-slate-600 dark:text-slate-400">
                      {h.dayOfWeek || new Date(h.holidayDate).toLocaleDateString('en-US', { weekday: 'long' })}
                    </td>
                    <td className="px-4 py-3 font-semibold text-primary-600 dark:text-primary-400">
                      {h.holidayName}
                    </td>
                    <td className="px-4 py-3 text-slate-500">
                      {h.description || '-'}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => handleOpenEdit(h)}
                          title="Edit Holiday"
                          className="p-1.5 rounded-lg text-amber-600 hover:bg-amber-50 dark:hover:bg-amber-950/60"
                        >
                          <Edit2 className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => handleDelete(h.id, h.holidayName)}
                          title="Delete Holiday"
                          className="p-1.5 rounded-lg text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/60"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* ========================================================================= */}
      {/* ADD / EDIT HOLIDAY MODAL */}
      {/* ========================================================================= */}
      <AnimatePresence>
        {showModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="glass-card w-full max-w-md p-6 border border-slate-200 dark:border-slate-800 space-y-4 bg-white dark:bg-slate-900 rounded-3xl"
            >
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
                <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">
                  {editId ? 'Edit Holiday' : 'Add New Company Holiday'}
                </h3>
                <button onClick={() => setShowModal(false)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              {errorMsg && (
                <div className="p-3 rounded-xl bg-rose-50 dark:bg-rose-950/60 border border-rose-200 text-rose-700 text-xs">
                  {errorMsg}
                </div>
              )}

              <form onSubmit={handleSave} className="space-y-3 text-xs">
                <div>
                  <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                    Holiday Name *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Diwali (Festival of Lights)"
                    value={holidayName}
                    onChange={(e) => setHolidayName(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                  />
                </div>

                <div>
                  <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                    Holiday Date *
                  </label>
                  <input
                    type="date"
                    required
                    value={holidayDate}
                    onChange={(e) => setHolidayDate(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                  />
                </div>

                <div>
                  <label className="block text-slate-700 dark:text-slate-300 font-semibold mb-1">
                    Description / Details (Optional)
                  </label>
                  <textarea
                    rows="2"
                    placeholder="e.g. National celebration, offices remain closed"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="w-full px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                  />
                </div>

                <div className="pt-3 flex items-center justify-end gap-2 border-t border-slate-100 dark:border-slate-800">
                  <button
                    type="button"
                    onClick={() => setShowModal(false)}
                    className="px-4 py-2 font-semibold rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className="px-4 py-2 font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-xs"
                  >
                    {submitting ? 'Saving...' : (editId ? 'Update Holiday' : 'Save Holiday')}
                  </button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
