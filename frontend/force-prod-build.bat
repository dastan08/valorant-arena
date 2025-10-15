@echo off
echo ========================================
echo   Force Production Build
echo ========================================
echo.

cd /d "%~dp0"

echo Checking environment files...
echo.
echo Development environment:
type src\app\core\config\environment.ts
echo.
echo.
echo Production environment:
type src\app\core\config\environment.prod.ts
echo.
echo.

pause
echo.
echo Cleaning...
if exist "dist" rmdir /s /q dist
if exist ".angular" rmdir /s /q .angular

echo.
echo Building with production configuration...
call ng build --configuration=production --base-href=/ --output-hashing=all

if %errorlevel% neq 0 (
    echo.
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Verifying build...
cd dist\valorant-arena-frontend

echo.
echo Checking if API URL is in the build:
findstr /s "13.201.185.135" *.js
if %errorlevel% equ 0 (
    echo ✓ API URL found in build!
) else (
    echo ✗ API URL NOT found in build!
    echo This means the production environment was not used.
    pause
    exit /b 1
)

echo.
echo Files ready to upload:
dir /b

echo.
echo ========================================
echo   Build Complete and Verified!
echo ========================================
echo.
echo Now upload to S3:
echo aws s3 sync . s3://valorant-arena-frontend/ --delete
echo.
pause
