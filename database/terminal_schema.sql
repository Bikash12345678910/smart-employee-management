-- ============================================================================
-- SMART FINGERPRINT & FACE EMPLOYEE ATTENDANCE MANAGEMENT SYSTEM
-- Shared POCO M2 Pro Terminal Schema Extension (MySQL 8+/9+)
-- Tables: employees, attendance, biometric_enrollments, attendance_audit_logs,
--         attendance_terminals, admin_users
-- ============================================================================

USE employee_management_db;

-- 1. Attendance Terminals Table (Shared POCO M2 Pro Kiosk Devices)
CREATE TABLE IF NOT EXISTS attendance_terminals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    terminal_code VARCHAR(64) NOT NULL UNIQUE,
    terminal_name VARCHAR(120) NOT NULL,
    device_model VARCHAR(100) NOT NULL DEFAULT 'Xiaomi POCO M2 Pro',
    location VARCHAR(150) NOT NULL DEFAULT 'Main Office Entrance',
    api_key_hash VARCHAR(255) NOT NULL,
    hmac_secret VARCHAR(255) NOT NULL,
    tap_timeout_ms INT NOT NULL DEFAULT 1400,
    face_match_threshold DECIMAL(5,4) NOT NULL DEFAULT 0.8200,
    liveness_required BOOLEAN NOT NULL DEFAULT TRUE,
    biometric_prompt_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    kiosk_mode_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_terminal_code (terminal_code)
);

-- 2. Biometric Enrollments Table (AES-256-GCM Encrypted Face Templates + Consent)
CREATE TABLE IF NOT EXISTS biometric_enrollments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    employee_code VARCHAR(50) NOT NULL,
    modality VARCHAR(30) NOT NULL DEFAULT 'FACE_CAMERAX', -- FACE_CAMERAX, FINGERPRINT_DEVICE
    encrypted_template TEXT NOT NULL, -- AES-256-GCM Base64 ciphertext (IV + Ciphertext + AuthTag)
    template_version VARCHAR(20) NOT NULL DEFAULT 'MLKIT_V1_28D',
    quality_score DECIMAL(5,4) DEFAULT 0.9000,
    consent_given BOOLEAN NOT NULL DEFAULT FALSE,
    consent_text_version VARCHAR(30) NOT NULL DEFAULT 'CONSENT_V1.0',
    consent_timestamp TIMESTAMP NOT NULL,
    enrolled_by_admin VARCHAR(100) NOT NULL DEFAULT 'admin',
    terminal_code VARCHAR(64) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_bio_enroll_emp FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY uq_emp_modality (employee_id, modality),
    INDEX idx_bio_enroll_code (employee_code)
);

-- 3. Attendance Audit Logs Table (Immutable Security & Replay Audit Trail)
CREATE TABLE IF NOT EXISTS attendance_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    terminal_code VARCHAR(64) NULL,
    employee_id BIGINT NULL,
    employee_code VARCHAR(50) NULL,
    action_type VARCHAR(50) NOT NULL, -- CHECK_IN, CHECK_OUT, FACE_ENROLL, FACE_REVOKE, ADMIN_LOGIN, REPLAY_BLOCKED, AUTH_FAILED
    verification_method VARCHAR(50) NULL, -- FACE_LIVENESS, FACE_AND_BIOMETRIC_PROMPT, ADMIN_JWT
    liveness_passed BOOLEAN NULL,
    match_score DECIMAL(5,4) NULL,
    status VARCHAR(30) NOT NULL, -- SUCCESS, FAILED, REJECTED
    failure_reason VARCHAR(255) NULL,
    request_nonce VARCHAR(100) NULL,
    client_ip VARCHAR(64) NULL,
    server_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_emp_code (employee_code),
    INDEX idx_audit_action (action_type),
    INDEX idx_audit_nonce (request_nonce),
    INDEX idx_audit_time (server_timestamp)
);

-- 4. Admin Users Table (Dedicated Terminal Admin Credentials Table)
CREATE TABLE IF NOT EXISTS admin_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_ADMIN',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed Default Shared POCO M2 Pro Terminal
INSERT INTO attendance_terminals (
    terminal_code, terminal_name, device_model, location,
    api_key_hash, hmac_secret, tap_timeout_ms, face_match_threshold,
    liveness_required, biometric_prompt_enabled, kiosk_mode_enabled, is_active
) VALUES (
    'POCO-M2-PRO-01',
    'POCO M2 Pro Shared Attendance Terminal',
    'Xiaomi POCO M2 Pro',
    'Main Office Reception',
    'POCO_TERMINAL_KEY_2026_SECURE',
    'POCO_HMAC_SECRET_9842_V1',
    1400,
    0.8200,
    TRUE,
    TRUE,
    TRUE,
    TRUE
) ON DUPLICATE KEY UPDATE
    terminal_name = VALUES(terminal_name),
    is_active = TRUE;

-- Seed Admin Users (Synced with main system Admin@123)
INSERT INTO admin_users (username, email, full_name, password_hash, role, is_active) VALUES
('admin', 'admin@apextech.com', 'System Administrator', '$2a$10$891Tdh0IlhRIaQgXaW0aEeL6dXc5DoCumlipLFrkvPRrGTT0O0sVC', 'ROLE_ADMIN', TRUE),
('admin2', 'admin2@apextech.com', 'HR Operations Admin', '$2a$10$891Tdh0IlhRIaQgXaW0aEeL6dXc5DoCumlipLFrkvPRrGTT0O0sVC', 'ROLE_ADMIN', TRUE)
ON DUPLICATE KEY UPDATE is_active = TRUE;
