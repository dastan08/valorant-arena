@echo off
echo ========================================
echo   Rebuild and Deploy to S3
echo ========================================
echo.

cd /d "%~dp0"

echo [1/5] Cleaning previous build...
if exist "dist" rmdir /s /q dist
if exist ".angular" rmdir /s /q .angular

echo.
echo [2/5] Verifying environment configuration...
echo.
echo Production environment (environment.prod.ts):
type src\app\core\config\environment.prod.ts | findstr "apiUrl"
echo.

echo [3/5] Building for production...
call ng build --configuration=production --base-href=/

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [4/5] Verifying build output...
cd dist\valorant-arena-frontend

echo Checking for EC2 IP in build...
findstr /s /c:"13.201.185.135" *.js > nul
if %errorlevel% equ 0 (
    echo ✓ EC2 IP found in build - Production environment is being used!
) else (
    echo ✗ EC2 IP NOT found in build!
    echo ✗ Production environment was NOT used!
    echo.
    echo This means the build is still using localhost.
    echo Check that angular.json has correct fileReplacements.
    pause
    exit /b 1
)

echo.
echo [5/5] Uploading to S3...
aws s3 sync . s3://valorant-arena-frontend/ --delete

if %errorlevel% neq 0 (
    echo.
    echo ERROR: S3 upload failed!
    echo Make sure AWS CLI is configured.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Deployment Complete!
echo ========================================
echo.
echo Your site: http://valorant-arena-frontend.s3-website.ap-south-1.amazonaws.com
echo.
echo Next steps:
echo 1. Clear browser cache (Ctrl+Shift+R)
echo 2. Open the S3 URL
echo 3. Check that tournaments load correctly
echo.
pause
