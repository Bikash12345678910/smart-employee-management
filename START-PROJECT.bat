@echo off
title Smart Employee Management System - Launcher
echo ============================================================
echo   Smart Employee Management ^& Biometric Attendance System
echo ============================================================
echo.

REM 1. Set Java 21 Home
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1"
set "PATH=%JAVA_HOME%\bin;%PATH%"

REM 2. Start MySQL Service (if not already running)
echo [1/3] Checking MySQL service...
net start MySQL97 >nul 2>&1
if %errorlevel%==0 (
    echo       MySQL97 started.
) else (
    echo       MySQL97 is already running (or start MySQL manually if needed).
)

REM 3. Start Spring Boot Backend in a new window
echo [2/3] Starting Spring Boot Backend (Port 8080)...
start "Smart Employee Backend (8080)" cmd /k "set JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1&& cd /d "%~dp0employee-management-backend" && mvnw.cmd spring-boot:run"

REM 4. Start React Vite Frontend in a new window
echo [3/3] Starting React Frontend (Port 3000)...
start "Smart Employee Frontend (3000)" cmd /k "cd /d "%~dp0employee-management-frontend" && if not exist node_modules (npm install) && npm run dev"

echo.
echo Waiting 10 seconds for servers to initialize...
timeout /t 10 /nobreak >nul
start http://localhost:3000

echo.
echo ============================================================
echo   Frontend : http://localhost:3000
echo   Backend  : http://localhost:8080
echo   Admin    : admin / Admin@123  (or admin2 / Admin@123)
echo   Employee : biswanath.m / Employee@123
echo ============================================================
pause
