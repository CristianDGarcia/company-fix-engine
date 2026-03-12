@echo off
:: ============================================================
:: Company FIX Engine - Install Windows Service
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

echo Installing Company FIX Engine Service...
FIXEngineService.exe install
if %errorLevel% neq 0 (
    echo ERROR: Failed to install service.
    pause
    exit /b 1
)

echo Starting service...
FIXEngineService.exe start
if %errorLevel% neq 0 (
    echo WARNING: Service installed but failed to start.
    echo Check logs in the logs\ directory.
    pause
    exit /b 1
)

echo.
echo ================================================
echo  FIX Engine Service installed and started.
echo  Service Name: Company FIX Engine
echo  Status: Running
echo  Logs: %~dp0logs\
echo  Config: %~dp0config\quickfix.cfg
echo ================================================
echo.
pause
