# Smart Employee Management & Biometric Attendance System

A production-grade, enterprise-scale Company Employee Management and Biometric Attendance Web Application engineered for corporate environments across desktop, laptop, tablet, and mobile devices.

The system features two independent panels:
- **Admin Panel**: Employee lifecycles, real-time biometric attendance monitoring, automated working hour calculations, custom deduction rules, monthly payslip generation with printable PDF vouchers, company holiday calendar, break audits, and global shift configuration.
- **Employee Panel**: Personal profile self-service (with strict privacy boundaries), real-time biometric punch (Face / Fingerprint / PIN), break tracker, personal attendance history, and monthly payslip downloads.

---

## 1. System Architecture & Tech Stack

```
[ React 18 + Vite + Tailwind CSS + Framer Motion ] (Frontend :3000)
                        │
                        ▼ (Axios + JWT Bearer Auth)
[ Spring Boot 3.2.5 (Java 21/17) + Spring Security 6 ] (Backend :8080)
   ├── Controller Layer (REST APIs)
   ├── Service Layer (Attendance Engine, Payroll Calculator)
   ├── Integration Layer (Biometric Device Gateway, OTP Simulators)
   ├── Security Layer (JWT Provider, Role Authorizer, Sensitive Masking)
   └── Repository Layer (Spring Data JPA / Hibernate)
                        │
                        ▼ (JDBC)
[ MySQL 8.0 / H2 In-Memory Database ] (:3306)
```

### Frontend Stack
- **Framework**: React 18 with Vite
- **Styling**: Tailwind CSS & Glassmorphism UI
- **Animations**: Framer Motion for micro-interactions, layout transitions, and modals
- **Icons**: Lucide React
- **Charts**: Recharts (Monthly attendance trends, department distributions)
- **HTTP Client**: Axios with automatic JWT interceptors and error handling
- **Routing**: React Router DOM v6 with role-based route guards

### Backend Stack
- **Language**: Java 21 / 17
- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security 6 with stateless JWT Bearer authentication
- **ORM / Persistence**: Spring Data JPA with Hibernate
- **Validation**: Jakarta Bean Validation
- **Database**: MySQL 8.0 (Production) & H2 (Zero-configuration dev mode)
- **Containerization**: Multi-stage Dockerfiles & Docker Compose

---

## 2. Directory Structure

```
smart-employee-management/
├── docker-compose.yml              # Multi-container orchestration (MySQL, Backend, Frontend)
├── .gitignore
├── README.md                       # Comprehensive system documentation
├── database/
│   ├── schema.sql                  # 20 normalized relational MySQL tables
│   └── seeds.sql                   # Realistic enterprise seed dataset
│
├── employee-management-backend/     # Spring Boot 3 Java Service
│   ├── Dockerfile                  # Multi-stage JDK build image
│   ├── pom.xml                     # Maven configuration & dependencies
│   ├── run-backend.ps1             # PowerShell launcher
│   ├── run-backend.bat             # Windows Batch launcher
│   └── src/
│       ├── main/
│       │   ├── java/com/company/employee_management/
│       │   │   ├── config/         # SecurityConfig, CorsFilter, AppConfig
│       │   │   ├── controller/     # 9 REST Controllers (Auth, Admin, Attendance, Payslip, etc.)
│       │   │   ├── dto/            # Request/Response Data Transfer Objects
│       │   │   ├── entity/         # 20 JPA Entities with relational mappings
│       │   │   ├── exception/      # GlobalExceptionHandler & custom exceptions
│       │   │   ├── integration/    # Biometric device gateway (ZKTeco/R307) & OTP services
│       │   │   ├── repository/     # Spring Data JPA Repositories
│       │   │   ├── security/       # JwtTokenProvider, UserPrincipal, CustomUserDetailsService
│       │   │   ├── service/        # Business logic services & interfaces
│       │   │   └── util/           # DataSeeder (auto-initializes sample records on boot)
│       │   └── resources/
│       │       ├── application.yml        # Dev profile with H2 in MySQL mode
│       │       └── application-mysql.yml  # Production MySQL profile
│
└── employee-management-frontend/    # React + Vite Web Application
    ├── Dockerfile                  # Multi-stage build with Nginx
    ├── nginx.conf                  # Nginx proxy configuration
    ├── package.json
    ├── tailwind.config.js
    ├── vite.config.js
    └── src/
        ├── assets/
        ├── components/
        │   ├── biometrics/         # FaceScannerModal & FingerprintScannerModal
        │   ├── common/             # StatsCard, Badge, StatusBadge, MethodBadge
        │   └── layout/             # AdminLayout, EmployeeLayout, Navbar, Sidebar
        ├── context/                # AuthContext (JWT state), ThemeContext (Dark/Light)
        ├── pages/
        │   ├── auth/               # AdminLogin, EmployeeLogin
        │   ├── admin/              # AdminDashboard, EmployeeManagement, AddEmployeeModal,
        │   │                       # BiometricManagement, AttendanceManagement, BreakManagement,
        │   │                       # PayslipManagement, HolidayManagement, MonthlyRecords, Settings
        │   └── employee/           # EmployeeDashboard, MyProfile, MyAttendance,
        │                           # MyBreaks, MyPayslips, EmployeeHolidays
        └── services/
            └── api.js              # Centralized Axios API client
```

---

## 3. Credentials for Local Development & Testing

The system is pre-loaded with realistic sample data and users:

### Administrator Account
| Field | Value |
|---|---|
| **Role** | System Administrator (`ROLE_ADMIN`) |
| **Login URL** | `http://localhost:3000/admin/login` |
| **Username** | `admin` |
| **Email** | `admin@apextech.com` |
| **Password** | `Admin@123` |

### Employee Accounts
| Employee ID | Name | Username / Email | Password | Department / Role |
|---|---|---|---|---|
| **EMP001** | John Doe | `john.doe@apextech.com` | `Employee@123` | Engineering (Principal Software Engineer) |
| **EMP002** | Sarah Jenkins | `sarah.j@apextech.com` | `Employee@123` | Engineering (Senior Frontend Engineer) |
| **EMP003** | David Kumar | `david.k@apextech.com` | `Employee@123` | Human Resources (HR Director) |
| **EMP004** | Elena Rostova | `elena.r@apextech.com` | `Employee@123` | Finance (Financial Controller) |
| **EMP005** | Marcus Vance | `marcus.v@apextech.com` | `Employee@123` | Product Design (Lead Product Designer) |
| **EMP006** | Priya Sharma | `priya.s@apextech.com` | `Employee@123` | Engineering (Senior Backend Engineer) |

*Both login screens feature convenient "Auto-Fill Demo Credentials" buttons for instant one-click login testing.*

---

## 4. Key Functional Features

### 4.1 Admin Panel
1. **Interactive Dashboard**:
   - Live metrics: Total Employees, Present Today, Late Today, Absent Today, On Leave, Half Day.
   - Animated count animations and hover effects.
   - Monthly attendance bar chart and departmental breakdown pie chart.
   - Quick attendance and employee registration activity feeds.
2. **Employee Lifecycle Management**:
   - Search, department/status filtering, pagination, column sorting, and CSV export.
   - Sensitive financial data masking (Aadhaar `XXXX-XXXX-1012`, Bank Account `XXXX9876`).
   - Registration wizard: Multi-step personal, contact, employment, financial, and biometric registration with SMS/Email OTP simulation.
   - Status toggle (Activate / Deactivate) and profile details inspection.
3. **Biometric Management & Device Gateway**:
   - Hardware device registry: ZKTeco uFace800, R307 Optical Sensor, SilkBio-101TC.
   - Device status monitoring, ping latency, and IP configuration.
   - Biometric enrollment tracking (Face templates and fingerprint slot indices).
4. **Attendance Monitoring**:
   - Real-time attendance ledger with check-in, check-out, working hours, and late minute counters.
   - Mark Absent automation for unmarked employees.
   - Admin audit correction modal (records admin identity and change reason).
   - Break details inspection with total break minutes and duration logs.
5. **Monthly Attendance Matrix (Grid View)**:
   - Day-by-day month view (1 to 31) showing status chips: `P` (Present), `A` (Absent), `L` (Late), `H` (Half Day), `LV` (Leave), `HL` (Holiday).
   - Total hours, total minutes, and attendance percentages per employee.
   - One-click CSV export.
6. **Configurable Payslip Management**:
   - Formula: `Net Payable Salary = Agreed Monthly Salary + Eligible Earnings - Applicable Deductions`.
   - Pro-rata absence deductions based on configured company shift rules.
   - Printable salary voucher / PDF preview with corporate logo, earnings breakdown, and audit sign-off.
7. **Holiday Management**:
   - Full calendar year scheduling with automatic consideration in attendance calculations.
8. **Company & Shift Settings**:
   - Configurable shift start time, shift end time, late arrival grace minutes, half-day threshold hours, and overtime rates.
   - Global toggle switches for Face, Fingerprint, and Password attendance.

### 4.2 Employee Portal
1. **Personal Dashboard**: Today's status chip, check-in/out timestamps, hours worked, and monthly attendance overview.
2. **Biometric Punch**:
   - **Face AI**: Interactive camera modal capturing face embeddings.
   - **Fingerprint**: Hardware sensor simulation verifying registered biometric template hashes.
   - **Password PIN**: Secure fallback punch option.
3. **Break Management**: Start and end tea breaks, lunch breaks, and short rests with live active timers.
4. **My Payslips**: Chronological list of finalized monthly payslips with instant print/PDF export.
5. **Company Holidays**: Upcoming holiday countdowns and full calendar year schedules.
6. **My Profile**: Self-service view with protected editability for contact details and profile picture.

---

## 5. Database Design (20 Relational Tables)

All tables include foreign key constraints, indexes on lookup keys, and audit timestamps:
1. `roles`: Role-based access control (`ROLE_ADMIN`, `ROLE_EMPLOYEE`).
2. `users`: Authentication credentials (username, email, BCrypt password hashes).
3. `departments`: Enterprise departments (ENG, HR, FIN, SALES, DES).
4. `designations`: Job titles mapped to departments.
5. `employees`: Core employee records, joining dates, salaries, masked identity attributes.
6. `employee_documents`: File attachments and verification documents.
7. `biometric_devices`: Hardware devices (ZKTeco, R307, SilkBio).
8. `biometric_registrations`: SHA-256 template hashes and biometric slot mappings.
9. `attendance`: Daily attendance summaries (hours, minutes, late minutes, status).
10. `attendance_events`: Immutable chronological punch log (IN / OUT events).
11. `employee_breaks`: Break start/end times and duration tracking.
12. `holidays`: Annual company holidays.
13. `leave_requests`: Formal leave applications.
14. `salary_structures`: Base salary rules.
15. `salary_components`: Allowances and deductions.
16. `payslips`: Finalized monthly payroll vouchers.
17. `company_settings`: Shift timings, grace periods, and verification flags.
18. `otp_verifications`: Phone and email OTP verification logs.
19. `audit_logs`: Administrative action audit trails.
20. `device_sync_logs`: Biometric device sync health and transactions.

---

## 6. How to Run the Application

### Option A: Local Development (Instant Out-of-the-Box)

The backend comes configured with an **in-memory H2 database** operating in MySQL compatibility mode. It automatically initializes all roles, settings, departments, designations, devices, holidays, employees, attendance history, and payslips via `DataSeeder.java`.

#### 1. Start the Spring Boot Backend (Port 8080)
Open a terminal in `employee-management-backend/`:
```powershell
# Using the bundled PowerShell launcher:
.\run-backend.ps1

# Or with Maven directly:
mvn spring-boot:run
```
*The backend starts at `http://localhost:8080` and H2 console is available at `http://localhost:8080/h2-console`.*

#### 2. Start the React Vite Frontend (Port 3000 / 5173)
Open a separate terminal in `employee-management-frontend/`:
```bash
# Install dependencies (already present in repository)
npm install

# Start Vite development server
npm run dev
```
Open your browser at `http://localhost:5173` or `http://localhost:3000`.

---

### Option B: Local Development with MySQL

1. Ensure MySQL is running on port 3306.
2. Create the database:
   ```sql
   CREATE DATABASE employee_management_db;
   ```
3. Run `database/schema.sql` and `database/seeds.sql`.
4. Run the backend with the `mysql` profile:
   ```powershell
   .\run-backend.ps1 -Profile mysql
   # Or
   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mysql"
   ```

---

### Option C: Complete Docker Deployment (Zero-Install)

Run the entire stack (MySQL 8.0, Spring Boot backend, and React/Nginx frontend) using Docker Compose:

```bash
docker-compose up --build
```

- **Frontend Application**: `http://localhost:3000`
- **Backend REST API**: `http://localhost:8080/api`
- **MySQL Database**: `localhost:3306` (Database: `employee_management_db`, User: `employee_user`, Password: `employee_password`)

To stop:
```bash
docker-compose down
```

---

## 7. Security Best Practices Implemented

- **BCrypt Password Hashing**: Passwords stored as one-way salt-hashed hashes (cost factor 10).
- **Stateless JWT**: Claims include username, user ID, role, and expiration timestamp.
- **Biometric Security Boundary**: Raw fingerprint images and face biometric photos are **never** stored or returned in APIs. Only irreversible cryptographically salted biometric template hashes (`SHA-256`) are kept.
- **Data Masking**: National identity (Aadhaar) and bank account numbers are masked at the API DTO layer (e.g. `XXXXXXXX1012`).
- **Role Isolation**: Employees cannot access any `/api/admin/*` endpoints or other employees' salary/attendance records. All endpoints enforce Spring Security `@PreAuthorize` checks.
- **Audit Trails**: Manual admin attendance adjustments require a documented rationale and record the modifying administrator's identity in `audit_logs`.
