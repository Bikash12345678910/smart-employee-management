@echo off
title Smart Employee Attendance & Biometric System
echo =========================================================
echo   Starting Smart Employee Attendance & Biometric System
echo =========================================================
echo.

if exist "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" (
    start "" "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe" --app="http://localhost:3000"
) else (
    if exist "C:\Program Files\Google\Chrome\Application\chrome.exe" (
        start "" "C:\Program Files\Google\Chrome\Application\chrome.exe" --app="http://localhost:3000"
    ) else (
        start "" "http://localhost:3000"
    )
)

echo App launched!
exit
