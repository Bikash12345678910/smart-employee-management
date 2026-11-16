-- ========================================================
-- Smart Employee Management & Biometric Attendance System
-- Relational Database Schema (MySQL 8.0 Compatible)
-- ========================================================

CREATE DATABASE IF NOT EXISTS employee_management_db;
USE employee_management_db;

-- 1. Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Users Table (Authentication & Access)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_login TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

-- 3. Departments Table
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Designations Table
CREATE TABLE IF NOT EXISTS designations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    department_id BIGINT NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_designations_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
);

-- 5. Employees Table
CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(25) NOT NULL UNIQUE,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    profile_photo_url TEXT,
    
    -- Employment Details
    department_id BIGINT NOT NULL,
    designation_id BIGINT NOT NULL,
    joining_date DATE NOT NULL,
    employment_status VARCHAR(30) DEFAULT 'ACTIVE', -- ACTIVE, PROBATION, SUSPENDED, TERMINATED
    agreed_monthly_salary DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    
    -- Sensitive Financial / Identity (Protected & Masked at API layer)
    aadhaar_number VARCHAR(20) NOT NULL,
    pan_number VARCHAR(20) NOT NULL,
    bank_account_number VARCHAR(40) NOT NULL,
    bank_ifsc_code VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100),
    
    -- Verification Flags
    is_phone_verified BOOLEAN DEFAULT FALSE,
    is_email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_employees_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_employees_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    CONSTRAINT fk_employees_desig FOREIGN KEY (designation_id) REFERENCES designations(id) ON DELETE RESTRICT,
    INDEX idx_emp_code (employee_code),
    INDEX idx_emp_email (email),
    INDEX idx_emp_status (employment_status)
);

-- 6. Employee Documents Table
CREATE TABLE IF NOT EXISTS employee_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL, -- AADHAAR, PAN, RESUME, OFFER_LETTER, CONTRACT
    document_name VARCHAR(255) NOT NULL,
    file_path TEXT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_emp_docs FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- 7. Biometric Devices Table (Hardware Device Gateway Integration)
CREATE TABLE IF NOT EXISTS biometric_devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_name VARCHAR(100) NOT NULL,
    device_type VARCHAR(50) DEFAULT 'BIOMETRIC', -- BIOMETRIC, MOBILE
    device_model VARCHAR(50) NOT NULL, -- ZKTECO_UFACE800, R307_UART_ESP32, REALAND_A_F031, MOBILE_KIOSK
    device_platform VARCHAR(50) NULL, -- Android, iOS, Embedded Linux
    device_serial_number VARCHAR(100) NOT NULL UNIQUE,
    ip_address VARCHAR(45) NOT NULL,
    port INT NOT NULL DEFAULT 4370,
    communication_protocol VARCHAR(50) DEFAULT 'TCP/IP',
    location VARCHAR(100) NOT NULL,
    assigned_employee_id BIGINT NULL,
    assigned_employee_name VARCHAR(100) NULL,
    device_username VARCHAR(100) NULL,
    device_password VARCHAR(255) NULL,
    status VARCHAR(20) DEFAULT 'ONLINE', -- ONLINE, OFFLINE, MAINTENANCE, CONNECTED, DISCONNECTED
    last_sync_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Biometric Registrations Table
CREATE TABLE IF NOT EXISTS biometric_registrations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    biometric_type VARCHAR(30) NOT NULL, -- FACE, FINGERPRINT
    device_id BIGINT NULL,
    template_hash VARCHAR(255) NOT NULL, -- Secure one-way hashed biometric template
    face_sample_data TEXT NULL, -- Live captured preview reference/hash
    fingerprint_slot INT NULL, -- Sensor memory slot if applicable
    is_active BOOLEAN DEFAULT TRUE,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_biometric_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_biometric_device FOREIGN KEY (device_id) REFERENCES biometric_devices(id) ON DELETE SET NULL,
    INDEX idx_biometric_emp_type (employee_id, biometric_type)
);

-- 9. Attendance Table
CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time TIME NULL,
    check_out_time TIME NULL,
    working_hours INT DEFAULT 0,
    working_minutes INT DEFAULT 0,
    attendance_status VARCHAR(30) NOT NULL, -- PRESENT, ABSENT, LATE, HALF_DAY, ON_LEAVE, HOLIDAY
    attendance_via VARCHAR(30) DEFAULT 'MANUAL', -- FACE_RECOGNITION, FINGERPRINT, PASSWORD, MANUAL
    break_count INT DEFAULT 0,
    total_break_minutes INT DEFAULT 0,
    late_minutes INT DEFAULT 0,
    early_departure_minutes INT DEFAULT 0,
    is_manual_correction BOOLEAN DEFAULT FALSE,
    correction_reason TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_emp_date UNIQUE (employee_id, attendance_date),
    CONSTRAINT fk_attendance_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    INDEX idx_att_date (attendance_date),
    INDEX idx_att_status (attendance_status)
);

-- 10. Attendance Events Table (Raw Punch Logs from Biometrics / Manual)
CREATE TABLE IF NOT EXISTS attendance_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    event_time TIMESTAMP NOT NULL,
    event_type VARCHAR(20) NOT NULL, -- CHECK_IN, CHECK_OUT
    punch_method VARCHAR(30) NOT NULL, -- FACE, FINGERPRINT, PASSWORD, ADMIN_MANUAL
    device_id BIGINT NULL,
    verification_score DECIMAL(5, 2) DEFAULT 100.00,
    device_log_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    CONSTRAINT fk_event_device FOREIGN KEY (device_id) REFERENCES biometric_devices(id) ON DELETE SET NULL
);

-- 11. Employee Breaks Table
CREATE TABLE IF NOT EXISTS employee_breaks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendance_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    break_date DATE NOT NULL,
    break_start_time TIME NOT NULL,
    break_end_time TIME NULL,
    duration_minutes INT DEFAULT 0,
    break_reason VARCHAR(100) DEFAULT 'TEA/LUNCH',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_breaks_attendance FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE,
    CONSTRAINT fk_breaks_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- 12. Holidays Table
CREATE TABLE IF NOT EXISTS holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    holiday_name VARCHAR(100) NOT NULL,
    holiday_date DATE NOT NULL UNIQUE,
    holiday_day VARCHAR(20) NOT NULL,
    description TEXT,
    year INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. Leave Requests Table
CREATE TABLE IF NOT EXISTS leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(50) NOT NULL, -- CASUAL, SICK, PAID, UNPAID
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- 14. Salary Structures Table
CREATE TABLE IF NOT EXISTS salary_structures (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL UNIQUE,
    basic_salary DECIMAL(12, 2) NOT NULL,
    hra DECIMAL(12, 2) DEFAULT 0.00,
    special_allowance DECIMAL(12, 2) DEFAULT 0.00,
    conveyance DECIMAL(12, 2) DEFAULT 0.00,
    medical_allowance DECIMAL(12, 2) DEFAULT 0.00,
    provident_fund DECIMAL(12, 2) DEFAULT 0.00,
    professional_tax DECIMAL(12, 2) DEFAULT 0.00,
    tds DECIMAL(12, 2) DEFAULT 0.00,
    net_monthly_salary DECIMAL(12, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_salary_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- 15. Salary Components Table (Configurable Rules)
CREATE TABLE IF NOT EXISTS salary_components (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    component_name VARCHAR(100) NOT NULL,
    component_type VARCHAR(20) NOT NULL, -- EARNING, DEDUCTION
    calculation_type VARCHAR(20) NOT NULL, -- PERCENTAGE, FIXED
    rate_value DECIMAL(8, 2) NOT NULL,
    is_taxable BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE
);

-- 16. Payslips Table
CREATE TABLE IF NOT EXISTS payslips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    payslip_number VARCHAR(50) NOT NULL UNIQUE,
    salary_month INT NOT NULL,
    salary_year INT NOT NULL,
    agreed_monthly_salary DECIMAL(12, 2) NOT NULL,
    
    total_working_days INT NOT NULL DEFAULT 30,
    present_days INT NOT NULL DEFAULT 0,
    absent_days INT NOT NULL DEFAULT 0,
    leave_days INT NOT NULL DEFAULT 0,
    late_minutes INT NOT NULL DEFAULT 0,
    total_working_hours DECIMAL(6, 2) NOT NULL DEFAULT 0.00,
    
    gross_earnings DECIMAL(12, 2) NOT NULL,
    total_deductions DECIMAL(12, 2) NOT NULL,
    net_payable_salary DECIMAL(12, 2) NOT NULL,
    
    status VARCHAR(20) DEFAULT 'FINALIZED', -- DRAFT, FINALIZED, PAID
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_date DATE NULL,
    
    CONSTRAINT uq_emp_month_year UNIQUE (employee_id, salary_month, salary_year),
    CONSTRAINT fk_payslip_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    INDEX idx_payslip_period (salary_year, salary_month)
);

-- 17. Company Settings Table
CREATE TABLE IF NOT EXISTS company_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    company_email VARCHAR(150) NOT NULL,
    contact_number VARCHAR(30) NOT NULL,
    company_address TEXT NOT NULL,
    company_logo_url TEXT,
    
    -- Shift Configuration
    shift_start_time TIME NOT NULL DEFAULT '09:00:00',
    shift_end_time TIME NOT NULL DEFAULT '18:00:00',
    late_arrival_grace_minutes INT NOT NULL DEFAULT 15,
    half_day_threshold_hours INT NOT NULL DEFAULT 4,
    full_day_working_hours INT NOT NULL DEFAULT 8,
    weekly_off_days VARCHAR(100) DEFAULT 'SUNDAY',
    
    -- Attendance Policy
    enable_face_attendance BOOLEAN DEFAULT TRUE,
    enable_fingerprint_attendance BOOLEAN DEFAULT TRUE,
    enable_password_attendance BOOLEAN DEFAULT TRUE,
    allow_manual_attendance BOOLEAN DEFAULT TRUE,
    max_breaks_allowed INT DEFAULT 3,
    max_break_minutes INT DEFAULT 60,
    
    -- Salary Calculation Policy
    standard_working_days INT DEFAULT 26,
    leave_deduction_rate VARCHAR(20) DEFAULT 'PRO_RATA',
    overtime_rate_per_hour DECIMAL(8, 2) DEFAULT 150.00,
    currency_code VARCHAR(10) DEFAULT 'INR',
    currency_symbol VARCHAR(10) DEFAULT '₹',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 18. OTP Verifications Table
CREATE TABLE IF NOT EXISTS otp_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_identifier VARCHAR(100) NOT NULL, -- phone or email
    otp_code VARCHAR(10) NOT NULL,
    otp_type VARCHAR(20) NOT NULL, -- PHONE, EMAIL
    is_verified BOOLEAN DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_target (target_identifier, otp_type)
);

-- 19. Audit Logs Table
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    performed_by VARCHAR(100) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    entity_name VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NULL,
    details TEXT,
    ip_address VARCHAR(45) NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_time (timestamp)
);

-- 20. Device Sync Logs Table
CREATE TABLE IF NOT EXISTS device_sync_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id BIGINT NOT NULL,
    sync_status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED
    records_synced INT DEFAULT 0,
    error_message TEXT NULL,
    synced_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sync_device FOREIGN KEY (device_id) REFERENCES biometric_devices(id) ON DELETE CASCADE
);
