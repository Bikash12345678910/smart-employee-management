import React, { useState, useEffect } from 'react';
import { adminApi, downloadAuthenticatedFile } from '../../services/api';
import AddEmployeeModal from './AddEmployeeModal';
import EmployeeProfileModal from './EmployeeProfileModal';
import { StatusBadge } from '../../components/common/Badge';
import {
  Users,
  Search,
  Filter,
  Plus,
  Download,
  Eye,
  Edit2,
  Trash2,
  ToggleLeft,
  ToggleRight,
  ShieldAlert,
  ArrowUpDown,
  ChevronLeft,
  ChevronRight,
  Fingerprint,
  Camera,
  CheckCircle,
  X
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function EmployeeManagement() {
  const [employees, setEmployees] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Filters & State
  const [search, setSearch] = useState('');
  const [selectedDept, setSelectedDept] = useState('');
  const [selectedStatus, setSelectedStatus] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [sortBy, setSortBy] = useState('id');
  const [sortDir, setSortDir] = useState('asc');
  const [loading, setLoading] = useState(true);

  // Modals
  const [showAddModal, setShowAddModal] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  const [showProfileModal, setShowProfileModal] = useState(false);
  const [editEmployeeData, setEditEmployeeData] = useState(null);
  const [deleteId, setDeleteId] = useState(null);

  const fetchEmployees = async () => {
    setLoading(true);
    try {
      const res = await adminApi.getEmployees({
        search,
        departmentId: selectedDept || undefined,
        status: selectedStatus || undefined,
        page,
        size,
        sortBy,
        sortDir
      });
      if (res.success && res.data) {
        setEmployees(res.data.content);
        setTotalElements(res.data.totalElements);
        setTotalPages(res.data.totalPages);
      }
    } catch (err) {
      console.error("Failed to fetch employees:", err);
    } finally {
      setLoading(false);
    }
  };

  const fetchDepartments = async () => {
    try {
      const res = await adminApi.getDepartments();
      if (res.success) setDepartments(res.data);
    } catch (err) {
      console.error("Failed to load departments", err);
    }
  };

  useEffect(() => {
    fetchDepartments();
  }, []);

  useEffect(() => {
    fetchEmployees();
  }, [search, selectedDept, selectedStatus, page, size, sortBy, sortDir]);

  const handleSort = (column) => {
    if (sortBy === column) {
      setSortDir(sortDir === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(column);
      setSortDir('asc');
    }
  };

  const handleToggleStatus = async (id) => {
    try {
      await adminApi.toggleStatus(id);
      fetchEmployees();
    } catch (err) {
      alert("Failed to toggle status: " + err.message);
    }
  };

  const handleDelete = async () => {
    if (!deleteId) return;
    try {
      await adminApi.deleteEmployee(deleteId);
      setDeleteId(null);
      await fetchEmployees();
      alert("Employee deleted successfully.");
    } catch (err) {
      alert("Failed to delete employee: " + err.message);
    }
  };

  const handleExportCsv = async () => {
    try {
      await downloadAuthenticatedFile(adminApi.exportEmployeesCsvUrl(), 'employees.csv');
    } catch (err) {
      alert("Failed to export employees CSV: " + err.message);
    }
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Users className="w-6 h-6 text-primary-500" /> Employee Directory
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            View, search, register, and manage corporate employee records and biometrics.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            onClick={handleExportCsv}
            className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-750 transition-colors shadow-xs flex items-center gap-1.5"
          >
            <Download className="w-3.5 h-3.5" /> Export CSV
          </button>

          <button
            onClick={() => {
              setEditEmployeeData(null);
              setShowAddModal(true);
            }}
            className="px-4 py-2 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-500/20 transition-all flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" /> Add Employee
          </button>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <div className="glass-card p-4 flex flex-col md:flex-row items-center justify-between gap-3">
        {/* Search Input */}
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
          <input
            type="text"
            placeholder="Search by name, code (e.g. EMP001), or email..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            className="w-full pl-10 pr-4 py-2 text-xs sm:text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50/50 dark:bg-slate-800/50 text-slate-900 dark:text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-primary-500 transition-all"
          />
        </div>

        {/* Dropdowns */}
        <div className="flex flex-wrap items-center gap-2.5 w-full md:w-auto">
          {/* Status Filter */}
          <select
            value={selectedStatus}
            onChange={(e) => {
              setSelectedStatus(e.target.value);
              setPage(0);
            }}
            className="text-xs font-medium px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary-500"
          >
            <option value="">All Statuses</option>
            <option value="ACTIVE">Active</option>
            <option value="PROBATION">Probation</option>
            <option value="SUSPENDED">Suspended</option>
            <option value="TERMINATED">Terminated</option>
          </select>
        </div>
      </div>

      {/* Employees Table */}
      <div className="glass-card overflow-hidden border border-slate-200 dark:border-slate-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs text-slate-600 dark:text-slate-300">
            <thead className="bg-slate-50 dark:bg-slate-800/70 border-b border-slate-200 dark:border-slate-800 text-[11px] font-bold uppercase tracking-wider text-slate-500 dark:text-slate-400">
              <tr>
                <th scope="col" className="px-4 py-3.5">Employee</th>
                <th scope="col" className="px-4 py-3.5 cursor-pointer select-none" onClick={() => handleSort('employeeCode')}>
                  <div className="flex items-center gap-1">
                    <span>Code</span>
                    <ArrowUpDown className="w-3 h-3" />
                  </div>
                </th>
                <th scope="col" className="px-4 py-3.5">Contact Details</th>
                <th scope="col" className="px-4 py-3.5">Department & Role</th>
                <th scope="col" className="px-4 py-3.5">Protected Financials (Masked)</th>
                <th scope="col" className="px-4 py-3.5">Biometrics</th>
                <th scope="col" className="px-4 py-3.5 cursor-pointer select-none" onClick={() => handleSort('employmentStatus')}>
                  <div className="flex items-center gap-1">
                    <span>Status</span>
                    <ArrowUpDown className="w-3 h-3" />
                  </div>
                </th>
                <th scope="col" className="px-4 py-3.5 text-right">Actions</th>
              </tr>
            </thead>

            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {loading ? (
                <tr>
                  <td colSpan="8" className="px-4 py-12 text-center text-slate-400">
                    <div className="w-6 h-6 border-2 border-primary-500/20 border-t-primary-600 rounded-full animate-spin mx-auto mb-2" />
                    Loading employee records...
                  </td>
                </tr>
              ) : employees.length === 0 ? (
                <tr>
                  <td colSpan="8" className="px-4 py-12 text-center text-slate-400">
                    No matching employees found.
                  </td>
                </tr>
              ) : (
                employees.map((emp) => (
                  <motion.tr
                    key={emp.id}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="hover:bg-slate-50/80 dark:hover:bg-slate-800/40 transition-colors"
                  >
                    {/* Photo & Name */}
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <div className="flex items-center gap-3">
                        <img
                          src={emp.profilePhotoUrl || "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100"}
                          alt={emp.fullName}
                          className="w-9 h-9 rounded-full object-cover ring-1 ring-slate-200 dark:ring-slate-700"
                        />
                        <div>
                          <div className="font-semibold text-slate-900 dark:text-slate-100 text-xs">
                            {emp.fullName}
                          </div>
                          <div className="text-[10px] text-slate-400">
                            {emp.gender} • Joined {emp.joiningDate}
                          </div>
                        </div>
                      </div>
                    </td>

                    {/* Employee Code */}
                    <td className="px-4 py-3.5 whitespace-nowrap font-mono font-bold text-primary-600 dark:text-primary-400">
                      {emp.employeeCode}
                    </td>

                    {/* Contact */}
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <div className="text-slate-900 dark:text-slate-100">{emp.email}</div>
                      <div className="text-[10px] text-slate-400">{emp.phoneNumber}</div>
                    </td>

                    {/* Department & Role */}
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <div className="font-medium text-slate-800 dark:text-slate-200">{emp.departmentName}</div>
                      <div className="text-[10px] text-slate-400">{emp.designationTitle}</div>
                    </td>

                    {/* Masked Sensitive Financials */}
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <div className="font-mono text-[11px] text-slate-700 dark:text-slate-300">
                        Aadhaar: <span className="text-slate-500">{emp.aadhaarMasked}</span>
                      </div>
                      <div className="font-mono text-[10px] text-slate-500">
                        Bank: {emp.bankAccountMasked} ({emp.bankIfscCode})
                      </div>
                    </td>

                    {/* Biometrics Badges */}
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <div className="flex items-center gap-1.5">
                        <span
                          className={`p-1 rounded-md text-[10px] flex items-center gap-0.5 ${
                            emp.isFaceRegistered
                              ? 'bg-cyan-100 dark:bg-cyan-950/80 text-cyan-700 dark:text-cyan-300'
                              : 'bg-slate-100 dark:bg-slate-800 text-slate-400'
                          }`}
                          title={emp.isFaceRegistered ? "Face Enrolled" : "Face Not Registered"}
                        >
                          <Camera className="w-3 h-3" />
                        </span>
                        <span
                          className={`p-1 rounded-md text-[10px] flex items-center gap-0.5 ${
                            emp.isFingerprintRegistered
                              ? 'bg-emerald-100 dark:bg-emerald-950/80 text-emerald-700 dark:text-emerald-300'
                              : 'bg-slate-100 dark:bg-slate-800 text-slate-400'
                          }`}
                          title={emp.isFingerprintRegistered ? "Fingerprint Enrolled" : "Fingerprint Not Registered"}
                        >
                          <Fingerprint className="w-3 h-3" />
                        </span>
                      </div>
                    </td>

                    {/* Status */}
                    <td className="px-4 py-3.5 whitespace-nowrap">
                      <StatusBadge status={emp.employmentStatus} />
                    </td>

                    {/* Actions */}
                    <td className="px-4 py-3.5 whitespace-nowrap text-right">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => {
                            setSelectedEmployee(emp);
                            setShowProfileModal(true);
                          }}
                          className="p-1.5 rounded-lg text-slate-500 hover:text-slate-800 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                          title="View Details"
                        >
                          <Eye className="w-3.5 h-3.5" />
                        </button>

                        <button
                          onClick={() => {
                            setEditEmployeeData(emp);
                            setShowAddModal(true);
                          }}
                          className="p-1.5 rounded-lg text-primary-500 hover:text-primary-700 hover:bg-primary-50 dark:hover:bg-primary-950/40 transition-colors"
                          title="Edit Employee"
                        >
                          <Edit2 className="w-3.5 h-3.5" />
                        </button>

                        <button
                          onClick={() => handleToggleStatus(emp.id)}
                          className={`p-1.5 rounded-lg transition-colors ${
                            emp.isActive
                              ? 'text-emerald-500 hover:text-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-950/40'
                              : 'text-amber-500 hover:text-amber-700 hover:bg-amber-50 dark:hover:bg-amber-950/40'
                          }`}
                          title={emp.isActive ? "Deactivate Employee" : "Activate Employee"}
                        >
                          {emp.isActive ? <ToggleRight className="w-4 h-4" /> : <ToggleLeft className="w-4 h-4" />}
                        </button>

                        <button
                          onClick={() => setDeleteId(emp.id)}
                          className="p-1.5 rounded-lg text-rose-500 hover:text-rose-700 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors"
                          title="Delete Employee"
                        >
                          <Trash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </td>
                  </motion.tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        <div className="p-4 border-t border-slate-100 dark:border-slate-800 flex flex-col sm:flex-row items-center justify-between gap-3 text-xs text-slate-500">
          <div>
            Showing <span className="font-semibold text-slate-800 dark:text-slate-200">{employees.length}</span> of{' '}
            <span className="font-semibold text-slate-800 dark:text-slate-200">{totalElements}</span> employees
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={() => setPage(prev => Math.max(0, prev - 1))}
              disabled={page === 0}
              className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40 transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>

            <span className="px-2 font-mono">
              Page {page + 1} of {Math.max(1, totalPages)}
            </span>

            <button
              onClick={() => setPage(prev => Math.min(totalPages - 1, prev + 1))}
              disabled={page >= totalPages - 1}
              className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40 transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>

      {/* Add / Edit Employee Modal */}
      {showAddModal && (
        <AddEmployeeModal
          isOpen={showAddModal}
          onClose={() => setShowAddModal(false)}
          onSuccess={() => {
            setShowAddModal(false);
            fetchEmployees();
          }}
          departments={departments}
          initialData={editEmployeeData}
        />
      )}

      {/* View Profile Modal */}
      {showProfileModal && selectedEmployee && (
        <EmployeeProfileModal
          isOpen={showProfileModal}
          onClose={() => {
            setShowProfileModal(false);
            setSelectedEmployee(null);
          }}
          employee={selectedEmployee}
        />
      )}

      {/* Delete Confirmation Modal */}
      {deleteId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm">
          <motion.div
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            className="w-full max-w-sm glass-card p-6 bg-white dark:bg-slate-900 space-y-4"
          >
            <div className="w-10 h-10 rounded-2xl bg-rose-100 dark:bg-rose-950/80 text-rose-600 flex items-center justify-center mx-auto">
              <ShieldAlert className="w-5 h-5" />
            </div>
            <div className="text-center space-y-1">
              <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">Confirm Deletion</h3>
              <p className="text-xs text-slate-500">
                Are you sure you want to permanently delete this employee? Their account and records will be removed.
              </p>
            </div>
            <div className="flex items-center justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setDeleteId(null)}
                className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleDelete}
                className="px-4 py-2 text-xs font-semibold rounded-xl bg-rose-600 hover:bg-rose-700 text-white"
              >
                Confirm Delete
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </div>
  );
}
