@echo off
:: ============================================================
:: Company FIX Engine - Build Distribution Package
:: Creates a self-contained deployment in dist\FIXEngine\
:: ============================================================

setlocal enabledelayedexpansion

set "PROJECT_DIR=%~dp0"
set "DIST_DIR=%PROJECT_DIR%dist\FIXEngine"
set "JAVA_HOME=C:\Program Files\Java\jdk-22"
set "MAVEN_HOME=C:\Tools\apache-maven-3.9.11"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

echo ================================================
echo  Building Company FIX Engine Distribution
echo ================================================
echo.

:: Step 1: Maven build
echo [1/4] Building with Maven...
cd /d "%PROJECT_DIR%"
call mvn clean package -DskipTests -q
if %errorLevel% neq 0 (
    echo ERROR: Maven build failed.
    exit /b 1
)
echo       Maven build OK.

:: Step 2: Create app-image with jpackage
echo [2/4] Creating app-image with jpackage (embedded JRE)...

:: Prepare input directory for jpackage
if exist "%PROJECT_DIR%target\dist" rmdir /s /q "%PROJECT_DIR%target\dist"
mkdir "%PROJECT_DIR%target\dist"
copy "%PROJECT_DIR%target\fix-engine-1.0.0-SNAPSHOT.jar" "%PROJECT_DIR%target\dist\" >nul

:: Remove old app-image if exists
if exist "%PROJECT_DIR%target\app-image" rmdir /s /q "%PROJECT_DIR%target\app-image"

"%JAVA_HOME%\bin\jpackage" ^
  --type app-image ^
  --name FIXEngine ^
  --input "%PROJECT_DIR%target\dist" ^
  --main-jar fix-engine-1.0.0-SNAPSHOT.jar ^
  --java-options "-Xms256m" ^
  --java-options "-Xmx1024m" ^
  --dest "%PROJECT_DIR%target\app-image"

if %errorLevel% neq 0 (
    echo ERROR: jpackage failed.
    exit /b 1
)
echo       App-image created OK.

:: Step 3: Assemble distribution
echo [3/4] Assembling distribution...

if exist "%DIST_DIR%" rmdir /s /q "%DIST_DIR%"
mkdir "%DIST_DIR%"
mkdir "%DIST_DIR%\config"
mkdir "%DIST_DIR%\logs"
mkdir "%DIST_DIR%\store"
mkdir "%DIST_DIR%\data"

:: Copy app, runtime and native launcher from jpackage output
xcopy /s /e /q "%PROJECT_DIR%target\app-image\FIXEngine\app" "%DIST_DIR%\app\" >nul
xcopy /s /e /q "%PROJECT_DIR%target\app-image\FIXEngine\runtime" "%DIST_DIR%\runtime\" >nul
copy "%PROJECT_DIR%target\app-image\FIXEngine\FIXEngine.exe" "%DIST_DIR%\" >nul

:: Copy configuration files
copy "%PROJECT_DIR%src\main\resources\quickfix.cfg" "%DIST_DIR%\config\" >nul
copy "%PROJECT_DIR%src\main\resources\logback.xml" "%DIST_DIR%\config\" >nul

:: Copy service files
copy "%PROJECT_DIR%service\FIXEngineService.xml" "%DIST_DIR%\" >nul
copy "%PROJECT_DIR%service\install-service.bat" "%DIST_DIR%\" >nul
copy "%PROJECT_DIR%service\uninstall-service.bat" "%DIST_DIR%\" >nul

:: Copy WinSW (rename to FIXEngineService.exe)
if exist "%PROJECT_DIR%tools\WinSW-x64.exe" (
    copy "%PROJECT_DIR%tools\WinSW-x64.exe" "%DIST_DIR%\FIXEngineService.exe" >nul
    echo       WinSW copied OK.
) else (
    echo       WARNING: tools\WinSW-x64.exe not found.
    echo       Download it from: https://github.com/winsw/winsw/releases/download/v2.12.0/WinSW-x64.exe
    echo       Place it in: %PROJECT_DIR%tools\WinSW-x64.exe
)

echo       Distribution assembled OK.

:: Step 4: Verify
echo [4/4] Verifying distribution...
echo.

if exist "%DIST_DIR%\FIXEngine.exe" (
    echo  [OK] Native launcher found (FIXEngine.exe)
) else (
    echo  [FAIL] Native launcher NOT found
)

if exist "%DIST_DIR%\runtime" (
    echo  [OK] Embedded JRE found
) else (
    echo  [FAIL] Embedded JRE NOT found
)

if exist "%DIST_DIR%\app\fix-engine-1.0.0-SNAPSHOT.jar" (
    echo  [OK] Application JAR found
) else (
    echo  [FAIL] Application JAR NOT found
)

if exist "%DIST_DIR%\config\quickfix.cfg" (
    echo  [OK] Configuration files found
) else (
    echo  [FAIL] Configuration files NOT found
)

if exist "%DIST_DIR%\FIXEngineService.exe" (
    echo  [OK] WinSW service wrapper found
) else (
    echo  [WARN] WinSW not found - download manually
)

echo.
echo ================================================
echo  Distribution ready at: %DIST_DIR%
echo.
echo  To deploy:
echo    1. Copy dist\FIXEngine\ to target machine
echo    2. Edit config\quickfix.cfg with real values
echo    3. Run install-service.bat as Administrator
echo ================================================
echo.
pause
