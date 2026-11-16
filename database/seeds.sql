-- ========================================================
-- Seed Data for Smart Employee Management & Biometric System
-- ========================================================

USE employee_management_db;

-- Insert Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Company Administrator with full privileges'),
(2, 'ROLE_EMPLOYEE', 'Standard Employee Portal access')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Insert Company Settings
INSERT INTO company_settings (id, company_name, company_email, contact_number, company_address, shift_start_time, shift_end_time, late_arrival_grace_minutes, half_day_threshold_hours, full_day_working_hours, weekly_off_days, enable_face_attendance, enable_fingerprint_attendance, enable_password_attendance, allow_manual_attendance, standard_working_days, leave_deduction_rate, overtime_rate_per_hour, currency_code, currency_symbol)
VALUES (1, 'Apex Global Technologies Inc.', 'admin@apextech.com', '+1 (800) 555-0199', '100 Tech Park Boulevard, Suite 500, Silicon City, CA 94025', '09:00:00', '18:00:00', 15, 4, 8, 'SUNDAY', TRUE, TRUE, TRUE, TRUE, 26, 'PRO_RATA', 200.00, 'INR', '₹')
ON DUPLICATE KEY UPDATE company_name=VALUES(company_name), currency_code=VALUES(currency_code), currency_symbol=VALUES(currency_symbol);

-- Insert Departments
INSERT INTO departments (id, department_code, name, description) VALUES
(1, 'ENG', 'Engineering & Technology', 'Software development, DevOps, and cloud systems'),
(2, 'HR', 'Human Resources', 'Talent acquisition, employee welfare, and operations'),
(3, 'FIN', 'Finance & Accounting', 'Payroll, taxation, and company financial audit'),
(4, 'SALES', 'Sales & Enterprise Solutions', 'Client relations, business development, and revenue'),
(5, 'DES', 'Product & UI/UX Design', 'Visual identity, design systems, and interaction design')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Insert Designations
INSERT INTO designations (id, title, department_id, description) VALUES
(1, 'Principal Software Engineer', 1, 'Technical architecture and core platform engineering'),
(2, 'Senior Frontend Engineer', 1, 'React, web UI components, and state management'),
(3, 'Senior Backend Engineer', 1, 'Java Spring Boot microservices, API architecture'),
(4, 'DevOps & SRE Specialist', 1, 'CI/CD, Docker container orchestration, and monitoring'),
(5, 'HR Director', 2, 'Strategic leadership in human resources'),
(6, 'Talent Acquisition Partner', 2, 'Technical recruiting and campus interviews'),
(7, 'Financial Controller', 3, 'Treasury, compliance, and corporate tax'),
(8, 'Enterprise Account Executive', 4, 'B2B enterprise client acquisition'),
(9, 'Lead Product Designer', 5, 'User journey mapping, design systems, and prototyping')
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Insert Biometric Hardware Devices (1 Biometric Demo Device + 1 Mobile Demo Device)
INSERT INTO biometric_devices (id, device_name, device_type, device_model, device_platform, device_serial_number, ip_address, port, communication_protocol, location, status) VALUES
(1, 'ZKTeco BioStation Pro (Demo)', 'BIOMETRIC', 'ZKTeco uFace800', 'Embedded Linux', 'ZK-UF800-98421', '192.168.1.120', 4370, 'TCP/IP', 'Building A - Ground Floor Turnstile', 'ONLINE'),
(4, 'Samsung Galaxy Kiosk Tab (Demo)', 'MOBILE', 'Galaxy Tab A9', 'Android', 'SAM-TAB-A9-DEMO-01', '192.168.1.50', 8080, 'HTTP/REST', 'Reception Kiosk Counter', 'ONLINE')
ON DUPLICATE KEY UPDATE device_name=VALUES(device_name);

-- Insert Holidays for 2026
INSERT INTO holidays (id, holiday_name, holiday_date, holiday_day, description, year, is_active) VALUES
(1, 'New Year Day', '2026-01-01', 'Thursday', 'Global New Year Celebration', 2026, TRUE),
(2, 'Republic Day', '2026-01-26', 'Monday', 'National Holiday', 2026, TRUE),
(3, 'Holi (Festival of Colours)', '2026-03-04', 'Wednesday', 'Spring Festival', 2026, TRUE),
(4, 'Good Friday', '2026-04-03', 'Friday', 'Easter Weekend Holiday', 2026, TRUE),
(5, 'Labour Day', '2026-05-01', 'Friday', 'International Workers Day', 2026, TRUE),
(6, 'Independence Day', '2026-08-15', 'Saturday', 'National Independence Day', 2026, TRUE),
(7, 'Gandhi Jayanti', '2026-10-02', 'Friday', 'Mahatma Gandhi Birth Anniversary', 2026, TRUE),
(8, 'Diwali (Deepavali)', '2026-11-08', 'Sunday', 'Festival of Lights', 2026, TRUE),
(9, 'Christmas Day', '2026-12-25', 'Friday', 'Christmas Celebration', 2026, TRUE)
ON DUPLICATE KEY UPDATE holiday_name=VALUES(holiday_name);

-- Insert Users
-- Passwords:
-- admin: Admin@123 -> $2a$10$wEeV0q0l8yYl9g43U4o13u5wYwP6W3n5bW3r5n7e9l1b3v5x7z9m
-- employees: Employee@123 -> $2a$10$wEeV0q0l8yYl9g43U4o13u5wYwP6W3n5bW3r5n7e9l1b3v5x7z9m
INSERT INTO users (id, username, email, password_hash, role_id, is_active) VALUES
(1, 'admin', 'admin@apextech.com', '$2a$10$tZ2h.B8rV0s7l9V2K6dYEuSgP4P8eE5f3b7u1x9c5z7m3v5b8w1l.', 1, TRUE),
(11, 'admin2', 'admin2@apextech.com', '$2a$10$tZ2h.B8rV0s7l9V2K6dYEuSgP4P8eE5f3b7u1x9c5z7m3v5b8w1l.', 1, TRUE),
(2, 'john.doe', 'john.doe@apextech.com', '$2a$10$tZ2h.B8rV0s7l9V2K6dYEuSgP4P8eE5f3b7u1x9c5z7m3v5b8w1l.', 2, TRUE),
(3, 'sarah.j', 'sarah.j@apextech.com', '$2a$10$tZ2h.B8rV0s7l9V2K6dYEuSgP4P8eE5f3b7u1x9c5z7m3v5b8w1l.', 2, TRUE),
(4, 'david.k', 'david.k@apextech.com', '$2a$10$tZ2h.B8rV0s7l9V2K6dYEuSgP4P8eE5f3b7u1x9c5z7m3v5b8w1l.', 2, TRUE),
(5, 'elena.r', 'elena.r@apextech.com', '$2a$10$tZ2h.B8rV0s7l9V2K6dYEuSgP4P8eE5f3b7u1x9c5z7m3v5b8w1l.', 2, TRUE),
(6, 'marcus.v', 'marcus.v@apextech.com', '$2a$10$tZ2h.B8rV0s7l9V2K6dYEuSgP4P8eE5f3b7u1x9c5z7m3v5b8w1l.', 2, TRUE),
(7, 'priya.s', 'priya.s@apextech.com', '$2a$10$tZ2h.B8rV0s7l9V2K6dYEuSgP4P8eE5f3b7u1x9c5z7m3v5b8w1l.', 2, TRUE),
(10, 'biswanath.m', 'biswanath.m@apextech.com', '$2a$10$tZ2h.B8rV0s7l9V2K6dYEuSgP4P8eE5f3b7u1x9c5z7m3v5b8w1l.', 2, TRUE)
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- Insert Employees
INSERT INTO employees (id, employee_code, user_id, first_name, last_name, email, phone_number, date_of_birth, gender, address, profile_photo_url, department_id, designation_id, joining_date, employment_status, agreed_monthly_salary, aadhaar_number, pan_number, bank_account_number, bank_ifsc_code, bank_name, is_phone_verified, is_email_verified, is_active) VALUES
(1, 'EMP001', 2, 'John', 'Doe', 'john.doe@apextech.com', '+1 (555) 234-5678', '1990-05-14', 'MALE', '452 Silicon Ave, Apt 4B, San Jose, CA', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80', 1, 1, '2022-03-01', 'ACTIVE', 8500.00, '987456321012', 'ABCDE1234F', '98765432109876', 'HDFC0001234', 'HDFC Bank', TRUE, TRUE, TRUE),
(2, 'EMP002', 3, 'Sarah', 'Jenkins', 'sarah.j@apextech.com', '+1 (555) 345-6789', '1993-08-22', 'FEMALE', '120 Market St, San Francisco, CA', 'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80', 1, 2, '2022-06-15', 'ACTIVE', 7200.00, '876543210987', 'BCDEF2345G', '87654321098765', 'ICIC0002345', 'ICICI Bank', TRUE, TRUE, TRUE),
(3, 'EMP003', 4, 'David', 'Kumar', 'david.k@apextech.com', '+1 (555) 456-7890', '1988-11-30', 'MALE', '78 Oakwood Dr, Palo Alto, CA', 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80', 2, 5, '2021-01-10', 'ACTIVE', 6800.00, '765432109876', 'CDEFG3456H', '76543210987654', 'SBIN0003456', 'State Bank', TRUE, TRUE, TRUE),
(4, 'EMP004', 5, 'Elena', 'Rostova', 'elena.r@apextech.com', '+1 (555) 567-8901', '1991-02-18', 'FEMALE', '330 Fremont St, San Francisco, CA', 'https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80', 3, 7, '2022-09-01', 'ACTIVE', 7500.00, '654321098765', 'DEFGH4567J', '65432109876543', 'CITI0004567', 'Citibank', TRUE, TRUE, TRUE),
(5, 'EMP005', 6, 'Marcus', 'Vance', 'marcus.v@apextech.com', '+1 (555) 678-9012', '1994-09-05', 'MALE', '210 Mission St, San Francisco, CA', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150&auto=format&fit=crop&q=80', 5, 9, '2023-02-01', 'ACTIVE', 6500.00, '543210987654', 'EFGHI5678K', '54321098765432', 'BARC0005678', 'Barclays', TRUE, TRUE, TRUE),
(6, 'EMP006', 7, 'Priya', 'Sharma', 'priya.s@apextech.com', '+1 (555) 789-0123', '1992-12-10', 'FEMALE', '89 Castro St, Mountain View, CA', 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=150&auto=format&fit=crop&q=80', 1, 3, '2023-04-15', 'ACTIVE', 7400.00, '432109876543', 'FGHIJ6789L', '43210987654321', 'WELL0006789', 'Wells Fargo', TRUE, TRUE, TRUE),
(9, 'EMP007', 10, 'Biswanath', 'Manna', 'biswanath.m@apextech.com', '+1 (555) 999-8877', '1990-01-15', 'MALE', '77 Central Avenue, Tech District, CA', 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&auto=format&fit=crop&q=80', 1, 1, '2022-01-01', 'ACTIVE', 15000.00, '123456789012', 'AABBM1234C', '11223344556677', 'HDFC0005678', 'HDFC Bank', TRUE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE employee_code=VALUES(employee_code);

-- Biometric Registrations
INSERT INTO biometric_registrations (id, employee_id, device_id, biometric_type, template_hash, fingerprint_slot, is_active) VALUES
(1, 1, 1, 'FACE', 'f8a7e4b9c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8', NULL, TRUE),
(2, 1, 1, 'FINGERPRINT', 'a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2', 1, TRUE),
(3, 2, 1, 'FACE', 'c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4', NULL, TRUE),
(4, 2, 1, 'FINGERPRINT', 'e5f6a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6', 2, TRUE),
(5, 3, 2, 'FINGERPRINT', '7f8a9b0c1d2e3f4a5b6c7d8e9f0a1b2c3d4e5f6a7b8c9d0e1f2a3b4c5d6e7f8a', 3, TRUE)
ON DUPLICATE KEY UPDATE id=VALUES(id);
