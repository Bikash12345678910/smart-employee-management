import React, { useState, useEffect, useMemo } from 'react';
import { adminApi } from '../../services/api';
import FaceScannerModal from '../../components/biometrics/FaceScannerModal';
import FingerprintScannerModal from '../../components/biometrics/FingerprintScannerModal';
import {
  Fingerprint,
  Camera,
  Cpu,
  Smartphone,
  Wifi,
  Radio,
  RefreshCw,
  CheckCircle,
  AlertTriangle,
  ShieldCheck,
  Server,
  Activity,
  Plus,
  Search,
  Filter,
  Trash2,
  Edit,
  Eye,
  X,
  Check,
  ChevronLeft,
  ChevronRight,
  HardDrive
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

export default function BiometricManagement() {
  const [devices, setDevices] = useState([]);
  const [employees, setEmployees] = useState([]);
  const [selectedEmpId, setSelectedEmpId] = useState('');
  const [loading, setLoading] = useState(true);
  const [testingId, setTestingId] = useState(null);
  const [message, setMessage] = useState(null);

  // Modals
  const [showFaceModal, setShowFaceModal] = useState(false);
  const [showFpModal, setShowFpModal] = useState(false);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(null);
  const [showEditModal, setShowEditModal] = useState(null);

  // Table filters & pagination
  const [searchQuery, setSearchQuery] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 6;

  // Add Device Form State
  const initialForm = {
    deviceType: 'BIOMETRIC', // 'BIOMETRIC' or 'MOBILE'
    deviceName: '',
    deviceSerialNumber: '',
    deviceUuid: '',
    deviceModel: '',
    manufacturer: 'ZKTeco',
    devicePlatform: 'Android',
    ipAddress: '',
    port: 4370,
    communicationProtocol: 'TCP/IP',
    connectionType: 'LAN',
    deviceUsername: '',
    devicePassword: '',
    location: '',
    assignedEmployeeId: '',
    status: 'DISCONNECTED',
  };
  const [formData, setFormData] = useState(initialForm);
  const [formErrors, setFormErrors] = useState({});
  const [savingDevice, setSavingDevice] = useState(false);
  const [companySettings, setCompanySettings] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [devRes, empRes, settingsRes] = await Promise.all([
        adminApi.getBiometricDevices(),
        adminApi.getEmployees({ size: 100 }),
        adminApi.getSettings().catch(() => ({ success: false }))
      ]);
      if (settingsRes?.success && settingsRes?.data) {
        setCompanySettings(settingsRes.data);
      }
      if (devRes.success) setDevices(devRes.data);
      if (empRes.success) {
        setEmployees(empRes.data.content);
        if (empRes.data.content.length > 0 && !selectedEmpId) {
          setSelectedEmpId(empRes.data.content[0].id);
        }
      }
    } catch (err) {
      console.error("Failed to load biometric data:", err);
      setMessage({ type: 'error', text: 'Error loading devices: ' + err.message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleTestConnection = async (id) => {
    setTestingId(id);
    try {
      const res = await adminApi.testBiometricDeviceConnection(id);
      if (res.success && res.data) {
        const info = res.data;
        if (info.connected) {
          setMessage({
            type: 'success',
            text: `Connection Verified: ${info.deviceName} responded in ${info.latencyMs}ms. Status updated to CONNECTED.`
          });
        } else {
          setMessage({
            type: 'error',
            text: `Connection Failed: ${info.message}`
          });
        }
        fetchData();
      }
    } catch (err) {
      setMessage({ type: 'error', text: 'Test connection failed: ' + err.message });
    } finally {
      setTestingId(null);
    }
  };

  const handleDeleteDevice = async (id, name) => {
    if (!window.confirm(`Are you sure you want to delete "${name}"?`)) return;
    try {
      await adminApi.deleteBiometricDevice(id);
      setMessage({ type: 'success', text: `Device "${name}" has been removed.` });
      fetchData();
    } catch (err) {
      setMessage({ type: 'error', text: 'Delete failed: ' + err.message });
    }
  };

  const validateDeviceForm = (data) => {
    const errors = {};
    if (!data.deviceName?.trim()) errors.deviceName = 'Device name is required';
    if (!data.deviceModel?.trim()) errors.deviceModel = 'Device model is required';
    if (!data.location?.trim()) errors.location = 'Location is required';

    if (data.deviceType === 'BIOMETRIC') {
      if (!data.deviceSerialNumber?.trim()) errors.deviceSerialNumber = 'Serial number is required';
      if (!data.ipAddress?.trim()) errors.ipAddress = 'IP Address is required';
      if (!data.port) errors.port = 'Port is required';
    } else {
      if (!data.deviceUuid?.trim() && !data.deviceSerialNumber?.trim()) {
        errors.deviceUuid = 'Device ID / UUID is required';
      }
    }
    return errors;
  };

  const handleSaveNewDevice = async (e) => {
    e.preventDefault();
    const errors = validateDeviceForm(formData);
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }
    setFormErrors({});
    setSavingDevice(true);
    try {
      const payload = {
        ...formData,
        deviceSerialNumber: formData.deviceType === 'MOBILE' ? (formData.deviceUuid || formData.deviceSerialNumber) : formData.deviceSerialNumber,
        manufacturer: formData.deviceType === 'MOBILE' ? (formData.manufacturer || 'Samsung') : formData.manufacturer,
        connectionType: formData.connectionType || (formData.deviceType === 'MOBILE' ? 'Wi-Fi' : 'LAN'),
        port: Number(formData.port) || (formData.deviceType === 'MOBILE' ? 8080 : 4370),
        assignedEmployeeId: formData.assignedEmployeeId ? Number(formData.assignedEmployeeId) : null,
      };
      await adminApi.createBiometricDevice(payload);
      setMessage({ type: 'success', text: `Registered new ${payload.deviceType === 'MOBILE' ? 'Mobile' : 'Biometric'} device successfully!` });
      setShowAddModal(false);
      setFormData(initialForm);
      fetchData();
    } catch (err) {
      setMessage({ type: 'error', text: 'Failed to add device: ' + err.message });
    } finally {
      setSavingDevice(false);
    }
  };

  const handleUpdateDevice = async (e) => {
    e.preventDefault();
    if (!showEditModal) return;
    const errors = validateDeviceForm(showEditModal);
    if (Object.keys(errors).length > 0) {
      setFormErrors(errors);
      return;
    }
    setFormErrors({});
    setSavingDevice(true);
    try {
      await adminApi.updateBiometricDevice(showEditModal.id, showEditModal);
      setMessage({ type: 'success', text: `Device "${showEditModal.deviceName}" updated successfully!` });
      setShowEditModal(null);
      fetchData();
    } catch (err) {
      setMessage({ type: 'error', text: 'Failed to update device: ' + err.message });
    } finally {
      setSavingDevice(false);
    }
  };

  const handleFaceEnrolled = async (base64) => {
    if (!selectedEmpId) return;
    try {
      await adminApi.enrollBiometric({
        employeeId: selectedEmpId,
        biometricType: 'FACE',
        rawSampleOrTemplate: base64
      });
      setMessage({ type: 'success', text: 'Face biometric vector enrolled successfully!' });
      fetchData();
    } catch (err) {
      setMessage({ type: 'error', text: 'Enrollment failed: ' + err.message });
    }
  };

  const handleFingerprintEnrolled = async (fpData) => {
    if (!selectedEmpId) return;
    try {
      await adminApi.enrollBiometric({
        employeeId: selectedEmpId,
        biometricType: 'FINGERPRINT',
        deviceId: fpData.deviceId,
        rawSampleOrTemplate: fpData.template
      });
      setMessage({ type: 'success', text: 'Fingerprint ridge template stored in device slot!' });
      fetchData();
    } catch (err) {
      setMessage({ type: 'error', text: 'Enrollment failed: ' + err.message });
    }
  };

  // Filtered devices
  const filteredDevices = useMemo(() => {
    return devices.filter(d => {
      const matchSearch =
        d.deviceName?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        d.deviceModel?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        d.deviceSerialNumber?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        d.ipAddress?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        d.location?.toLowerCase().includes(searchQuery.toLowerCase());
      
      const matchType = typeFilter === 'ALL' || d.deviceType === typeFilter;
      const matchStatus = statusFilter === 'ALL' || d.status === statusFilter;
      return matchSearch && matchType && matchStatus;
    });
  }, [devices, searchQuery, typeFilter, statusFilter]);

  const totalPages = Math.ceil(filteredDevices.length / itemsPerPage) || 1;
  const paginatedDevices = useMemo(() => {
    const start = (currentPage - 1) * itemsPerPage;
    return filteredDevices.slice(start, start + itemsPerPage);
  }, [filteredDevices, currentPage]);

  const selectedEmployee = employees.find(e => e.id === Number(selectedEmpId));

  const getStatusBadge = (status) => {
    const s = (status || '').toUpperCase();
    if (s === 'CONNECTED' || s === 'ONLINE') {
      return (
        <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-100 text-emerald-700 dark:bg-emerald-950/80 dark:text-emerald-400 border border-emerald-300 dark:border-emerald-800 flex items-center gap-1.5 inline-flex">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" /> Connected
        </span>
      );
    }
    if (s === 'CONNECTION_FAILED' || s === 'ERROR') {
      return (
        <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-rose-100 text-rose-700 dark:bg-rose-950/80 dark:text-rose-400 border border-rose-300 dark:border-rose-800 flex items-center gap-1.5 inline-flex">
          <span className="w-2 h-2 rounded-full bg-rose-500" /> Connection Failed
        </span>
      );
    }
    return (
      <span className="px-2.5 py-1 rounded-full text-[11px] font-bold bg-amber-100 text-amber-700 dark:bg-amber-950/80 dark:text-amber-400 border border-amber-300 dark:border-amber-800 flex items-center gap-1.5 inline-flex">
        <span className="w-2 h-2 rounded-full bg-amber-500" /> Disconnected
      </span>
    );
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Fingerprint className="w-6 h-6 text-primary-500" /> Biometric & Mobile Attendance Devices
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400">
            Register, monitor and test hardware biometric terminals and Android mobile kiosk devices.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={fetchData}
            className="px-3.5 py-2 text-xs font-semibold rounded-xl bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-750 transition-colors shadow-xs flex items-center gap-1.5"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </button>

          <button
            onClick={() => {
              setFormData(initialForm);
              setFormErrors({});
              setShowAddModal(true);
            }}
            className="px-4 py-2 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-500/20 transition-all flex items-center gap-1.5"
          >
            <Plus className="w-4 h-4" /> Add Device
          </button>
        </div>
      </div>

      {/* Alert banner */}
      {message && (
        <div className={`p-4 rounded-2xl text-xs flex items-center justify-between gap-2 border ${
          message.type === 'success'
            ? 'bg-emerald-50 dark:bg-emerald-950/60 text-emerald-700 dark:text-emerald-300 border-emerald-200 dark:border-emerald-800'
            : 'bg-rose-50 dark:bg-rose-950/60 text-rose-700 dark:text-rose-300 border-rose-200 dark:border-rose-800'
        }`}>
          <div className="flex items-center gap-2">
            {message.type === 'success' ? <CheckCircle className="w-4 h-4 flex-shrink-0" /> : <AlertTriangle className="w-4 h-4 flex-shrink-0" />}
            <span>{message.text}</span>
          </div>
          <button onClick={() => setMessage(null)} className="text-slate-400 hover:text-slate-600 text-base font-bold">
            &times;
          </button>
        </div>
      )}

      {/* SECTION 1: Hardware Device Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-4">
        {devices.map((device) => {
          const isMobile = device.deviceType === 'MOBILE';
          return (
            <div key={device.id} className="glass-card p-5 border border-slate-200 dark:border-slate-800 space-y-4 relative overflow-hidden">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <div className={`p-3 rounded-2xl ${isMobile ? 'bg-purple-50 dark:bg-purple-950 text-purple-600 dark:text-purple-400' : 'bg-cyan-50 dark:bg-cyan-950 text-cyan-600 dark:text-cyan-400'}`}>
                    {isMobile ? <Smartphone className="w-5 h-5" /> : <HardDrive className="w-5 h-5" />}
                  </div>
                  <div>
                    <span className="text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-md bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-300">
                      {isMobile ? 'Mobile Device' : 'Biometric Device'}
                    </span>
                    <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm mt-0.5">
                      {device.deviceName}
                    </h3>
                  </div>
                </div>

                <div>{getStatusBadge(device.status)}</div>
              </div>

              <div className="p-3.5 rounded-xl bg-slate-50 dark:bg-slate-800/60 text-xs space-y-1.5">
                <div className="flex justify-between text-slate-500">
                  <span>Model / Make:</span>
                  <span className="font-semibold text-slate-800 dark:text-slate-200">{device.manufacturer || 'Standard'} {device.deviceModel}</span>
                </div>
                <div className="flex justify-between text-slate-500">
                  <span>Device ID / SN:</span>
                  <span className="font-mono text-slate-800 dark:text-slate-200">{device.deviceSerialNumber || device.deviceUuid}</span>
                </div>
                <div className="flex justify-between text-slate-500">
                  <span>Connection:</span>
                  <span className="font-mono text-slate-800 dark:text-slate-200">
                    {device.connectionType} • {device.ipAddress ? `${device.ipAddress}:${device.port}` : 'Mobile IP'}
                  </span>
                </div>
                <div className="flex justify-between text-slate-500">
                  <span>Assigned Location:</span>
                  <span className="text-slate-800 dark:text-slate-200 font-medium">{device.location}</span>
                </div>
                <div className="flex justify-between text-slate-500">
                  <span>Last Handshake:</span>
                  <span className="font-mono text-[10px] text-emerald-600 dark:text-emerald-400 font-semibold">
                    {device.lastSyncTime ? new Date(device.lastSyncTime).toLocaleString() : 'Verified Ready'}
                  </span>
                </div>
              </div>

              <div className="pt-2 flex items-center justify-between border-t border-slate-100 dark:border-slate-800">
                <span className="text-[11px] text-slate-400 flex items-center gap-1 font-mono">
                  <Activity className="w-3.5 h-3.5 text-emerald-500" />
                  Protocol: {device.communicationProtocol || 'TCP/IP'}
                </span>

                <button
                  type="button"
                  onClick={() => handleTestConnection(device.id)}
                  disabled={testingId === device.id}
                  className="px-3 py-1.5 text-xs font-semibold rounded-xl bg-primary-50 dark:bg-primary-950/60 border border-primary-200 dark:border-primary-800 text-primary-700 dark:text-primary-300 hover:bg-primary-100 transition-colors shadow-xs flex items-center gap-1.5"
                >
                  <Wifi className={`w-3.5 h-3.5 ${testingId === device.id ? 'animate-ping' : ''}`} />
                  {testingId === device.id ? 'Testing...' : 'Test Connection'}
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* SECTION 2: Add Device Record Section (Complete Table) */}
      <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h2 className="text-base font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
              <Server className="w-4 h-4 text-primary-500" /> Device Records
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Registered Mobile and Biometric devices with connection verification and management.
            </p>
          </div>

          {/* Search and Filters */}
          <div className="flex flex-wrap items-center gap-2">
            <div className="relative">
              <Search className="w-3.5 h-3.5 absolute left-3 top-2.5 text-slate-400" />
              <input
                type="text"
                placeholder="Search device name, ID, IP..."
                value={searchQuery}
                onChange={(e) => { setSearchQuery(e.target.value); setCurrentPage(1); }}
                className="pl-8 pr-3 py-1.5 text-xs rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary-500 w-48 sm:w-60"
              />
            </div>

            <select
              value={typeFilter}
              onChange={(e) => { setTypeFilter(e.target.value); setCurrentPage(1); }}
              className="px-2.5 py-1.5 text-xs font-medium rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
            >
              <option value="ALL">All Types</option>
              <option value="BIOMETRIC">Biometric Devices</option>
              <option value="MOBILE">Mobile Devices</option>
            </select>

            <select
              value={statusFilter}
              onChange={(e) => { setStatusFilter(e.target.value); setCurrentPage(1); }}
              className="px-2.5 py-1.5 text-xs font-medium rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100"
            >
              <option value="ALL">All Statuses</option>
              <option value="CONNECTED">Connected / Online</option>
              <option value="DISCONNECTED">Disconnected</option>
              <option value="CONNECTION_FAILED">Connection Failed</option>
            </select>
          </div>
        </div>

        {/* Device Table */}
        <div className="overflow-x-auto rounded-xl border border-slate-200 dark:border-slate-800">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-50 dark:bg-slate-800/80 text-slate-600 dark:text-slate-300 font-bold uppercase tracking-wider text-[11px] border-b border-slate-200 dark:border-slate-800">
              <tr>
                <th className="px-4 py-3">Device Name</th>
                <th className="px-3 py-3">Device ID</th>
                <th className="px-3 py-3">Device Type</th>
                <th className="px-3 py-3">Device Model</th>
                <th className="px-3 py-3">Connection Type</th>
                <th className="px-3 py-3">IP Address</th>
                <th className="px-3 py-3">Assigned Location</th>
                <th className="px-3 py-3">Connection Status</th>
                <th className="px-3 py-3">Created Date</th>
                <th className="px-3 py-3">Last Connected</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
              {paginatedDevices.length === 0 ? (
                <tr>
                  <td colSpan="11" className="px-4 py-8 text-center text-slate-400">
                    No registered devices matching the selected filters.
                  </td>
                </tr>
              ) : (
                paginatedDevices.map((d) => (
                  <tr key={d.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/50 transition-colors">
                    <td className="px-4 py-3 font-semibold text-slate-900 dark:text-slate-100">
                      <div className="flex items-center gap-2">
                        {d.deviceType === 'MOBILE' ? (
                          <Smartphone className="w-4 h-4 text-purple-500 flex-shrink-0" />
                        ) : (
                          <HardDrive className="w-4 h-4 text-cyan-500 flex-shrink-0" />
                        )}
                        <span>{d.deviceName}</span>
                      </div>
                    </td>
                    <td className="px-3 py-3 font-mono text-[11px] text-slate-600 dark:text-slate-300">
                      {d.deviceSerialNumber || d.deviceUuid}
                    </td>
                    <td className="px-3 py-3">
                      <span className={`px-2 py-0.5 rounded text-[10px] font-bold ${
                        d.deviceType === 'MOBILE'
                          ? 'bg-purple-100 text-purple-700 dark:bg-purple-950 dark:text-purple-300'
                          : 'bg-cyan-100 text-cyan-700 dark:bg-cyan-950 dark:text-cyan-300'
                      }`}>
                        {d.deviceType === 'MOBILE' ? 'Mobile Device' : 'Biometric Device'}
                      </span>
                    </td>
                    <td className="px-3 py-3 text-slate-700 dark:text-slate-300">
                      {d.deviceModel}
                    </td>
                    <td className="px-3 py-3 text-slate-600 dark:text-slate-400">
                      {d.connectionType || 'Wi-Fi'}
                    </td>
                    <td className="px-3 py-3 font-mono text-[11px] text-slate-600 dark:text-slate-400">
                      {d.ipAddress ? `${d.ipAddress}:${d.port}` : '-'}
                    </td>
                    <td className="px-3 py-3 text-slate-700 dark:text-slate-300">
                      {d.location}
                    </td>
                    <td className="px-3 py-3">
                      {getStatusBadge(d.status)}
                    </td>
                    <td className="px-3 py-3 text-slate-500 text-[11px]">
                      {d.createdAt ? new Date(d.createdAt).toLocaleDateString() : 'Initial'}
                    </td>
                    <td className="px-3 py-3 font-mono text-[11px] text-slate-600 dark:text-slate-400">
                      {d.lastSyncTime ? new Date(d.lastSyncTime).toLocaleTimeString() : 'Never'}
                    </td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex items-center justify-end gap-1.5">
                        <button
                          onClick={() => handleTestConnection(d.id)}
                          disabled={testingId === d.id}
                          title="Test Connection"
                          className="p-1.5 rounded-lg text-primary-600 hover:bg-primary-50 dark:hover:bg-primary-950/60"
                        >
                          <Wifi className={`w-3.5 h-3.5 ${testingId === d.id ? 'animate-ping' : ''}`} />
                        </button>
                        <button
                          onClick={() => setShowViewModal(d)}
                          title="View Details"
                          className="p-1.5 rounded-lg text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800"
                        >
                          <Eye className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => {
                            setShowEditModal({ ...d });
                            setFormErrors({});
                          }}
                          title="Edit Device"
                          className="p-1.5 rounded-lg text-amber-600 hover:bg-amber-50 dark:hover:bg-amber-950/60"
                        >
                          <Edit className="w-3.5 h-3.5" />
                        </button>
                        <button
                          onClick={() => handleDeleteDevice(d.id, d.deviceName)}
                          title="Delete Device"
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

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between text-xs text-slate-500 pt-2">
            <span>Showing {paginatedDevices.length} of {filteredDevices.length} devices</span>
            <div className="flex items-center gap-1">
              <button
                disabled={currentPage === 1}
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 disabled:opacity-40"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <span className="px-2 font-bold">{currentPage} / {totalPages}</span>
              <button
                disabled={currentPage === totalPages}
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                className="p-1.5 rounded-lg border border-slate-200 dark:border-slate-700 disabled:opacity-40"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* SECTION 3: Employee Biometric Enrolment Desk */}
      <div className="glass-card p-6 border border-slate-200 dark:border-slate-800 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-100 dark:border-slate-800">
          <div>
            <h2 className="text-sm font-bold text-slate-900 dark:text-slate-100 flex items-center gap-2">
              <ShieldCheck className="w-4 h-4 text-primary-500" /> Employee Biometric Enrolment Desk
            </h2>
            <p className="text-xs text-slate-500 dark:text-slate-400">
              Select an employee to capture new facial features or fingerprint sensor templates.
            </p>
          </div>

          {/* Employee Picker */}
          <div className="w-full sm:w-72">
            <select
              value={selectedEmpId}
              onChange={(e) => setSelectedEmpId(e.target.value)}
              className="w-full text-xs font-semibold px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 focus:ring-2 focus:ring-primary-500"
            >
              {employees.map(e => (
                <option key={e.id} value={e.id}>
                  {e.employeeCode} - {e.fullName} ({e.departmentName})
                </option>
              ))}
            </select>
          </div>
        </div>

        {selectedEmployee && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Face Registration Box */}
            <div className="p-5 rounded-3xl bg-slate-50 dark:bg-slate-850 border border-slate-200/80 dark:border-slate-750 flex flex-col justify-between space-y-4">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="p-3 rounded-2xl bg-cyan-100 dark:bg-cyan-950 text-cyan-600 dark:text-cyan-400">
                    <Camera className="w-6 h-6" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">Live Face Registration</h3>
                    <p className="text-xs text-slate-500">256-D Neural Facial Embedding Vector</p>
                  </div>
                </div>

                <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
                  selectedEmployee.isFaceRegistered
                    ? 'bg-emerald-100 dark:bg-emerald-950 text-emerald-600'
                    : 'bg-amber-100 dark:bg-amber-950 text-amber-600'
                }`}>
                  {selectedEmployee.isFaceRegistered ? "Enrolled" : "Not Enrolled"}
                </span>
              </div>

              <div className="p-3 rounded-2xl bg-white dark:bg-slate-900 text-xs space-y-1">
                <div className="flex justify-between text-slate-500">
                  <span>Enrolment Status:</span>
                  <span className="font-semibold text-slate-800 dark:text-slate-200">
                    {selectedEmployee.isFaceRegistered ? "Active Vector in Database" : "Pending Capture"}
                  </span>
                </div>
                <div className="flex justify-between text-slate-500">
                  <span>AI Recognition Model:</span>
                  <span className="font-mono text-slate-800 dark:text-slate-200">MobileNet FaceNet v2</span>
                </div>
              </div>

              <button
                type="button"
                disabled={companySettings?.enableFaceAttendance === false}
                onClick={() => setShowFaceModal(true)}
                className="w-full py-2.5 text-xs font-semibold rounded-xl bg-cyan-600 hover:bg-cyan-700 text-white shadow-md shadow-cyan-600/20 transition-all flex items-center justify-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <Camera className="w-4 h-4" />
                {companySettings?.enableFaceAttendance === false
                  ? 'Face Attendance Disabled (Company Settings)'
                  : selectedEmployee.isFaceRegistered
                  ? 'Re-enroll Face Biometric'
                  : 'Open Camera & Capture Face'}
              </button>
            </div>

            {/* Fingerprint Registration Box */}
            <div className="p-5 rounded-3xl bg-slate-50 dark:bg-slate-850 border border-slate-200/80 dark:border-slate-750 flex flex-col justify-between space-y-4">
              <div className="flex items-start justify-between">
                <div className="flex items-center gap-3">
                  <div className="p-3 rounded-2xl bg-emerald-100 dark:bg-emerald-950 text-emerald-600 dark:text-emerald-400">
                    <Fingerprint className="w-6 h-6" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">Optical Fingerprint Sensor</h3>
                    <p className="text-xs text-slate-500">Ridge Minutiae Template Extraction</p>
                  </div>
                </div>

                <span className={`px-2.5 py-0.5 rounded-full text-[10px] font-semibold ${
                  selectedEmployee.isFingerprintRegistered
                    ? 'bg-emerald-100 dark:bg-emerald-950 text-emerald-600'
                    : 'bg-amber-100 dark:bg-amber-950 text-amber-600'
                }`}>
                  {selectedEmployee.isFingerprintRegistered ? "Enrolled" : "Not Enrolled"}
                </span>
              </div>

              <div className="p-3 rounded-2xl bg-white dark:bg-slate-900 text-xs space-y-1">
                <div className="flex justify-between text-slate-500">
                  <span>Hardware Sensor Slot:</span>
                  <span className="font-mono text-slate-800 dark:text-slate-200">
                    Slot #{selectedEmployee.id} (Allocated)
                  </span>
                </div>
                <div className="flex justify-between text-slate-500">
                  <span>Template Encryption:</span>
                  <span className="font-mono text-slate-800 dark:text-slate-200">One-way SHA-256 Hashed</span>
                </div>
              </div>

              <button
                type="button"
                disabled={companySettings?.enableFingerprintAttendance === false}
                onClick={() => setShowFpModal(true)}
                className="w-full py-2.5 text-xs font-semibold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-md shadow-emerald-600/20 transition-all flex items-center justify-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed"
              >
                <Fingerprint className="w-4 h-4" />
                {companySettings?.enableFingerprintAttendance === false
                  ? 'Fingerprint Attendance Disabled (Company Settings)'
                  : selectedEmployee.isFingerprintRegistered
                  ? 'Re-scan Fingerprint'
                  : 'Connect Device & Enroll Fingerprint'}
              </button>
            </div>
          </div>
        )}
      </div>

      {/* ========================================================================= */}
      {/* ADD DEVICE MODAL - Dynamic fields based on Biometric vs Mobile */}
      {/* ========================================================================= */}
      <AnimatePresence>
        {showAddModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs overflow-y-auto">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="glass-card w-full max-w-xl max-h-[90vh] overflow-y-auto p-6 border border-slate-200 dark:border-slate-800 space-y-5 bg-white dark:bg-slate-900 shadow-2xl rounded-3xl"
            >
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
                <div className="flex items-center gap-2">
                  <div className="p-2 rounded-xl bg-primary-100 dark:bg-primary-950 text-primary-600">
                    <Plus className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-900 dark:text-slate-100 text-base">Register New Device</h3>
                    <p className="text-xs text-slate-500">Add an attendance terminal to MySQL database</p>
                  </div>
                </div>
                <button onClick={() => setShowAddModal(false)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              <form onSubmit={handleSaveNewDevice} className="space-y-4">
                {/* 1. Device Type Selector Radio Cards */}
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-2">
                    Select Device Type *
                  </label>
                  <div className="grid grid-cols-2 gap-3">
                    <button
                      type="button"
                      onClick={() => setFormData({ ...formData, deviceType: 'BIOMETRIC', manufacturer: 'ZKTeco', port: 4370, connectionType: 'LAN' })}
                      className={`p-3.5 rounded-2xl border text-left flex items-start gap-3 transition-all ${
                        formData.deviceType === 'BIOMETRIC'
                          ? 'border-cyan-500 bg-cyan-50/70 dark:bg-cyan-950/40 ring-2 ring-cyan-500/20'
                          : 'border-slate-200 dark:border-slate-700 hover:bg-slate-50'
                      }`}
                    >
                      <HardDrive className={`w-5 h-5 mt-0.5 ${formData.deviceType === 'BIOMETRIC' ? 'text-cyan-600' : 'text-slate-400'}`} />
                      <div>
                        <div className="font-bold text-xs text-slate-900 dark:text-slate-100">Biometric Device</div>
                        <div className="text-[10px] text-slate-500">ZKTeco, Suprema, R307, LAN/TCP</div>
                      </div>
                    </button>

                    <button
                      type="button"
                      onClick={() => setFormData({ ...formData, deviceType: 'MOBILE', manufacturer: 'Samsung', port: 8080, connectionType: 'Wi-Fi' })}
                      className={`p-3.5 rounded-2xl border text-left flex items-start gap-3 transition-all ${
                        formData.deviceType === 'MOBILE'
                          ? 'border-purple-500 bg-purple-50/70 dark:bg-purple-950/40 ring-2 ring-purple-500/20'
                          : 'border-slate-200 dark:border-slate-700 hover:bg-slate-50'
                      }`}
                    >
                      <Smartphone className={`w-5 h-5 mt-0.5 ${formData.deviceType === 'MOBILE' ? 'text-purple-600' : 'text-slate-400'}`} />
                      <div>
                        <div className="font-bold text-xs text-slate-900 dark:text-slate-100">Mobile Device</div>
                        <div className="text-[10px] text-slate-500">Android Tablet, Kiosk Mode, App</div>
                      </div>
                    </button>
                  </div>
                </div>

                {/* Common Field: Device Name */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                      Device Name *
                    </label>
                    <input
                      type="text"
                      placeholder={formData.deviceType === 'MOBILE' ? "e.g. Front Gate Android Tab" : "e.g. HQ Turnstile Scanner"}
                      value={formData.deviceName}
                      onChange={(e) => setFormData({ ...formData, deviceName: e.target.value })}
                      className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary-500"
                    />
                    {formErrors.deviceName && <p className="text-[10px] text-rose-500 mt-0.5">{formErrors.deviceName}</p>}
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                      Device Model *
                    </label>
                    <input
                      type="text"
                      placeholder={formData.deviceType === 'MOBILE' ? "e.g. Galaxy Tab A9+" : "e.g. ZKTeco uFace800"}
                      value={formData.deviceModel}
                      onChange={(e) => setFormData({ ...formData, deviceModel: e.target.value })}
                      className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 focus:ring-2 focus:ring-primary-500"
                    />
                    {formErrors.deviceModel && <p className="text-[10px] text-rose-500 mt-0.5">{formErrors.deviceModel}</p>}
                  </div>
                </div>

                {/* DYNAMIC FIELDS: BIOMETRIC DEVICE */}
                {formData.deviceType === 'BIOMETRIC' ? (
                  <>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Manufacturer *
                        </label>
                        <select
                          value={formData.manufacturer}
                          onChange={(e) => setFormData({ ...formData, manufacturer: e.target.value })}
                          className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        >
                          <option value="ZKTeco">ZKTeco</option>
                          <option value="Suprema">Suprema BioStation</option>
                          <option value="Mantra">Mantra Softech</option>
                          <option value="Morpho">Morpho Safran</option>
                          <option value="Hikvision">Hikvision MinMoe</option>
                          <option value="Realtime">Realtime Biometrics</option>
                          <option value="Custom/ESP32">R307 UART / ESP32</option>
                        </select>
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Serial Number (Device ID) *
                        </label>
                        <input
                          type="text"
                          placeholder="e.g. ZK-UF800-48821"
                          value={formData.deviceSerialNumber}
                          onChange={(e) => setFormData({ ...formData, deviceSerialNumber: e.target.value })}
                          className="w-full text-xs font-mono px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        />
                        {formErrors.deviceSerialNumber && <p className="text-[10px] text-rose-500 mt-0.5">{formErrors.deviceSerialNumber}</p>}
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                      <div className="sm:col-span-2">
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Device IP Address *
                        </label>
                        <input
                          type="text"
                          placeholder="e.g. 192.168.1.120"
                          value={formData.ipAddress}
                          onChange={(e) => setFormData({ ...formData, ipAddress: e.target.value })}
                          className="w-full text-xs font-mono px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        />
                        {formErrors.ipAddress && <p className="text-[10px] text-rose-500 mt-0.5">{formErrors.ipAddress}</p>}
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Port Number *
                        </label>
                        <input
                          type="number"
                          placeholder="4370"
                          value={formData.port}
                          onChange={(e) => setFormData({ ...formData, port: e.target.value })}
                          className="w-full text-xs font-mono px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Communication Protocol
                        </label>
                        <select
                          value={formData.communicationProtocol}
                          onChange={(e) => setFormData({ ...formData, communicationProtocol: e.target.value })}
                          className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        >
                          <option value="TCP/IP">TCP/IP</option>
                          <option value="UDP">UDP (ZKTeco Native)</option>
                          <option value="RS485">RS485 Serial</option>
                          <option value="USB">USB Direct</option>
                          <option value="REST/HTTP">REST / Webhook</option>
                        </select>
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Connection Type
                        </label>
                        <select
                          value={formData.connectionType}
                          onChange={(e) => setFormData({ ...formData, connectionType: e.target.value })}
                          className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        >
                          <option value="LAN">Ethernet / LAN</option>
                          <option value="Wi-Fi">Wi-Fi (802.11 b/g/n)</option>
                          <option value="USB">USB Cable</option>
                        </select>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Device Username (Optional)
                        </label>
                        <input
                          type="text"
                          placeholder="admin"
                          value={formData.deviceUsername}
                          onChange={(e) => setFormData({ ...formData, deviceUsername: e.target.value })}
                          className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        />
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Device Password (Optional)
                        </label>
                        <input
                          type="password"
                          placeholder="••••••••"
                          value={formData.devicePassword}
                          onChange={(e) => setFormData({ ...formData, devicePassword: e.target.value })}
                          className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        />
                      </div>
                    </div>
                  </>
                ) : (
                  /* DYNAMIC FIELDS: MOBILE DEVICE */
                  <>
                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Device ID / UUID *
                        </label>
                        <input
                          type="text"
                          placeholder="e.g. MOB-TAB-A9-001"
                          value={formData.deviceUuid}
                          onChange={(e) => setFormData({ ...formData, deviceUuid: e.target.value })}
                          className="w-full text-xs font-mono px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        />
                        {formErrors.deviceUuid && <p className="text-[10px] text-rose-500 mt-0.5">{formErrors.deviceUuid}</p>}
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Device Platform
                        </label>
                        <input
                          type="text"
                          value="Android (Android 14 / Kiosk)"
                          disabled
                          className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-800 text-slate-500"
                        />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Device IP Address (Optional)
                        </label>
                        <input
                          type="text"
                          placeholder="e.g. 192.168.1.50"
                          value={formData.ipAddress}
                          onChange={(e) => setFormData({ ...formData, ipAddress: e.target.value })}
                          className="w-full text-xs font-mono px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        />
                      </div>

                      <div>
                        <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                          Connection Type
                        </label>
                        <select
                          value={formData.connectionType}
                          onChange={(e) => setFormData({ ...formData, connectionType: e.target.value })}
                          className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                        >
                          <option value="Wi-Fi">Wi-Fi (Recommended)</option>
                          <option value="Ethernet / LAN">Ethernet / LAN</option>
                          <option value="Mobile Data">Mobile Data (4G / 5G)</option>
                        </select>
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                        Assigned Employee (Optional for Personal Mobile Check-in)
                      </label>
                      <select
                        value={formData.assignedEmployeeId}
                        onChange={(e) => setFormData({ ...formData, assignedEmployeeId: e.target.value })}
                        className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                      >
                        <option value="">-- Unassigned (Company Kiosk Mode) --</option>
                        {employees.map(emp => (
                          <option key={emp.id} value={emp.id}>
                            {emp.employeeCode} - {emp.fullName}
                          </option>
                        ))}
                      </select>
                    </div>
                  </>
                )}

                {/* Common Field: Company Location */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                      Company Location *
                    </label>
                    <input
                      type="text"
                      placeholder="e.g. Reception Desk / Main Entrance"
                      value={formData.location}
                      onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                      className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                    />
                    {formErrors.location && <p className="text-[10px] text-rose-500 mt-0.5">{formErrors.location}</p>}
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">
                      Device Status
                    </label>
                    <select
                      value={formData.status}
                      onChange={(e) => setFormData({ ...formData, status: e.target.value })}
                      className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                    >
                      <option value="DISCONNECTED">Disconnected</option>
                      <option value="CONNECTED">Connected</option>
                      <option value="INACTIVE">Inactive</option>
                    </select>
                  </div>
                </div>

                <div className="pt-4 flex items-center justify-end gap-3 border-t border-slate-100 dark:border-slate-800">
                  <button
                    type="button"
                    onClick={() => setShowAddModal(false)}
                    className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={savingDevice}
                    className="px-5 py-2 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white shadow-md shadow-primary-500/20 disabled:opacity-50 flex items-center gap-2"
                  >
                    {savingDevice ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <Check className="w-4 h-4" />}
                    Save Device Record
                  </button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* ========================================================================= */}
      {/* EDIT DEVICE MODAL */}
      {/* ========================================================================= */}
      <AnimatePresence>
        {showEditModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="glass-card w-full max-w-lg p-6 border border-slate-200 dark:border-slate-800 space-y-4 bg-white dark:bg-slate-900 rounded-3xl"
            >
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
                <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">
                  Edit Device: {showEditModal.deviceName}
                </h3>
                <button onClick={() => setShowEditModal(null)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              <form onSubmit={handleUpdateDevice} className="space-y-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">Device Name</label>
                  <input
                    type="text"
                    value={showEditModal.deviceName || ''}
                    onChange={(e) => setShowEditModal({ ...showEditModal, deviceName: e.target.value })}
                    className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">Model</label>
                    <input
                      type="text"
                      value={showEditModal.deviceModel || ''}
                      onChange={(e) => setShowEditModal({ ...showEditModal, deviceModel: e.target.value })}
                      className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">Location</label>
                    <input
                      type="text"
                      value={showEditModal.location || ''}
                      onChange={(e) => setShowEditModal({ ...showEditModal, location: e.target.value })}
                      className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">IP Address</label>
                    <input
                      type="text"
                      value={showEditModal.ipAddress || ''}
                      onChange={(e) => setShowEditModal({ ...showEditModal, ipAddress: e.target.value })}
                      className="w-full text-xs font-mono px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">Port</label>
                    <input
                      type="number"
                      value={showEditModal.port || ''}
                      onChange={(e) => setShowEditModal({ ...showEditModal, port: e.target.value })}
                      className="w-full text-xs font-mono px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">Connection Type</label>
                    <select
                      value={showEditModal.connectionType || 'Wi-Fi'}
                      onChange={(e) => setShowEditModal({ ...showEditModal, connectionType: e.target.value })}
                      className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                    >
                      <option value="LAN">LAN</option>
                      <option value="Wi-Fi">Wi-Fi</option>
                      <option value="USB">USB</option>
                      <option value="Mobile Data">Mobile Data</option>
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-1">Status</label>
                    <select
                      value={showEditModal.status || 'DISCONNECTED'}
                      onChange={(e) => setShowEditModal({ ...showEditModal, status: e.target.value })}
                      className="w-full text-xs px-3 py-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800"
                    >
                      <option value="CONNECTED">Connected</option>
                      <option value="DISCONNECTED">Disconnected</option>
                      <option value="INACTIVE">Inactive</option>
                    </select>
                  </div>
                </div>

                <div className="pt-3 flex items-center justify-end gap-2 border-t border-slate-100 dark:border-slate-800">
                  <button
                    type="button"
                    onClick={() => setShowEditModal(null)}
                    className="px-4 py-2 text-xs font-semibold rounded-xl border border-slate-200 dark:border-slate-700"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    disabled={savingDevice}
                    className="px-4 py-2 text-xs font-semibold rounded-xl bg-primary-600 hover:bg-primary-700 text-white"
                  >
                    Save Changes
                  </button>
                </div>
              </form>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* ========================================================================= */}
      {/* VIEW DEVICE MODAL */}
      {/* ========================================================================= */}
      <AnimatePresence>
        {showViewModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-xs">
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95 }}
              className="glass-card w-full max-w-md p-6 border border-slate-200 dark:border-slate-800 space-y-4 bg-white dark:bg-slate-900 rounded-3xl"
            >
              <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
                <div className="flex items-center gap-2">
                  {showViewModal.deviceType === 'MOBILE' ? (
                    <Smartphone className="w-5 h-5 text-purple-500" />
                  ) : (
                    <HardDrive className="w-5 h-5 text-cyan-500" />
                  )}
                  <div>
                    <h3 className="font-bold text-slate-900 dark:text-slate-100 text-sm">{showViewModal.deviceName}</h3>
                    <p className="text-[11px] text-slate-500">{showViewModal.deviceType === 'MOBILE' ? 'Mobile Android Device' : 'Biometric Sensor Terminal'}</p>
                  </div>
                </div>
                <button onClick={() => setShowViewModal(null)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="space-y-2 text-xs">
                <div className="flex justify-between p-2 rounded-lg bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Device ID / Serial:</span>
                  <span className="font-mono font-bold text-slate-900 dark:text-slate-100">{showViewModal.deviceSerialNumber || showViewModal.deviceUuid}</span>
                </div>
                <div className="flex justify-between p-2 rounded-lg bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Model:</span>
                  <span className="font-semibold">{showViewModal.manufacturer} {showViewModal.deviceModel}</span>
                </div>
                <div className="flex justify-between p-2 rounded-lg bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Network Address:</span>
                  <span className="font-mono">{showViewModal.ipAddress}:{showViewModal.port}</span>
                </div>
                <div className="flex justify-between p-2 rounded-lg bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Connection Protocol:</span>
                  <span>{showViewModal.communicationProtocol} ({showViewModal.connectionType})</span>
                </div>
                <div className="flex justify-between p-2 rounded-lg bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Assigned Location:</span>
                  <span className="font-semibold">{showViewModal.location}</span>
                </div>
                <div className="flex justify-between p-2 rounded-lg bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Current Status:</span>
                  <span>{getStatusBadge(showViewModal.status)}</span>
                </div>
                <div className="flex justify-between p-2 rounded-lg bg-slate-50 dark:bg-slate-800">
                  <span className="text-slate-500">Last Connected:</span>
                  <span className="font-mono">{showViewModal.lastSyncTime ? new Date(showViewModal.lastSyncTime).toLocaleString() : 'Never'}</span>
                </div>
              </div>

              <div className="pt-3 flex justify-end">
                <button
                  onClick={() => setShowViewModal(null)}
                  className="px-4 py-2 text-xs font-semibold rounded-xl bg-slate-100 dark:bg-slate-800 hover:bg-slate-200"
                >
                  Close
                </button>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>

      {/* Scanner Modals */}
      <FaceScannerModal
        isOpen={showFaceModal}
        onClose={() => setShowFaceModal(false)}
        onCapture={handleFaceEnrolled}
        referencePhotoUrl={selectedEmployee?.profilePhotoUrl || null}
        title={`Live Face Register: ${selectedEmployee?.fullName || ''}`}
        subtitle="Must be clear and match the employee's stored face photo"
      />

      <FingerprintScannerModal
        isOpen={showFpModal}
        onClose={() => setShowFpModal(false)}
        onScanComplete={handleFingerprintEnrolled}
        devices={devices}
        title={`Fingerprint Enrolment: ${selectedEmployee?.fullName || ''}`}
      />
    </div>
  );
}
