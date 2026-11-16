import jsPDF from 'jspdf';
import 'jspdf-autotable';

// Currency Formatter for Indian Rupees (₹)
export const formatINR = (amount) => {
  if (amount === undefined || amount === null) return '₹0.00';
  const num = Number(amount);
  return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
};

/**
 * Generate standard table PDF (for Attendance Date-wise or Monthly)
 */
export const exportTableToPdf = ({
  title,
  subtitle,
  dateInfo,
  headers,
  rows,
  filename = 'attendance-report.pdf',
  orientation = 'portrait',
}) => {
  const doc = new jsPDF({
    orientation: orientation,
    unit: 'pt',
    format: 'a4',
  });

  // Header Banner
  doc.setFillColor(30, 41, 59); // Slate-800
  doc.rect(0, 0, doc.internal.pageSize.getWidth(), 65, 'F');

  // Title
  doc.setTextColor(255, 255, 255);
  doc.setFontSize(16);
  doc.setFont('helvetica', 'bold');
  doc.text(title || 'Apex Global Technologies - Report', 40, 32);

  // Subtitle / Date
  doc.setFontSize(9);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(203, 213, 225); // Slate-300
  const sub = subtitle ? `${subtitle} • Generated: ${new Date().toLocaleString()}` : `Generated: ${new Date().toLocaleString()}`;
  doc.text(sub, 40, 48);

  if (dateInfo) {
    doc.text(dateInfo, doc.internal.pageSize.getWidth() - 40, 48, { align: 'right' });
  }

  // AutoTable
  doc.autoTable({
    startY: 85,
    head: [headers],
    body: rows,
    theme: 'striped',
    headStyles: {
      fillColor: [79, 70, 229], // Primary indigo
      textColor: 255,
      fontSize: 9,
      fontStyle: 'bold',
      halign: 'left',
    },
    bodyStyles: {
      fontSize: 8.5,
      textColor: [30, 41, 59],
      cellPadding: 5,
    },
    alternateRowStyles: {
      fillColor: [248, 250, 252],
    },
    margin: { left: 35, right: 35 },
    didDrawPage: (data) => {
      // Footer page numbering
      const pageCount = doc.internal.getNumberOfPages();
      doc.setFontSize(8);
      doc.setTextColor(148, 163, 184);
      doc.text(
        `Page ${data.pageNumber} of ${pageCount} • Apex Global Technologies Employee Attendance Management`,
        doc.internal.pageSize.getWidth() / 2,
        doc.internal.pageSize.getHeight() - 20,
        { align: 'center' }
      );
    },
  });

  doc.save(filename);
};

/**
 * Generate Individual Professional Employee Payslip PDF in INR (₹)
 */
export const exportIndividualPayslipPdf = (payslip, companySetting = {}) => {
  const doc = new jsPDF({
    orientation: 'portrait',
    unit: 'pt',
    format: 'a4',
  });

  const pageWidth = doc.internal.pageSize.getWidth();
  const companyName = companySetting.companyName || 'Apex Global Technologies Inc.';
  const companyAddress = companySetting.companyAddress || '100 Tech Park Boulevard, Suite 500, Silicon City, CA';
  const companyEmail = companySetting.companyEmail || 'payroll@apextech.com';

  // 1. Top Decorative Brand Bar
  doc.setFillColor(30, 41, 59); // Slate-800
  doc.rect(0, 0, pageWidth, 90, 'F');

  // Accent Line
  doc.setFillColor(79, 70, 229); // Primary Indigo
  doc.rect(0, 86, pageWidth, 4, 'F');

  // Company Header
  doc.setTextColor(255, 255, 255);
  doc.setFontSize(18);
  doc.setFont('helvetica', 'bold');
  doc.text(companyName, 40, 36);

  doc.setFontSize(9);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(203, 213, 225);
  doc.text(companyAddress, 40, 52);
  doc.text(`Email: ${companyEmail} • Official Payslip Statement`, 40, 66);

  // Payslip Badge (Right Aligned)
  doc.setFontSize(14);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(129, 140, 248); // Indigo-300
  doc.text('SALARY PAYSLIP', pageWidth - 40, 38, { align: 'right' });

  doc.setFontSize(9);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(255, 255, 255);
  const monthName = new Date(payslip.salaryYear || 2026, (payslip.salaryMonth || 11) - 1).toLocaleString('default', { month: 'long' });
  doc.text(`Period: ${monthName} ${payslip.salaryYear || 2026}`, pageWidth - 40, 54, { align: 'right' });
  doc.text(`Slip No: ${payslip.payslipNumber || 'PAY-2026-11-EMP'}`, pageWidth - 40, 68, { align: 'right' });

  // 2. Employee Details Card
  let currentY = 115;
  doc.setFillColor(248, 250, 252);
  doc.setDrawColor(226, 232, 240);
  doc.roundedRect(40, currentY, pageWidth - 80, 85, 8, 8, 'FD');

  doc.setTextColor(30, 41, 59);
  doc.setFontSize(10);
  doc.setFont('helvetica', 'bold');
  doc.text('EMPLOYEE INFORMATION', 55, currentY + 20);

  doc.setFontSize(9);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(71, 85, 105);

  // Left Column
  doc.text(`Employee Name:`, 55, currentY + 40);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(`${payslip.employeeName || 'Biswanath Manna'}`, 150, currentY + 40);

  doc.setFont('helvetica', 'normal');
  doc.setTextColor(71, 85, 105);
  doc.text(`Employee Code:`, 55, currentY + 56);
  doc.setFont('helvetica', 'bold');
  doc.text(`${payslip.employeeCode || 'EMP007'}`, 150, currentY + 56);

  doc.setFont('helvetica', 'normal');
  doc.setTextColor(71, 85, 105);
  doc.text(`Designation:`, 55, currentY + 72);
  doc.setFont('helvetica', 'bold');
  doc.text(`${payslip.designation || 'Principal Software Engineer'}`, 150, currentY + 72);

  // Right Column
  const rightColX = 330;
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(71, 85, 105);
  doc.text(`Department:`, rightColX, currentY + 40);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(15, 23, 42);
  doc.text(`${payslip.department || 'Engineering'}`, rightColX + 90, currentY + 40);

  doc.setFont('helvetica', 'normal');
  doc.setTextColor(71, 85, 105);
  doc.text(`Payment Date:`, rightColX, currentY + 56);
  doc.setFont('helvetica', 'bold');
  doc.text(`${payslip.paymentDate || new Date().toISOString().split('T')[0]}`, rightColX + 90, currentY + 56);

  doc.setFont('helvetica', 'normal');
  doc.setTextColor(71, 85, 105);
  doc.text(`Payment Status:`, rightColX, currentY + 72);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(16, 185, 129);
  doc.text(`PAID (${payslip.status || 'FINALIZED'})`, rightColX + 90, currentY + 72);

  // 3. Exact Salary Calculation Table (Minute-based formulation)
  currentY += 105;
  doc.setTextColor(30, 41, 59);
  doc.setFontSize(10);
  doc.setFont('helvetica', 'bold');
  doc.text('ATTENDANCE & TIME-BASED SALARY CALCULATION', 40, currentY);

  const calcHeaders = [
    'Agreed Salary',
    'Actual Time (Min)',
    'Normal Time (Min)',
    'Difference (Min)',
    'Rate / Minute',
    'Excess / Shortfall',
    'Actual Payable',
  ];

  const agreed = payslip.agreedMonthlySalary ? Number(payslip.agreedMonthlySalary) : 15000;
  const actualMin = payslip.actualWorkingMinutes !== undefined ? payslip.actualWorkingMinutes : 12000;
  const normalMin = payslip.normalWorkingMinutes !== undefined ? payslip.normalWorkingMinutes : 13200;
  const diffMin = payslip.differenceMinutes !== undefined ? payslip.differenceMinutes : -1200;
  const rateMin = payslip.salaryPerMinute !== undefined ? Number(payslip.salaryPerMinute) : 1.1364;
  const excess = payslip.excessOrShortfallSalary !== undefined ? Number(payslip.excessOrShortfallSalary) : -1363.64;
  const payable = payslip.netPayableSalary !== undefined ? Number(payslip.netPayableSalary) : 13636.36;

  const calcRows = [
    [
      formatINR(agreed),
      `${actualMin} mins`,
      `${normalMin} mins`,
      `${diffMin > 0 ? '+' : ''}${diffMin} mins`,
      `INR ${rateMin.toFixed(4)}`,
      `${excess >= 0 ? '+' : ''}${formatINR(excess)}`,
      formatINR(payable),
    ],
  ];

  doc.autoTable({
    startY: currentY + 10,
    head: [calcHeaders],
    body: calcRows,
    theme: 'grid',
    headStyles: {
      fillColor: [241, 245, 249],
      textColor: [51, 65, 85],
      fontSize: 8,
      fontStyle: 'bold',
      halign: 'center',
    },
    bodyStyles: {
      fontSize: 8.5,
      halign: 'center',
      fontStyle: 'bold',
      textColor: [15, 23, 42],
    },
    margin: { left: 40, right: 40 },
  });

  // 4. Detailed Earnings & Deductions Breakdown Table
  const finalCalcTableY = doc.lastAutoTable.finalY + 20;

  doc.setFontSize(10);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(30, 41, 59);
  doc.text('EARNINGS & ADJUSTMENTS BREAKDOWN', 40, finalCalcTableY);

  const basic = agreed * 0.5;
  const hra = agreed * 0.2;
  const special = agreed * 0.2;
  const conveyance = agreed * 0.1;
  const bonus = payslip.bonusOrIncentive ? Number(payslip.bonusOrIncentive) : 0;
  const deductionAmount = excess < 0 ? Math.abs(excess) : 0;

  const breakdownRows = [
    ['Basic Salary (50%)', formatINR(basic), 'Shortfall Time Deduction', formatINR(deductionAmount)],
    ['House Rent Allowance (20%)', formatINR(hra), 'Professional Tax', '₹0.00'],
    ['Special Allowance (20%)', formatINR(special), 'Provident Fund (PF)', '₹0.00'],
    ['Conveyance Allowance (10%)', formatINR(conveyance), 'Other Deductions', '₹0.00'],
    ['Performance Bonus', formatINR(bonus), 'Total Deductions', formatINR(deductionAmount)],
    ['Total Agreed Gross', formatINR(agreed + bonus), 'Net Payable Salary', formatINR(payable)],
  ];

  doc.autoTable({
    startY: finalCalcTableY + 10,
    head: [['Earnings Head', 'Amount (INR)', 'Deductions & Adjustments', 'Amount (INR)']],
    body: breakdownRows,
    theme: 'striped',
    headStyles: {
      fillColor: [79, 70, 229],
      textColor: 255,
      fontSize: 8.5,
      fontStyle: 'bold',
    },
    bodyStyles: {
      fontSize: 8,
      cellPadding: 4,
    },
    margin: { left: 40, right: 40 },
  });

  // 5. Total Net Pay Callout Box
  const summaryY = doc.lastAutoTable.finalY + 20;
  doc.setFillColor(238, 242, 255); // Indigo-50
  doc.setDrawColor(199, 210, 254);
  doc.roundedRect(40, summaryY, pageWidth - 80, 50, 8, 8, 'FD');

  doc.setFontSize(10);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(67, 56, 202);
  doc.text('TOTAL NET PAYABLE SALARY (INR)', 55, summaryY + 22);

  doc.setFontSize(18);
  doc.setFont('helvetica', 'bold');
  doc.setTextColor(49, 46, 129);
  doc.text(formatINR(payable), 55, summaryY + 42);

  doc.setFontSize(8.5);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Formula: Agreed Salary + (Difference Minutes × Salary Per Minute)', pageWidth - 55, summaryY + 24, { align: 'right' });
  doc.setFont('helvetica', 'bold');
  doc.text(`Status: Verified & Processed`, pageWidth - 55, summaryY + 40, { align: 'right' });

  // 6. Signature Lines & Disclaimer
  const signY = summaryY + 80;
  doc.setDrawColor(203, 213, 225);
  doc.line(60, signY, 200, signY);
  doc.line(pageWidth - 200, signY, pageWidth - 60, signY);

  doc.setFontSize(8);
  doc.setFont('helvetica', 'normal');
  doc.setTextColor(100, 116, 139);
  doc.text('Authorized Signatory (HR / Finance)', 130, signY + 12, { align: 'center' });
  doc.text('Employee Signature', pageWidth - 130, signY + 12, { align: 'center' });

  doc.setFontSize(7.5);
  doc.text(
    'This is a system generated payslip and requires no physical stamp. Calculated strictly in Indian Rupees (INR).',
    pageWidth / 2,
    signY + 36,
    { align: 'center' }
  );

  doc.save(`Payslip_${payslip.employeeCode || 'EMP'}_${monthName}_${payslip.salaryYear || 2026}.pdf`);
};
