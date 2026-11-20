import React, { useState, useEffect } from 'react';
import { adminApi, downloadAuthenticatedFile } from '../../services/api';
import { exportTableToPdf, exportIndividualPayslipPdf, formatINR } from '../../utils/pdfGenerator';
import {
  FileText,
  DollarSign,
  Calendar,
  Printer,
  Download,
  Plus,
  Eye,
  CheckCircle2,
  AlertCircle,
  Building,
  User,
  CreditCard,
  Search,
  RefreshCw,
  Clock,
  ArrowRight,
  TrendingUp,
  TrendingDown,
  X,
  FileSpreadsheet,
  ChevronLeft,
  ChevronRight
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function PayslipManagement() {
  const currentDate = new Date();
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [month, setMonth] = useState(currentDate.getMonth() + 1);
  const [year, setYear] = useState(currentDate.getFullYear());
  const [search, setSearch] = useState('');

  const [salaryRows, setSalaryRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [viewPayslip, setViewPayslip] = useState(null);
  const [generatingId, setGeneratingId] = useState(null);
  const [feedback, setFeedback] = useState(null);
  const [showDownloadMenu, setShowDownloadMenu] = useState(false);
  const [companySettings, setCompanySettings] = useState(null);

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

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

  const fetchSalaryCalculations = async () => {
    setLoading(true);
    setFeedback(null);
    try {
      const [res, setRes] = await Promise.all([
        adminApi.getSalaryCalculations(month, year),
        adminApi.getSettings()
      ]);
      if (res.success && res.data) {
        setSalaryRows(res.data);
      }
      if (setRes.success && setRes.data) {
        setCompanySettings(setRes.data);
      }
    } catch (err) {
      console.error("Failed to load salary calculations:", err);
      setFeedback({ type: 'error', text: err.message || 'Failed to load salary calculations' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSalaryCalculations();
  }, [month, year]);

  const handleGenerateOrView = async (row) => {
    setGeneratingId(row.employeeId);
    try {
      const res = await adminApi.generatePayslip({
        employeeId: row.employeeId,
        month: Number(month),
        year: Number(year),
        bonusOrIncentive: 0
      });
      if (res.success && res.data) {
        setViewPayslip(res.data);
        fetchSalaryCalculations();
      }
    } catch (err) {
      alert("Error generating payslip: " + err.message);
    } finally {
      setGeneratingId(null);
    }
  };

  const handlePrint = () => {
    window.print();
  };

  const handleDownloadBulkCsv = () => {
    const url = adminApi.exportPayslipsCsvUrl(month, year);
    downloadAuthenticatedFile(url, `Payslips_${year}_${month}.csv`);
    setShowDownloadMenu(false);
  };

  const handleDownloadBulkPdf = () => {
    const headers = [
      'Employee Name',
      'Agreed Salary',
      'Actual Time (Min)',
      'Normal Time (Min)',
      'Difference (Min)',
      'Salary / Min',
      'Excess / Shortfall',
      'Actual Payable Salary',
    ];

    const rows = filteredRows.map(r => [
      r.employeeName,
      formatINR(r.agreedSalary),
      `${r.actualWorkingMinutes} mins`,
      `${r.normalWorkingMinutes} mins`,
      `${r.differenceMinutes > 0 ? '+' : ''}${r.differenceMinutes} mins`,
      `INR ${Number(r.salaryPerMinute).toFixed(4)}`,
      `${Number(r.excessOrShortfallSalary) >= 0 ? '+' : ''}${formatINR(r.excessOrShortfallSalary)}`,
      formatINR(r.actualPayableSalary),
    ]);

    exportTableToPdf({
      title: 'Company Employee Salary Calculation & Payroll Register',
      subtitle: `Period: ${monthNames[month - 1]} ${year} • Currency: Indian Rupees (INR, ₹)`,
      dateInfo: `${monthNames[month - 1]} ${year}`,
      headers,
      rows,
      filename: `Payslips_Register_${year}_${month}.pdf`,
      orientation: 'landscape',
    });
    setShowDownloadMenu(false);
  };

  const filteredRows = salaryRows.filter(r =>
    !search ||
    r.employeeName?.toLowerCase().includes(search.toLowerCase()) ||
    r.employeeCode?.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <DollarSign className="w-6 h-6 text-emerald-500" /> Employee Salary Calculation & Payslip Engine
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            Automated minute-based salary formula in <span className="font-bold text-emerald-600">Indian Rupees (₹)</span> with variance and individual payslip vouchers.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {/* Download All Records Button */}
          <div className="relative">
            <button
              onClick={() => setShowDownloadMenu(!showDownloadMenu)}
              className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-750 transition-colors shadow-xs flex items-center gap-1.5"
            >
              <Download className="w-3.5 h-3.5 text-primary-500" /> Download All Employee Records
            </button>

            {showDownloadMenu && (
              <div className="absolute right-0 mt-2 w-64 rounded-2xl bg-white dark:bg-slate-900 shadow-xl border border-slate-200 dark:border-slate-800 p-2 z-30 space-y-1">
                <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400 px-3 py-1">
                  Bulk Period Export ({monthNames[month - 1]} {year})
                </div>
                <button
                  onClick={handleDownloadBulkCsv}
                  className="w-full text-left px-3 py-2 text-xs font-medium rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 flex items-center gap-2 text-slate-700 dark:text-slate-200"
                >
                  <FileSpreadsheet className="w-4 h-4 text-emerald-500" /> Export All Records (Excel / CSV)
                </button>
                <button
                  onClick={handleDownloadBulkPdf}
                  className="w-full text-left px-3 py-2 text-xs font-medium rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800 flex items-center gap-2 text-slate-700 dark:text-slate-200"
                >
                  <FileText className="w-4 h-4 text-rose-500" /> Export All Records (PDF)
                </button>
              </div>
            )}
          </div>

          <button
            onClick={fetchSalaryCalculations}
            className="px-3 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 transition-colors shadow-xs flex items-center gap-1.5"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </button>
        </div>
      </div>

      {feedback && (
        <div className={`p-3.5 rounded-2xl text-xs flex items-center justify-between ${
          feedback.type === 'success'
            ? 'bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 border border-emerald-200'
            : 'bg-rose-50 dark:bg-rose-950/60 text-rose-700 border border-rose-200'
        }`}>
          <span>{feedback.text}</span>
          <button onClick={() => setFeedback(null)} className="text-slate-400">&times;</button>
        </div>
      )}

      {/* Payslip Period Navigation & Search Bar */}
      <div className="glass-card p-4 flex flex-col md:flex-row items-center justify-between gap-3">
        {/* Interactive Month & Year Navigation */}
        <div className="flex flex-wrap items-center gap-2 w-full md:w-auto">
          <button
            onClick={handlePrevMonth}
            title="Previous Month"
            className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-slate-50 text-slate-700 dark:text-slate-200"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>

          <div className="px-4 py-2 rounded-xl bg-primary-50 dark:bg-primary-950/60 border border-primary-200 dark:border-primary-800 text-primary-800 dark:text-primary-200 font-bold text-xs flex items-center gap-2">
            <Calendar className="w-4 h-4 text-primary-600" />
            <span>{monthNames[month - 1]} {year}</span>
          </div>

          <button
            onClick={handleNextMonth}
            title="Next Month"
            className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 hover:bg-slate-50 text-slate-700 dark:text-slate-200"
          >
            <ChevronRight className="w-4 h-4" />
          </button>

          {/* Date Picker input to select any date */}
          <div className="flex items-center gap-1.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 px-3 py-1.5 rounded-xl text-xs ml-1">
            <span className="text-[11px] text-slate-400 font-medium">Period Calendar:</span>
            <input
              type="date"
              value={selectedDate}
              onChange={handleCalendarDateChange}
              className="bg-transparent font-medium text-slate-800 dark:text-slate-100 focus:outline-none cursor-pointer"
            />
          </div>
        </div>

        {/* Employee Search */}
        <div className="relative w-full md:w-80">
          <Search className="w-3.5 h-3.5 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search employee name or code..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full pl-9 pr-4 py-2 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/50 text-slate-900 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
          />
        </div>
      </div>

      {/* Formula Info Banner */}
      <div className="p-3.5 rounded-2xl bg-emerald-50/80 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800/60 text-xs text-emerald-900 dark:text-emerald-200 flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <CheckCircle2 className="w-4 h-4 text-emerald-600 dark:text-emerald-400 flex-shrink-0" />
          <span><strong>Calculation Logic:</strong> Salary Per Minute = Agreed Salary &divide; Normal Time &bull; Difference = Actual - Normal &bull; Excess/Shortfall = Diff &times; Rate &bull; Actual Payable = Agreed + Excess/Shortfall</span>
        </div>
        <span className="font-mono text-[11px] bg-emerald-100 dark:bg-emerald-900 px-2.5 py-1 rounded-md font-bold text-emerald-800 dark:text-emerald-200">
          Currency: {companySettings?.currencyCode || 'INR'} ({companySettings?.currencySymbol || '₹'}) &bull; Overtime: {companySettings?.currencySymbol || '₹'}{Number(companySettings?.overtimeRatePerHour ?? 200).toFixed(2)}/hr &bull; Std: {companySettings?.standardWorkingDays || 26}d &times; {companySettings?.fullDayWorkingHours || 8}h
        </span>
      </div>

      {/* Employee Salary Calculation Table (Exact columns matching requirement) */}
      <div className="glass-card overflow-hidden border border-slate-200 dark:border-slate-800 shadow-md">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead className="text-slate-950 text-[12px] font-bold border-b border-emerald-300">
              <tr className="bg-[#d8e8d2] text-slate-950">
                <th scope="col" className="px-4 py-3.5 font-bold text-slate-950 border-r border-emerald-200/50">Employee Name</th>
                <th scope="col" className="px-4 py-3.5 font-bold text-right text-slate-950 border-r border-emerald-200/50">Agreed Salary</th>
                <th scope="col" className="px-4 py-3.5 font-bold text-center text-slate-950 border-r border-emerald-200/50">Actual Working time (Minute)</th>
                <th scope="col" className="px-4 py-3.5 font-bold text-center text-slate-950 border-r border-emerald-200/50">Normal Working Time</th>
                <th scope="col" className="px-4 py-3.5 font-bold text-center text-slate-950 border-r border-emerald-200/50">Difference (Minute)</th>
                <th scope="col" className="px-4 py-3.5 font-bold text-right text-slate-950 border-r border-emerald-200/50">Salary Per Minute</th>
                <th scope="col" className="px-4 py-3.5 font-bold text-right text-slate-950 border-r border-emerald-200/50">Excess of Shortfall Salary</th>
                <th scope="col" className="px-4 py-3.5 font-black text-right bg-[#8ec984] text-slate-950 border-r border-emerald-300">Actual Payable Salary</th>
                <th scope="col" className="px-4 py-3.5 font-bold text-center text-slate-950">Action</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {loading ? (
                <tr>
                  <td colSpan="9" className="px-4 py-12 text-center text-slate-400">
                    <div className="w-6 h-6 border-2 border-emerald-500/20 border-t-emerald-600 rounded-full animate-spin mx-auto mb-2" />
                    Calculating employee salary data in INR (₹)...
                  </td>
                </tr>
              ) : filteredRows.length === 0 ? (
                <tr>
                  <td colSpan="9" className="px-4 py-12 text-center text-slate-400">
                    No employee records found matching your filter.
                  </td>
                </tr>
              ) : (
                filteredRows.map((row) => {
                  const isNegative = row.differenceMinutes < 0;
                  const isPositive = row.differenceMinutes > 0;

                  return (
                    <tr
                      key={row.employeeId}
                      className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors"
                    >
                      {/* Employee Name */}
                      <td className="px-4 py-3.5 whitespace-nowrap">
                        <div className="font-bold text-slate-900 dark:text-slate-100">{row.employeeName}</div>
                        <div className="text-[10px] text-slate-400 font-mono">{row.employeeCode}</div>
                      </td>

                      {/* Agreed Salary in INR */}
                      <td className="px-4 py-3.5 whitespace-nowrap text-right font-mono font-semibold text-slate-800 dark:text-slate-200">
                        {formatINR(row.agreedSalary)}
                      </td>

                      {/* Actual Working Time (Minute) */}
                      <td className="px-4 py-3.5 whitespace-nowrap text-center font-mono font-bold text-slate-800 dark:text-slate-200">
                        {row.actualWorkingMinutes.toLocaleString()}
                      </td>

                      {/* Normal Working Time (Minute) */}
                      <td className="px-4 py-3.5 whitespace-nowrap text-center font-mono text-slate-600 dark:text-slate-300">
                        {row.normalWorkingMinutes.toLocaleString()}
                      </td>

                      {/* Difference (Minute) */}
                      <td className={`px-4 py-3.5 whitespace-nowrap text-center font-mono font-bold ${
                        isNegative ? 'text-rose-600 dark:text-rose-400' : isPositive ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-600'
                      }`}>
                        {row.differenceMinutes > 0 ? `+${row.differenceMinutes}` : row.differenceMinutes}
                      </td>

                      {/* Salary Per Minute */}
                      <td className="px-4 py-3.5 whitespace-nowrap text-right font-mono text-slate-700 dark:text-slate-300">
                        ₹{Number(row.salaryPerMinute).toFixed(4)}
                      </td>

                      {/* Excess of Shortfall Salary */}
                      <td className={`px-4 py-3.5 whitespace-nowrap text-right font-mono font-bold ${
                        isNegative ? 'text-rose-600 dark:text-rose-400' : isPositive ? 'text-emerald-600 dark:text-emerald-400' : 'text-slate-600'
                      }`}>
                        {Number(row.excessOrShortfallSalary) >= 0 ? `+${formatINR(row.excessOrShortfallSalary)}` : formatINR(row.excessOrShortfallSalary)}
                      </td>

                      {/* Actual Payable Salary */}
                      <td className="px-4 py-3.5 whitespace-nowrap text-right font-mono font-bold text-emerald-700 dark:text-emerald-400 bg-emerald-50/50 dark:bg-emerald-950/20 text-sm">
                        {formatINR(row.actualPayableSalary)}
                      </td>

                      {/* Action: Generate Payslip */}
                      <td className="px-4 py-3.5 whitespace-nowrap text-center">
                        <button
                          onClick={() => handleGenerateOrView(row)}
                          disabled={generatingId === row.employeeId}
                          className="px-3 py-1.5 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-xs transition-all flex items-center gap-1 mx-auto disabled:opacity-50"
                        >
                          {generatingId === row.employeeId ? (
                            <span className="animate-spin w-3 h-3 border border-white border-t-transparent rounded-full" />
                          ) : (
                            <FileText className="w-3.5 h-3.5" />
                          )}
                          Generate Payslip
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Official Formatted Payslip View Modal */}
      {viewPayslip && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/75 backdrop-blur-sm overflow-y-auto">
          <motion.div
            initial={{ scale: 0.95, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            className="w-full max-w-2xl bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden my-6"
          >
            {/* Modal Controls (Hidden in Print) */}
            <div className="no-print flex items-center justify-between px-6 py-3.5 border-b border-slate-100 dark:border-slate-800 bg-slate-50 dark:bg-slate-850">
              <span className="text-xs font-bold text-slate-600 dark:text-slate-300 uppercase tracking-wider flex items-center gap-1.5">
                <FileText className="w-4 h-4 text-primary-500" />
                Employee Individual Payslip
              </span>

              <div className="flex items-center gap-2">
                <button
                  onClick={() => exportIndividualPayslipPdf(viewPayslip, companySettings)}
                  className="px-3 py-1.5 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-xs flex items-center gap-1.5 transition-colors"
                >
                  <Download className="w-3.5 h-3.5" /> Download PDF
                </button>
                <button
                  onClick={handlePrint}
                  className="px-3 py-1.5 text-xs font-semibold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs flex items-center gap-1.5 transition-colors"
                >
                  <Printer className="w-3.5 h-3.5" /> Print
                </button>
                <button
                  onClick={() => setViewPayslip(null)}
                  className="p-1.5 rounded-xl text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Printable Payslip Body */}
            <div id="printable-payslip" className="p-8 space-y-6 text-slate-800 dark:text-slate-100 bg-white dark:bg-slate-900">
              {/* 1. Company Name, Logo & Address */}
              <div className="flex items-start justify-between border-b-2 border-slate-800 dark:border-slate-200 pb-4">
                <div>
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 rounded-lg bg-emerald-600 text-white flex items-center justify-center font-bold text-sm shadow-sm">
                      AG
                    </div>
                    <h2 className="text-xl font-black tracking-tight text-slate-900 dark:text-white">
                      Apex Global Technologies Inc.
                    </h2>
                  </div>
                  <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                    100 Tech Park Boulevard, Suite 500, Silicon City, CA 94025
                  </p>
                  <p className="text-xs text-slate-500 dark:text-slate-400">
                    Phone: +1 (800) 555-0199 &bull; Email: payroll@apextech.com
                  </p>
                </div>

                <div className="text-right">
                  <div className="px-3 py-1 rounded-lg bg-slate-100 dark:bg-slate-800 font-mono text-xs font-bold text-primary-600 dark:text-primary-400 inline-block">
                    {viewPayslip.payslipNumber || `PAY-${year}-${month}-${viewPayslip.employeeCode}`}
                  </div>
                  <div className="text-xs font-semibold text-slate-700 dark:text-slate-300 mt-1">
                    Month: {viewPayslip.monthName || monthNames[month - 1]} {viewPayslip.salaryYear || year}
                  </div>
                </div>
              </div>

              {/* 2. Employee Details: Name, ID, Designation, Department */}
              <div className="grid grid-cols-2 gap-4 text-xs p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200/80 dark:border-slate-700">
                <div className="space-y-1.5">
                  <div><span className="text-slate-500">Employee Name:</span> <strong className="ml-1 text-slate-900 dark:text-white">{viewPayslip.employeeName}</strong></div>
                  <div><span className="text-slate-500">Employee ID:</span> <span className="ml-1 font-mono font-bold text-primary-600">{viewPayslip.employeeCode}</span></div>
                  <div><span className="text-slate-500">Designation:</span> <span className="ml-1">{viewPayslip.designationTitle || 'Software Engineer'}</span></div>
                  <div><span className="text-slate-500">Department:</span> <span className="ml-1">{viewPayslip.departmentName || 'Engineering & Technology'}</span></div>
                </div>

                <div className="space-y-1.5">
                  <div><span className="text-slate-500">Payment Status:</span> <span className="ml-1 font-bold text-emerald-600 uppercase">{viewPayslip.status || 'PAID'}</span></div>
                  <div><span className="text-slate-500">Payment Date:</span> <span className="ml-1 font-mono">{viewPayslip.paymentDate || new Date().toISOString().split('T')[0]}</span></div>
                  <div><span className="text-slate-500">Currency:</span> <span className="ml-1 font-bold text-indigo-600">INR (₹)</span></div>
                  <div><span className="text-slate-500">Payment Mode:</span> <span className="ml-1 font-mono">Bank Direct Credit</span></div>
                </div>
              </div>

              {/* 3. Automatic Minute-Based Working Time & Salary Calculation Table */}
              <div className="border border-slate-200 dark:border-slate-700 rounded-2xl overflow-hidden">
                <div className="bg-slate-100 dark:bg-slate-800 px-4 py-2 font-bold text-xs uppercase tracking-wider text-slate-700 dark:text-slate-200 border-b border-slate-200 dark:border-slate-700">
                  Minute-Based Working Time & Salary Breakdown
                </div>
                <div className="p-4 space-y-2.5 text-xs">
                  <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                    <span className="text-slate-600 dark:text-slate-400 font-medium">Agreed Monthly Salary:</span>
                    <span className="font-mono font-bold text-slate-900 dark:text-white">
                      {formatINR(viewPayslip.agreedMonthlySalary)}
                    </span>
                  </div>

                  <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                    <span className="text-slate-600 dark:text-slate-400">Actual Working Time:</span>
                    <span className="font-mono font-semibold text-slate-900 dark:text-white">
                      {Number(viewPayslip.actualWorkingMinutes || 12000).toLocaleString()} Minutes
                    </span>
                  </div>

                  <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                    <span className="text-slate-600 dark:text-slate-400">Normal Working Time:</span>
                    <span className="font-mono text-slate-600 dark:text-slate-300">
                      {Number(viewPayslip.normalWorkingMinutes || 13200).toLocaleString()} Minutes
                    </span>
                  </div>

                  <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                    <span className="text-slate-600 dark:text-slate-400">Difference in Working Minutes:</span>
                    <span className={`font-mono font-bold ${Number(viewPayslip.differenceMinutes) < 0 ? 'text-rose-600' : 'text-emerald-600'}`}>
                      {Number(viewPayslip.differenceMinutes || -1200)} Minutes
                    </span>
                  </div>

                  <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                    <span className="text-slate-600 dark:text-slate-400">Salary Per Minute:</span>
                    <span className="font-mono font-semibold text-slate-900 dark:text-white">
                      ₹{Number(viewPayslip.salaryPerMinute || 1.1364).toFixed(4)}
                    </span>
                  </div>

                  <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                    <span className="text-slate-600 dark:text-slate-400">Excess Working Salary:</span>
                    <span className="font-mono text-emerald-600 font-semibold">
                      +{formatINR(viewPayslip.excessWorkingSalary || 0)}
                    </span>
                  </div>

                  <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                    <span className="text-slate-600 dark:text-slate-400">Shortfall Salary:</span>
                    <span className="font-mono text-rose-600 font-semibold">
                      -{formatINR(viewPayslip.shortfallSalary || 1363.64)}
                    </span>
                  </div>

                  <div className="flex justify-between py-1 border-b border-slate-100 dark:border-slate-800">
                    <span className="text-slate-600 dark:text-slate-400">Total Salary Adjustment:</span>
                    <span className={`font-mono font-bold ${Number(viewPayslip.totalSalaryAdjustment) < 0 ? 'text-rose-600' : 'text-emerald-600'}`}>
                      {Number(viewPayslip.totalSalaryAdjustment) >= 0 ? `+${formatINR(viewPayslip.totalSalaryAdjustment)}` : `-${formatINR(Math.abs(Number(viewPayslip.totalSalaryAdjustment || -1363.64)))}`}
                    </span>
                  </div>
                </div>
              </div>

              {/* 4. Actual Payable Salary Box */}
              <div className="p-4 rounded-2xl bg-emerald-50 dark:bg-emerald-950/60 border border-emerald-300 dark:border-emerald-700 flex items-center justify-between">
                <div>
                  <span className="text-xs font-bold uppercase tracking-wider text-emerald-800 dark:text-emerald-300">
                    Actual Payable Salary
                  </span>
                  <p className="text-[11px] text-emerald-600 dark:text-emerald-400">
                    Agreed Monthly Salary + Excess / Shortfall Adjustment
                  </p>
                </div>
                <div className="text-2xl font-black font-mono text-emerald-700 dark:text-emerald-300">
                  {formatINR(viewPayslip.netPayableSalary || viewPayslip.actualPayableSalary || 13636.36)}
                </div>
              </div>

              {/* Authorized Signatory Footer */}
              <div className="pt-6 border-t border-slate-200 dark:border-slate-800 flex justify-between items-end text-[11px] text-slate-400">
                <div>
                  <p>Computer-generated salary voucher created via Apex Biometric Payroll Engine.</p>
                  <p>Calculated in Indian Rupees (INR, ₹).</p>
                </div>
                <div className="text-right">
                  <div className="font-semibold text-slate-700 dark:text-slate-300">Finance & Payroll Officer</div>
                  <div>Apex Global Technologies Inc.</div>
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  );
}
