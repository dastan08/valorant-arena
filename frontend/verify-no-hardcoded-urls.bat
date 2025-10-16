@echo off
echo ========================================
echo   Verifying No Hardcoded URLs
echo ========================================
echo.

cd /d "%~dp0"

echo Checking for localhost:8080...
findstr /s /n /i "localhost:8080" src\app\*.ts 2>nul
if %errorlevel% equ 0 (
    echo.
    echo ✗ Found localhost:8080 references!
    set HAS_ERRORS=1
) else (
    echo ✓ No localhost:8080 found
)

echo.
echo Checking for hardcoded EC2 IP in TypeScript files...
findstr /s /n "13.201.185.135:8080" src\app\*.ts 2>nul | findstr /v "environment.ts environment.prod.ts app.module.ts" 2>nul
if %errorlevel% equ 0 (
    echo.
    echo ✗ Found hardcoded EC2 IP!
    set HAS_ERRORS=1
) else (
    echo ✓ No hardcoded EC2 IP found (except in environment files)
)

echo.
echo ========================================
if defined HAS_ERRORS (
    echo   ✗ FAILED - Fix hardcoded URLs above
    echo ========================================
    pause
    exit /b 1
) else (
    echo   ✓ PASSED - Ready to build!
    echo ========================================
    echo.
    echo Next steps:
    echo   1. ng build --configuration production
    echo   2. cd dist\valorant-arena-frontend
    echo   3. aws s3 sync . s3://valorant-arena-frontend/ --delete
    echo.
    pause
)
