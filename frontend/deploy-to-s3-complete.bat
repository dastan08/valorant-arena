@echo off
echo ========================================
echo   Complete S3 Deployment Script
echo ========================================
echo.

cd /d "%~dp0"

echo Step 1: Verify environment configuration
echo ========================================
echo.
echo Checking environment.prod.ts...
findstr "apiUrl" src\app\core\config\environment.prod.ts
echo.

echo Step 2: Clean previous build
echo ========================================
if exist "dist" (
    echo Removing old dist folder...
    rmdir /s /q dist
)
echo.

echo Step 3: Build for production
echo ========================================
echo Building with production configuration...
call ng build --configuration production --base-href=/ --output-hashing=all

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    echo Please fix the build errors above.
    pause
    exit /b 1
)

echo.
echo Step 4: Verify build output
echo ========================================
cd dist\valorant-arena-frontend

if exist "index.html.html" (
    echo WARNING: Found index.html.html - Fixing...
    ren index.html.html index.html
)

if not exist "index.html" (
    echo ERROR: index.html not found!
    echo Build may have failed.
    pause
    exit /b 1
)

echo ✓ index.html found
echo.
echo Build files:
dir /b
echo.

echo Step 5: Upload to S3
echo ========================================
echo.
echo Uploading to S3 bucket: valorant-arena-frontend
echo.

REM Upload all files except index.html with caching
aws s3 sync . s3://valorant-arena-frontend/ --delete --exclude "index.html" --cache-control "public,max-age=31536000,immutable"

if %errorlevel% neq 0 (
    echo.
    echo ERROR: S3 sync failed!
    echo Make sure AWS CLI is configured: aws configure
    pause
    exit /b 1
)

REM Upload index.html without caching
aws s3 cp index.html s3://valorant-arena-frontend/index.html --cache-control "no-cache,no-store,must-revalidate"

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Failed to upload index.html!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Deployment Complete!
echo ========================================
echo.
echo Your site is available at:
echo http://valorant-arena-frontend.s3-website.ap-south-1.amazonaws.com
echo.
echo Backend API:
echo http://13.201.185.135:8080/api
echo.
echo Next steps:
echo 1. Clear browser cache (Ctrl+Shift+R)
echo 2. Open the S3 URL above
echo 3. Check browser console for any errors
echo.
pause
