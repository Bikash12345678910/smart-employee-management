import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { exportIndividualPayslipPdf, formatINR } from '../../utils/pdfGenerator';
import {
  FileText,
  Printer,
  Calendar,
  DollarSign,
  Building,
  User,
  ShieldCheck,
  CheckCircle2,
  AlertCircle,
  Eye,
  X,
  RefreshCw,
  Clock,
  Search,
  TrendingUp,
  TrendingDown,
  Building2,
  Download
} from 'lucide-react';
import { employeeApi } from '../../services/api';
import StatsCard from '../../components/common/StatsCard';

export default function MyPayslips() {
  const [payslips, setPayslips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedPayslip, setSelectedPayslip] = useState(null);
  const [feedback, setFeedback] = useState(null);

  // Payslip Calendar Filter
  const now = new Date();
  const [selectedYear, setSelectedYear] = useState(now.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState(now.getMonth() + 1);

  const fetchPayslips = async () => {
    setLoading(true);
    setFeedback(null);
    try {
      const res = await employeeApi.getMyPayslips();
      if (res.success && res.data) {
        setPayslips(res.data);
      }
    } catch (err) {
      setFeedback({ type: 'error', message: err.message || 'Failed to load payslips' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPayslips();
  }, []);

  const handlePrint = () => {
    window.print();
  };

  const monthNames = [
    'All Months',
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const filteredPayslips = payslips.filter((p) => {
    if (selectedYear && p.salaryYear !== parseInt(selectedYear, 10)) {
      return false;
    }
    if (selectedMonth && selectedMonth !== 'ALL' && p.salaryMonth !== parseInt(selectedMonth, 10)) {
      return false;
    }
    return true;
  });

  const latestPayslip = filteredPayslips[0] || payslips[0];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <FileText className="w-6 h-6 text-emerald-600" />
            My Salary & Payslip Calendar
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            View automatic salary calculations, working minute adjustments, and download monthly payslips.
          </p>
        </div>

        {/* Payslip Calendar Controls */}
        <div className="flex flex-wrap items-center gap-2">
          <div className="flex items-center gap-1.5 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 px-3 py-1.5 rounded-xl shadow-xs">
            <Calendar className="w-4 h-4 text-emerald-600" />
            <select
              value={selectedMonth}
              onChange={(e) => setSelectedMonth(e.target.value === 'ALL' ? 'ALL' : parseInt(e.target.value, 10))}
              className="text-xs font-semibold bg-transparent text-slate-700 dark:text-slate-200 outline-none cursor-pointer"
            >
              <option value="ALL">All Months</option>
              {monthNames.slice(1).map((m, idx) => (
                <option key={m} value={idx + 1}>
                  {m}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-center gap-1.5 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 px-3 py-1.5 rounded-xl shadow-xs">
            <select
              value={selectedYear}
              onChange={(e) => setSelectedYear(parseInt(e.target.value, 10))}
              className="text-xs font-semibold bg-transparent text-slate-700 dark:text-slate-200 outline-none cursor-pointer"
            >
              {[2024, 2025, 2026, 2027].map((y) => (
                <option key={y} value={y}>
                  Year {y}
                </option>
              ))}
            </select>
          </div>

          <button
            onClick={fetchPayslips}
            disabled={loading}
            className="flex items-center gap-2 px-3 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-800 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors shadow-xs"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
        </div>
      </div>

      {/* Alert feedback */}
      <AnimatePresence>
        {feedback && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            className="p-4 rounded-xl flex items-start gap-3 border shadow-xs bg-red-50 dark:bg-red-950/40 border-red-200 dark:border-red-800 text-red-800 dark:text-red-200"
          >
            <AlertCircle className="w-5 h-5 flex-shrink-0 text-red-600 dark:text-red-400 mt-0.5" />
            <div className="flex-1 text-sm font-medium">{feedback.message}</div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Highlights */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatsCard
          title="Agreed Monthly Salary"
          value={latestPayslip ? `₹${parseFloat(latestPayslip.agreedMonthlySalary || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : '--'}
          icon={ShieldCheck}
          trend="Base Agreement"
          trendType="neutral"
        />
        <StatsCard
          title="Actual Payable Salary"
          value={latestPayslip ? `₹${parseFloat(latestPayslip.actualPayableSalary || latestPayslip.netPayableSalary || 0).toLocaleString('en-IN', { minimumFractionDigits: 2 })}` : '--'}
          icon={DollarSign}
          trend={latestPayslip ? `${monthNames[latestPayslip.salaryMonth]} ${latestPayslip.salaryYear}` : 'No slip'}
          trendType="up"
        />
        <StatsCard
          title="Normal Working Minutes"
          value={latestPayslip?.normalWorkingMinutes ? `${latestPayslip.normalWorkingMinutes} min` : '13,200 min'}
          icon={Clock}
          trend="Standard 220 hrs / mo"
          trendType="neutral"
        />
      </div>

      {/* Exact Employee Salary Calculation Table */}
      <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-6 shadow-xs space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-bold text-slate-900 dark:text-white flex items-center gap-2">
            <span className="w-2.5 h-6 rounded-full bg-emerald-500 inline-block"></span>
            Employee Salary Calculation Table
          </h3>
          <span className="text-xs text-slate-500 font-medium">
            Calculated per minute: Rate = Agreed Salary / 13,200 min
          </span>
        </div>

        <div className="overflow-x-auto rounded-xl border border-emerald-300 dark:border-slate-700 shadow-xs">
          <table className="w-full text-left text-xs border-collapse">
            <thead className="text-slate-950 text-[12px] font-bold border-b border-emerald-300">
              <tr className="bg-[#d8e8d2] text-slate-950">
                <th className="px-4 py-3.5 font-bold text-slate-950 border-r border-emerald-200/50">Employee Name</th>
                <th className="px-4 py-3.5 font-bold text-right text-slate-950 border-r border-emerald-200/50">Agreed Salary</th>
                <th className="px-4 py-3.5 font-bold text-center text-slate-950 border-r border-emerald-200/50">Actual Working time (Minute)</th>
                <th className="px-4 py-3.5 font-bold text-center text-slate-950 border-r border-emerald-200/50">Normal Working Time</th>
                <th className="px-4 py-3.5 font-bold text-center text-slate-950 border-r border-emerald-200/50">Difference (Minute)</th>
                <th className="px-4 py-3.5 font-bold text-right text-slate-950 border-r border-emerald-200/50">Salary Per Minute</th>
                <th className="px-4 py-3.5 font-bold text-right text-slate-950 border-r border-emerald-200/50">Excess of Shortfall Salary</th>
                <th className="px-4 py-3.5 font-black text-right bg-[#8ec984] text-slate-950 border-r border-emerald-300">Actual Payable Salary</th>
                <th className="px-4 py-3.5 font-bold text-center text-slate-950">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800 bg-white dark:bg-slate-900 font-medium text-slate-800 dark:text-slate-200">
              {loading ? (
                <tr>
                  <td colSpan={9} className="px-4 py-10 text-center text-slate-400">
                    <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-emerald-600" />
                    Loading payslip calculations...
                  </td>
                </tr>
              ) : filteredPayslips.length === 0 ? (
                <tr>
                  <td colSpan={9} className="px-4 py-12 text-center text-slate-400">
                    <FileText className="w-8 h-8 mx-auto mb-2 opacity-40" />
                    No salary records found for selected period.
                  </td>
                </tr>
              ) : (
                filteredPayslips.map((p) => {
                  const agreedSalary = parseFloat(p.agreedMonthlySalary || 0);
                  const normalMinutes = p.normalWorkingMinutes || 13200;
                  const actualMinutes = p.actualWorkingMinutes ?? (Math.round((parseFloat(p.totalWorkingHours) || 0) * 60));
                  const diffMinutes = p.differenceMinutes ?? (actualMinutes - normalMinutes);
                  const ratePerMin = p.salaryPerMinute ?? (normalMinutes > 0 ? agreedSalary / normalMinutes : 0);
                  const excessShortfall = p.excessOrShortfallSalary ?? (diffMinutes * ratePerMin);
                  const payableSalary = p.actualPayableSalary ?? Math.max(0, agreedSalary + excessShortfall);

                  return (
                    <tr
                      key={p.id}
                      className="hover:bg-emerald-50/40 dark:hover:bg-slate-800/40 transition-colors"
                    >
                      <td className="px-4 py-3 font-bold text-slate-900 dark:text-white border-r border-slate-100 dark:border-slate-800">
                        {p.employeeName}
                        <span className="block text-[11px] font-normal text-slate-400">
                          {monthNames[p.salaryMonth]} {p.salaryYear} &bull; {p.payslipNumber}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-right font-semibold border-r border-slate-100 dark:border-slate-800">
                        {agreedSalary.toFixed(0)}
                      </td>
                      <td className="px-4 py-3 text-right font-mono border-r border-slate-100 dark:border-slate-800">
                        {actualMinutes}
                      </td>
                      <td className="px-4 py-3 text-right font-mono border-r border-slate-100 dark:border-slate-800">
                        {normalMinutes}
                      </td>
                      <td className={`px-4 py-3 text-right font-mono font-bold border-r border-slate-100 dark:border-slate-800 ${
                        diffMinutes >= 0 ? 'text-emerald-600' : 'text-rose-600'
                      }`}>
                        {diffMinutes > 0 ? `+${diffMinutes}` : diffMinutes}
                      </td>
                      <td className="px-4 py-3 text-right font-mono border-r border-slate-100 dark:border-slate-800">
                        {Number(ratePerMin).toFixed(4)}
                      </td>
                      <td className={`px-4 py-3 text-right font-mono font-bold border-r border-slate-100 dark:border-slate-800 ${
                        excessShortfall >= 0 ? 'text-emerald-600' : 'text-rose-600'
                      }`}>
                        {Number(excessShortfall).toFixed(2)}
                      </td>
                      <td className="px-4 py-3 text-right font-mono font-black text-emerald-700 dark:text-emerald-400 bg-emerald-50/70 dark:bg-emerald-950/20 border-r border-slate-100 dark:border-slate-800">
                        {Number(payableSalary).toFixed(2)}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <div className="flex items-center justify-center gap-1.5">
                          <button
                            onClick={() => setSelectedPayslip({
                              ...p,
                              agreedSalary,
                              actualMinutes,
                              normalMinutes,
                              diffMinutes,
                              ratePerMin,
                              excessShortfall,
                              payableSalary
                            })}
                            className="inline-flex items-center gap-1.5 px-3 py-1 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-700 text-white transition-colors shadow-xs"
                          >
                            <Eye className="w-3.5 h-3.5" />
                            View
                          </button>
                          <button
                            onClick={() => exportIndividualPayslipPdf({
                              ...p,
                              agreedMonthlySalary: agreedSalary,
                              actualWorkingMinutes: actualMinutes,
                              normalWorkingMinutes: normalMinutes,
                              differenceMinutes: diffMinutes,
                              salaryPerMinute: ratePerMin,
                              excessOrShortfallSalary: excessShortfall,
                              netPayableSalary: payableSalary
                            })}
                            title="Download Official PDF"
                            className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-semibold bg-indigo-600 hover:bg-indigo-700 text-white transition-colors shadow-xs"
                          >
                            <Download className="w-3.5 h-3.5" /> PDF
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
      </div>

      {/* Individual Professional Payslip Modal */}
      <AnimatePresence>
        {selectedPayslip && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-xs overflow-y-auto">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl max-w-2xl w-full shadow-2xl overflow-hidden my-8"
            >
              {/* Header Bar */}
              <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/50 print:hidden">
                <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">
                  Payslip: {selectedPayslip.payslipNumber}
                </span>
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => exportIndividualPayslipPdf(selectedPayslip)}
                    className="flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-semibold rounded-lg bg-indigo-600 hover:bg-indigo-700 text-white shadow-xs transition-colors"
                  >
                    <Download className="w-3.5 h-3.5" />
                    Download PDF
                  </button>
                  <button
                    onClick={handlePrint}
                    className="flex items-center gap-1.5 px-3.5 py-1.5 text-xs font-semibold rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs transition-colors"
                  >
                    <Printer className="w-3.5 h-3.5" />
                    Print
                  </button>
                  <button
                    onClick={() => setSelectedPayslip(null)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200"
                  >
                    <X className="w-5 h-5" />
                  </button>
                </div>
              </div>

              {/* Printable Body */}
              <div className="p-8 space-y-6 text-slate-800 dark:text-slate-200 bg-white dark:bg-slate-900 printable-slip">
                {/* 1. Company Name & Logo, 2. Address, 3. Month & Year, 4. Payslip Number */}
                <div className="flex items-start justify-between border-b pb-6 border-slate-200 dark:border-slate-700">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <div className="w-10 h-10 rounded-xl bg-emerald-600 flex items-center justify-center text-white font-black text-xl shadow-xs">
                        EMS
                      </div>
                      <h2 className="text-xl font-black tracking-tight text-slate-900 dark:text-white">
                        Smart Employee Management Corp.
                      </h2>
                    </div>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                      100 Tech Park Avenue, Innovation Hub, Tech City - 700091
                    </p>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                      support@smartemployee.com | +91 (033) 2345-6789
                    </p>
                  </div>
                  <div className="text-right space-y-1">
                    <span className="inline-block px-3 py-1 rounded-md bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300 font-bold text-xs">
                      FINAL SALARY SLIP
                    </span>
                    <p className="font-mono text-xs text-slate-500 dark:text-slate-400">
                      Payslip No: <span className="font-bold text-slate-700 dark:text-slate-300">{selectedPayslip.payslipNumber}</span>
                    </p>
                    <p className="text-xs font-semibold text-slate-700 dark:text-slate-300">
                      Month & Year: {monthNames[selectedPayslip.salaryMonth]} {selectedPayslip.salaryYear}
                    </p>
                  </div>
                </div>

                {/* 5. Employee Name, 6. Employee ID, 7. Designation, 8. Department */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-4 rounded-xl bg-slate-50 dark:bg-slate-800/40 text-xs">
                  <div>
                    <span className="text-slate-400 block font-medium">Employee Name</span>
                    <span className="font-bold text-slate-900 dark:text-white text-sm">
                      {selectedPayslip.employeeName}
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-400 block font-medium">Employee ID</span>
                    <span className="font-bold text-slate-900 dark:text-white text-sm">
                      {selectedPayslip.employeeCode}
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-400 block font-medium">Designation</span>
                    <span className="font-semibold text-slate-800 dark:text-slate-200">
                      {selectedPayslip.designationTitle || 'Staff Member'}
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-400 block font-medium">Department</span>
                    <span className="font-semibold text-slate-800 dark:text-slate-200">
                      {selectedPayslip.departmentName || 'General Staff'}
                    </span>
                  </div>
                </div>

                {/* Exact Minute-Based Working & Salary Adjustment Breakdown */}
                <div className="border border-slate-200 dark:border-slate-700 rounded-xl overflow-hidden text-xs">
                  <div className="bg-slate-100 dark:bg-slate-800 px-4 py-2.5 font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300 border-b border-slate-200 dark:border-slate-700">
                    Minute-Based Salary Calculation & Breakdown
                  </div>
                  <div className="divide-y divide-slate-100 dark:divide-slate-800">
                    <div className="flex justify-between px-4 py-2.5">
                      <span className="text-slate-600 dark:text-slate-400">9. Agreed Monthly Salary</span>
                      <span className="font-bold font-mono text-slate-900 dark:text-white">
                        ₹{parseFloat(selectedPayslip.agreedSalary || selectedPayslip.agreedMonthlySalary || 0).toFixed(2)}
                      </span>
                    </div>

                    <div className="flex justify-between px-4 py-2.5 bg-slate-50/50 dark:bg-slate-800/30">
                      <span className="text-slate-600 dark:text-slate-400">10. Actual Working Time</span>
                      <span className="font-mono font-bold text-slate-900 dark:text-white">
                        {selectedPayslip.actualMinutes ?? (selectedPayslip.actualWorkingMinutes || Math.round((parseFloat(selectedPayslip.totalWorkingHours) || 0) * 60))} Minutes
                      </span>
                    </div>

                    <div className="flex justify-between px-4 py-2.5">
                      <span className="text-slate-600 dark:text-slate-400">11. Normal Working Time</span>
                      <span className="font-mono font-bold text-slate-900 dark:text-white">
                        {selectedPayslip.normalMinutes ?? (selectedPayslip.normalWorkingMinutes || 13200)} Minutes (220 hrs)
                      </span>
                    </div>

                    <div className="flex justify-between px-4 py-2.5 bg-slate-50/50 dark:bg-slate-800/30">
                      <span className="text-slate-600 dark:text-slate-400">12. Difference in Working Minutes</span>
                      <span className={`font-mono font-bold ${
                        (selectedPayslip.diffMinutes ?? selectedPayslip.differenceMinutes ?? 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'
                      }`}>
                        {(selectedPayslip.diffMinutes ?? selectedPayslip.differenceMinutes ?? 0) > 0 ? '+' : ''}
                        {selectedPayslip.diffMinutes ?? selectedPayslip.differenceMinutes ?? 0} Minutes
                      </span>
                    </div>

                    <div className="flex justify-between px-4 py-2.5">
                      <span className="text-slate-600 dark:text-slate-400">13. Salary Per Minute (Agreed / Normal Minutes)</span>
                      <span className="font-mono font-bold text-slate-900 dark:text-white">
                        ₹{Number(selectedPayslip.ratePerMin ?? selectedPayslip.salaryPerMinute ?? 0).toFixed(4)} / min
                      </span>
                    </div>

                    <div className="flex justify-between px-4 py-2.5 bg-slate-50/50 dark:bg-slate-800/30">
                      <span className="text-slate-600 dark:text-slate-400">14. Excess Working Salary</span>
                      <span className="font-mono font-bold text-emerald-600">
                        ₹{(selectedPayslip.diffMinutes ?? selectedPayslip.differenceMinutes ?? 0) > 0
                          ? Number(selectedPayslip.excessShortfall ?? selectedPayslip.excessOrShortfallSalary ?? 0).toFixed(2)
                          : '0.00'}
                      </span>
                    </div>

                    <div className="flex justify-between px-4 py-2.5">
                      <span className="text-slate-600 dark:text-slate-400">15. Shortfall Salary</span>
                      <span className="font-mono font-bold text-rose-600">
                        ₹{(selectedPayslip.diffMinutes ?? selectedPayslip.differenceMinutes ?? 0) < 0
                          ? Math.abs(Number(selectedPayslip.excessShortfall ?? selectedPayslip.excessOrShortfallSalary ?? 0)).toFixed(2)
                          : '0.00'}
                      </span>
                    </div>

                    <div className="flex justify-between px-4 py-2.5 bg-slate-50/50 dark:bg-slate-800/30">
                      <span className="text-slate-600 dark:text-slate-400">16. Total Salary Adjustment</span>
                      <span className={`font-mono font-bold ${
                        Number(selectedPayslip.excessShortfall ?? selectedPayslip.excessOrShortfallSalary ?? 0) >= 0 ? 'text-emerald-600' : 'text-rose-600'
                      }`}>
                        {Number(selectedPayslip.excessShortfall ?? selectedPayslip.excessOrShortfallSalary ?? 0) >= 0 ? '+' : ''}
                        ₹{Number(selectedPayslip.excessShortfall ?? selectedPayslip.excessOrShortfallSalary ?? 0).toFixed(2)}
                      </span>
                    </div>
                  </div>
                </div>

                {/* 17. Actual Payable Salary Box */}
                <div className="p-4 rounded-xl bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-300 dark:border-emerald-700 flex items-center justify-between">
                  <div>
                    <span className="text-xs uppercase font-extrabold text-emerald-800 dark:text-emerald-300">
                      17. Actual Payable Salary
                    </span>
                    <p className="text-xs text-slate-500 dark:text-slate-400">
                      Agreed Salary + Total Salary Adjustment (Rounded to 2 decimals)
                    </p>
                  </div>
                  <div className="text-2xl font-black text-emerald-700 dark:text-emerald-400 font-mono">
                    ₹{Number(selectedPayslip.payableSalary ?? selectedPayslip.actualPayableSalary ?? selectedPayslip.netPayableSalary ?? 0).toFixed(2)}
                  </div>
                </div>

                {/* 18. Payment Status & Payment Date */}
                <div className="grid grid-cols-2 gap-4 p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 text-xs">
                  <div>
                    <span className="text-slate-400 block font-medium">Payment Status</span>
                    <span className="inline-block mt-1 px-2.5 py-0.5 rounded-full font-bold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/60 dark:text-emerald-300">
                      {selectedPayslip.status || 'PAID'}
                    </span>
                  </div>
                  <div>
                    <span className="text-slate-400 block font-medium">Payment Date</span>
                    <span className="font-bold text-slate-800 dark:text-slate-200 block mt-1">
                      {selectedPayslip.paymentDate || new Date().toISOString().split('T')[0]}
                    </span>
                  </div>
                </div>

                {/* Sign-off footer */}
                <div className="pt-4 border-t border-slate-200 dark:border-slate-800 flex justify-between items-end text-[11px] text-slate-400">
                  <div>
                    <p>Computer-generated document. No physical signature required.</p>
                    <p>Generated by Smart Employee Management Engine</p>
                  </div>
                  <div className="text-right">
                    <p className="font-semibold text-slate-700 dark:text-slate-300">Authorized Signatory</p>
                    <p>HR & Accounts Directorate</p>
                  </div>
                </div>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
