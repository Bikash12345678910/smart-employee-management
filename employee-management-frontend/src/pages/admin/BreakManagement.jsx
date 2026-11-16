import React, { useState, useEffect } from 'react';
import { adminApi } from '../../services/api';
import { Coffee, Search, Calendar, Download, RefreshCw, ChevronLeft, ChevronRight } from 'lucide-react';
import { motion } from 'framer-motion';

export default function BreakManagement() {
  const [date, setDate] = useState(new Date().toISOString().split('T')[0]);
  const [breaks, setBreaks] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const fetchBreaks = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getBreaks({
        date,
        page,
        size: 15
      });
      if (res.success && res.data) {
        setBreaks(res.data.content);
        setTotalElements(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      }
    } catch (err) {
      console.error("Failed to load breaks:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBreaks();
  }, [date, page]);

  const handleExportCsv = () => {
    window.open(adminApi.exportBreaksCsvUrl(date), '_blank');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Coffee className="w-6 h-6 text-amber-500" /> Employee Break Logs
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            Monitor interval tea/lunch breaks, durations, and compliance with company break caps.
          </p>
        </div>

        <button
          onClick={handleExportCsv}
          className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-750 transition-colors shadow-xs flex items-center gap-1.5 self-start"
        >
          <Download className="w-3.5 h-3.5" /> Export Break CSV
        </button>
      </div>

      {/* Date Filter */}
      <div className="glass-card p-4 flex items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Calendar className="w-4 h-4 text-primary-500" />
          <span className="text-xs font-semibold text-slate-700 dark:text-slate-300">Select Date:</span>
          <input
            type="date"
            value={date}
            onChange={(e) => {
              setDate(e.target.value);
              setPage(0);
            }}
            className="text-xs font-medium px-3 py-1.5 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>

        <button
          onClick={fetchBreaks}
          className="p-1.5 rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300"
        >
          <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Table */}
      <div className="glass-card overflow-hidden border border-slate-200 dark:border-slate-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-600 dark:text-slate-300">
            <thead className="bg-slate-50 dark:bg-slate-800/70 border-b border-slate-200 dark:border-slate-800 text-[11px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              <tr>
                <th scope="col" className="px-4 py-3.5">Employee</th>
                <th scope="col" className="px-4 py-3.5">Employee ID</th>
                <th scope="col" className="px-4 py-3.5">Date</th>
                <th scope="col" className="px-4 py-3.5">Break Start</th>
                <th scope="col" className="px-4 py-3.5">Break End</th>
                <th scope="col" className="px-4 py-3.5">Duration</th>
                <th scope="col" className="px-4 py-3.5">Reason / Type</th>
                <th scope="col" className="px-4 py-3.5 text-right">Status</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {loading ? (
                <tr>
                  <td colSpan="8" className="px-4 py-12 text-center text-slate-400">
                    <div className="w-6 h-6 border-2 border-primary-500/20 border-t-primary-600 rounded-full animate-spin mx-auto mb-2" />
                    Loading break logs...
                  </td>
                </tr>
              ) : breaks.length === 0 ? (
                <tr>
                  <td colSpan="8" className="px-4 py-12 text-center text-slate-400">
                    No breaks recorded for this date.
                  </td>
                </tr>
              ) : (
                breaks.map((b) => (
                  <tr key={b.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors">
                    <td className="px-4 py-3.5 whitespace-nowrap font-semibold text-slate-900 dark:text-slate-100">
                      {b.employeeName}
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap font-mono font-bold text-primary-600 dark:text-primary-400">
                      {b.employeeCode}
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap font-mono">{b.breakDate}</td>
                    <td className="px-4 py-3.5 whitespace-nowrap font-mono text-slate-800 dark:text-slate-200">
                      {b.breakStartTime}
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap font-mono text-slate-800 dark:text-slate-200">
                      {b.breakEndTime || <span className="text-amber-500 font-semibold animate-pulse">In Progress...</span>}
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap font-mono font-bold text-amber-600 dark:text-amber-400">
                      {b.durationMinutes} mins
                    </td>
                    <td className="px-4 py-3.5 whitespace-nowrap">{b.breakReason}</td>
                    <td className="px-4 py-3.5 whitespace-nowrap text-right">
                      {b.breakEndTime ? (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-emerald-100 dark:bg-emerald-950 text-emerald-600">
                          Completed
                        </span>
                      ) : (
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-semibold bg-amber-100 dark:bg-amber-950 text-amber-600">
                          On Break
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        <div className="p-4 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between text-xs text-slate-500">
          <div>Total {totalElements} break intervals</div>
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
    </div>
  );
}
