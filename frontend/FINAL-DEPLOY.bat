@echo off
echo ========================================
echo   VALORANT ARENA - FINAL DEPLOYMENT
echo ========================================
echo.

cd /d "%~dp0"

echo [Step 1/6] Checking for hardcoded URLs...
echo.
findstr /s /n /i "localhost:8080" src\*.ts src\*.html 2>nul
if %errorlevel% equ 0 (
    echo.
    echo ERROR: Found localhost references!
    echo Please fix these before deploying.
    pause
    exit /b 1
)
echo ✓ No localhost references found
echo.

echo [Step 2/6] Cleaning previous build...
if exist "dist" rmdir /s /q dist
if exist ".angular" rmdir /s /q .angular
echo ✓
