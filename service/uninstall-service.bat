@echo off
:: ============================================================
:: Company FIX Engine - Uninstall Windows Service
:: Run as Administrator
:: ============================================================

net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ERROR: This script must be run as Administrator.
    echo Right-click and select "Run as administrator".
    pause
    exit /b 1
)

cd /d "%~dp0"

echo Stopping Company FIX Engine Service...
FIXEngineService.exe stop
timeout /t 5 /nobreak >nul

echo Uninstalling service...
FIXEngineService.exe uninstall
if %errorLevel% neq 0 (
    echo ERROR: Failed to uninstall service.
    pause
    exit /b 1
)

echo.
echo FIX Engine Service uninstalled successfully.
echo.
pause
