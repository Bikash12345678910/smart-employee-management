import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Coffee,
  Play,
  Square,
  Clock,
  Calendar,
  CheckCircle2,
  AlertCircle,
  RefreshCw,
  Utensils,
  Briefcase,
  UserCheck
} from 'lucide-react';
import { employeeApi } from '../../services/api';
import StatsCard from '../../components/common/StatsCard';

export default function MyBreaks() {
  const [breaks, setBreaks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [feedback, setFeedback] = useState(null);
  const [selectedReason, setSelectedReason] = useState('TEA_BREAK');

  const fetchBreaks = async () => {
    setLoading(true);
    setFeedback(null);
    try {
      const res = await employeeApi.getMyBreaks();
      if (res.success && res.data) {
        setBreaks(res.data);
      }
    } catch (err) {
      setFeedback({ type: 'error', message: err.message || 'Failed to load breaks' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchBreaks();
  }, []);

  // Check if an ongoing active break exists (breakEndTime is null)
  const activeBreak = breaks.find((b) => !b.breakEndTime);

  const handleStartBreak = async () => {
    setActionLoading(true);
    setFeedback(null);
    try {
      const res = await employeeApi.breakAction({
        action: 'START',
        breakType: selectedReason,
      });
      if (res.success) {
        setFeedback({
          type: 'success',
          message: `Break started at ${res.data.breakStartTime || 'now'}. Enjoy your pause!`,
        });
        fetchBreaks();
      }
    } catch (err) {
      setFeedback({ type: 'error', message: err.message || 'Failed to start break' });
    } finally {
      setActionLoading(false);
    }
  };

  const handleEndBreak = async () => {
    setActionLoading(true);
    setFeedback(null);
    try {
      const res = await employeeApi.breakAction({
        action: 'END',
      });
      if (res.success) {
        setFeedback({
          type: 'success',
          message: `Break finished! Total duration: ${res.data.durationMinutes || 0} minutes. Welcome back!`,
        });
        fetchBreaks();
      }
    } catch (err) {
      setFeedback({ type: 'error', message: err.message || 'Failed to end break' });
    } finally {
      setActionLoading(false);
    }
  };

  // Stats calculations
  const todayDateStr = new Date().toISOString().split('T')[0];
  const todayBreaks = breaks.filter((b) => b.breakDate === todayDateStr);
  const todayBreakCount = todayBreaks.length;
  const todayTotalMinutes = todayBreaks.reduce((acc, b) => acc + (b.durationMinutes || 0), 0);
  const totalBreaksCount = breaks.length;
  const monthTotalMinutes = breaks.reduce((acc, b) => acc + (b.durationMinutes || 0), 0);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <Coffee className="w-6 h-6 text-amber-500" />
            My Break Management
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Log your shift tea, lunch, and rest breaks according to company attendance policies.
          </p>
        </div>
        <button
          onClick={fetchBreaks}
          disabled={loading || actionLoading}
          className="flex items-center gap-2 px-3.5 py-2 text-sm font-medium rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors shadow-xs"
        >
          <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Feedback banner */}
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
            <div className="flex-1 text-sm font-medium">{feedback.message}</div>
            <button
              onClick={() => setFeedback(null)}
              className="text-xs font-semibold underline ml-2 opacity-75 hover:opacity-100"
            >
              Dismiss
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Break Control Card */}
      <motion.div
        initial={{ opacity: 0, y: 15 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-xs relative overflow-hidden"
      >
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <span
                className={`px-2.5 py-1 text-xs font-bold rounded-lg ${
                  activeBreak
                    ? 'bg-amber-100 dark:bg-amber-950/80 text-amber-700 dark:text-amber-300 animate-pulse'
                    : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400'
                }`}
              >
                {activeBreak ? 'CURRENTLY ON BREAK' : 'CURRENTLY WORKING'}
              </span>
            </div>

            <h2 className="text-xl font-bold text-slate-900 dark:text-white">
              {activeBreak
                ? `Break started at ${activeBreak.breakStartTime}`
                : 'Ready for a tea or lunch break?'}
            </h2>

            <p className="text-sm text-slate-500 dark:text-slate-400">
              {activeBreak
                ? `Active Break Type: ${activeBreak.breakType || 'General Break'}. Click below when you return to your workstation.`
                : 'Select break type and click Start Break to automatically log your break time.'}
            </p>
          </div>

          {/* Action Trigger */}
          <div className="flex flex-col sm:flex-row items-center gap-3 w-full md:w-auto">
            {!activeBreak ? (
              <>
                <select
                  value={selectedReason}
                  onChange={(e) => setSelectedReason(e.target.value)}
                  disabled={actionLoading}
                  className="px-3 py-2.5 text-sm font-medium rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 focus:outline-hidden focus:ring-2 focus:ring-amber-500 w-full sm:w-auto"
                >
                  <option value="TEA_BREAK">Tea / Coffee Break (15 min)</option>
                  <option value="LUNCH_BREAK">Lunch Break (45 min)</option>
                  <option value="PERSONAL_BREAK">Short Personal Break (10 min)</option>
                  <option value="PRAYER_BREAK">Prayer / Rest Break</option>
                </select>

                <button
                  onClick={handleStartBreak}
                  disabled={actionLoading}
                  className="flex items-center justify-center gap-2 px-5 py-2.5 rounded-xl bg-amber-500 hover:bg-amber-600 text-white font-medium text-sm transition-all shadow-sm active:scale-95 w-full sm:w-auto"
                >
                  <Play className="w-4 h-4 fill-white" />
                  Start Break
                </button>
              </>
            ) : (
              <button
                onClick={handleEndBreak}
                disabled={actionLoading}
                className="flex items-center justify-center gap-2 px-6 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-sm transition-all shadow-md active:scale-95 w-full sm:w-auto"
              >
                <Square className="w-4 h-4 fill-white" />
                End Break & Resume Work
              </button>
            )}
          </div>
        </div>
      </motion.div>

      {/* Stats Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <StatsCard
          title="Today's Breaks"
          value={todayBreakCount}
          icon={Coffee}
          trend={`${todayTotalMinutes} mins total`}
          trendType="neutral"
        />
        <StatsCard
          title="Today's Break Time"
          value={`${todayTotalMinutes}m`}
          icon={Clock}
          trend="Within policy"
          trendType="up"
        />
        <StatsCard
          title="Total Logged Breaks"
          value={totalBreaksCount}
          icon={Calendar}
          trend="This period"
          trendType="neutral"
        />
        <StatsCard
          title="Monthly Break Time"
          value={`${Math.floor(monthTotalMinutes / 60)}h ${monthTotalMinutes % 60}m`}
          icon={Utensils}
          trend="Accumulated"
          trendType="neutral"
        />
      </div>

      {/* Break History Log Table */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-xs space-y-4">
        <h3 className="text-lg font-bold text-slate-900 dark:text-white">
          Break Activity Log
        </h3>

        <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-800">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 dark:bg-slate-800/60 text-xs font-bold text-slate-500 dark:text-slate-400 uppercase tracking-wider">
              <tr>
                <th className="px-4 py-3">Date</th>
                <th className="px-4 py-3">Break Type</th>
                <th className="px-4 py-3">Start Time</th>
                <th className="px-4 py-3">End Time</th>
                <th className="px-4 py-3">Duration</th>
                <th className="px-4 py-3">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {loading ? (
                <tr>
                  <td colSpan={6} className="px-4 py-10 text-center text-slate-400">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-amber-500" />
                    Loading break logs...
                  </td>
                </tr>
              ) : breaks.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-4 py-10 text-center text-slate-400">
                    <Coffee className="w-8 h-8 mx-auto mb-2 opacity-40" />
                    No break records logged yet.
                  </td>
                </tr>
              ) : (
                breaks.map((b) => (
                  <tr
                    key={b.id || Math.random()}
                    className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    <td className="px-4 py-3 font-semibold text-slate-900 dark:text-white">
                      {b.breakDate}
                    </td>
                    <td className="px-4 py-3 font-medium text-slate-700 dark:text-slate-300">
                      {b.breakType || 'General Break'}
                    </td>
                    <td className="px-4 py-3 font-mono text-xs">
                      {b.breakStartTime || '--:--'}
                    </td>
                    <td className="px-4 py-3 font-mono text-xs">
                      {b.breakEndTime || (
                        <span className="text-amber-500 font-semibold italic">Ongoing</span>
                      )}
                    </td>
                    <td className="px-4 py-3 font-bold text-slate-800 dark:text-slate-200">
                      {b.durationMinutes != null ? `${b.durationMinutes} mins` : '--'}
                    </td>
                    <td className="px-4 py-3">
                      {b.breakEndTime ? (
                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">
                          Completed
                        </span>
                      ) : (
                        <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300 animate-pulse">
                          Active
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
