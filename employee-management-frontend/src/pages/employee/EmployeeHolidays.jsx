import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Calendar,
  CalendarDays,
  Clock,
  Sparkles,
  RefreshCw,
  Search,
  Filter,
  CheckCircle2,
  CalendarCheck,
  ChevronLeft,
  ChevronRight,
  Star,
  FileText,
  FileSpreadsheet,
  X,
  Info
} from 'lucide-react';
import { employeeApi, downloadAuthenticatedFile } from '../../services/api';
import { exportTableToPdf } from '../../utils/pdfGenerator';
import StatsCard from '../../components/common/StatsCard';

export default function EmployeeHolidays() {
  const currentDate = new Date();
  const [currentMonth, setCurrentMonth] = useState(currentDate.getMonth()); // 0-11
  const [currentYear, setCurrentYear] = useState(currentDate.getFullYear());
  const [holidays, setHolidays] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedHolidayDetail, setSelectedHolidayDetail] = useState(null);

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  const fetchHolidays = async () => {
    setLoading(true);
    try {
      const res = await employeeApi.getHolidays(currentYear);
      if (res.success && res.data) {
        setHolidays(res.data);
      }
    } catch (err) {
      console.error('Failed to load holidays:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchHolidays();
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

  // Build holiday map by date
  const holidayMap = {};
  holidays.forEach(h => {
    holidayMap[h.holidayDate] = h;
  });

  const todayStr = new Date().toISOString().split('T')[0];
  const upcomingHolidays = holidays.filter((h) => h.holidayDate >= todayStr);
  const nextHoliday = upcomingHolidays[0];

  const firstDayOfMonth = new Date(currentYear, currentMonth, 1).getDay();
  const totalDaysInMonth = new Date(currentYear, currentMonth + 1, 0).getDate();

  const handleExportPdf = () => {
    const headers = ['Holiday Date', 'Day of Week', 'Holiday Name', 'Description'];
    const rows = holidays.map(h => [
      h.holidayDate,
      h.dayOfWeek || new Date(h.holidayDate).toLocaleDateString('en-US', { weekday: 'long' }),
      h.holidayName,
      h.description || '-',
    ]);

    exportTableToPdf({
      title: 'Company Holiday Schedule',
      subtitle: `Year: ${currentYear} • Approved Holidays for Employees`,
      dateInfo: `${currentYear}`,
      headers,
      rows,
      filename: `Company_Holidays_${currentYear}.pdf`,
    });
  };

  const filteredHolidays = holidays.filter(h =>
    !searchTerm ||
    h.holidayName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    h.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <Calendar className="w-6 h-6 text-primary-500" />
            Company Holiday Calendar
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
            Interactive company holiday calendar with date highlighting and holiday descriptions.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleExportPdf}
            className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 transition-colors shadow-xs"
          >
            <FileText className="w-3.5 h-3.5 text-rose-500" /> Download PDF
          </button>
          <button
            onClick={fetchHolidays}
            disabled={loading}
            className="flex items-center gap-1.5 px-3.5 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors shadow-xs"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {/* Top Banner / Next Holiday Card */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="md:col-span-2 p-5 rounded-2xl bg-gradient-to-r from-indigo-500 via-primary-600 to-indigo-700 text-white shadow-md flex items-center justify-between">
          <div>
            <span className="text-[10px] font-bold uppercase tracking-wider bg-white/20 px-2.5 py-1 rounded-full">
              Upcoming Celebration
            </span>
            <h3 className="text-lg font-black mt-2">
              {nextHoliday ? nextHoliday.holidayName : 'No immediate upcoming holiday'}
            </h3>
            <p className="text-xs text-white/80 mt-0.5">
              {nextHoliday ? `${nextHoliday.holidayDate} (${nextHoliday.dayOfWeek || 'Scheduled'}) • ${nextHoliday.description || 'Company Holiday'}` : 'All holidays for this cycle have concluded'}
            </p>
          </div>
          <Sparkles className="w-10 h-10 text-white/30" />
        </div>

        <StatsCard
          title="Total Holidays This Year"
          value={holidays.length}
          icon={CalendarCheck}
          trend={`${upcomingHolidays.length} upcoming`}
          trendType="neutral"
        />
      </div>

      {/* ========================================================================= */}
      {/* SECTION 1: INTERACTIVE CALENDAR INTERFACE */}
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

          <div className="text-xs text-slate-500">
            Click on any highlighted date to view official celebration details.
          </div>
        </div>

        {/* Days of Week Header */}
        <div className="grid grid-cols-7 gap-1.5 text-center text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400 py-1">
          {daysOfWeek.map((day, idx) => (
            <div key={day} className={`py-1 ${idx === 0 ? 'text-rose-500' : ''}`}>
              {day}
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
            const isSunday = new Date(currentYear, currentMonth, dayNum).getDay() === 0;

            return (
              <div
                key={dayNum}
                onClick={() => {
                  if (holiday) {
                    setSelectedHolidayDetail(holiday);
                  }
                }}
                className={`min-h-[85px] p-2 rounded-xl border transition-all flex flex-col justify-between ${
                  holiday
                    ? 'border-indigo-400 dark:border-indigo-600 bg-indigo-50/90 dark:bg-indigo-950/70 shadow-xs cursor-pointer hover:scale-[1.02]'
                    : isSunday
                    ? 'border-slate-200/60 dark:border-slate-800 bg-slate-50 dark:bg-slate-850/60'
                    : 'border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900'
                }`}
              >
                <div className="flex items-center justify-between">
                  <span className={`text-xs font-bold font-mono ${
                    holiday ? 'text-indigo-700 dark:text-indigo-300' : isSunday ? 'text-rose-500' : 'text-slate-800 dark:text-slate-200'
                  }`}>
                    {dayNum}
                  </span>
                  {holiday && (
                    <Star className="w-3.5 h-3.5 fill-indigo-500 text-indigo-500 animate-pulse" />
                  )}
                </div>

                {holiday ? (
                  <div className="mt-1 p-1 rounded-lg bg-indigo-100 dark:bg-indigo-900 text-[10px] font-bold text-indigo-900 dark:text-indigo-100 truncate" title={holiday.holidayName}>
                    {holiday.holidayName}
                  </div>
                ) : isSunday ? (
                  <div className="text-[10px] text-slate-400 italic">Weekly Off</div>
                ) : (
                  <div className="text-[10px] text-slate-300 dark:text-slate-700">Working Day</div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Selected Holiday Detail Dialog */}
      <AnimatePresence>
        {selectedHolidayDetail && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="glass-card w-full max-w-md p-6 border border-slate-200 dark:border-slate-800 space-y-4 bg-white dark:bg-slate-900 rounded-3xl shadow-2xl"
            >
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
                <div className="flex items-center gap-2">
                  <div className="p-2 rounded-xl bg-indigo-100 dark:bg-indigo-950 text-indigo-600">
                    <Star className="w-5 h-5 fill-indigo-500 text-indigo-500" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">
                      {selectedHolidayDetail.holidayName}
                    </h3>
                    <p className="text-[11px] text-slate-500">Official Company Observance</p>
                  </div>
                </div>
                <button onClick={() => setSelectedHolidayDetail(null)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-2 text-xs">
                <div className="flex justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Date:</span>
                  <span className="font-mono font-bold text-slate-900 dark:text-slate-100">{selectedHolidayDetail.holidayDate}</span>
                </div>
                <div className="flex justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Day:</span>
                  <span className="font-semibold">{selectedHolidayDetail.dayOfWeek || new Date(selectedHolidayDetail.holidayDate).toLocaleDateString('en-US', { weekday: 'long' })}</span>
                </div>
                <div className="flex justify-between p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Attendance Impact:</span>
                  <span className="font-bold text-emerald-600">Paid Holiday (Exempted)</span>
                </div>
                <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800 space-y-1">
                  <span className="text-slate-500">Description:</span>
                  <p className="text-slate-800 dark:text-slate-200">{selectedHolidayDetail.description || 'Official company holiday. Offices remain closed.'}</p>
                </div>
              </div>

              <div className="pt-2 flex justify-end">
                <button
                  onClick={() => setSelectedHolidayDetail(null)}
                  className="px-4 py-2 text-xs font-semibold rounded-xl bg-primary-600 text-white hover:bg-primary-700"
                >
                  Close
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* SECTION 2: List View */}
      <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100">
              All Scheduled Holidays ({currentYear})
            </h2>
            <p className="text-xs text-slate-500">Searchable list of all holidays for the year.</p>
          </div>

          <div className="relative w-full sm:w-64">
            <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-slate-400" />
            <input
              type="text"
              placeholder="Search holidays..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
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
                <th className="px-4 py-3">Details</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {filteredHolidays.map((h) => (
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
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
