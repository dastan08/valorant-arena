@echo off
echo ========================================
echo   Fix S3 Upload - Ensure Correct Files
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] Cleaning previous build...
if exist "dist" rmdir /s /q dist

echo.
echo [2/3] Building with production configuration...
call ng build --configuration production --base-href=/

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [3/3] Verifying and fixing build output...
cd dist\valorant-arena-frontend

echo.
echo Files to upload:
dir /b

echo.
echo Checking for index.html...
if exist "index.html.html" (
    echo ✗ Found index.html.html - Fixing double extension...
    ren index.html.html index.html
    echo ✓ Renamed to index.html
)

if exist "index.html" (
    echo ✓ index.html found
) else (
    echo ✗ index.html NOT found!
    echo This will cause issues!
)

echo.
echo ========================================
echo   Build Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Navigate to: dist\valorant-arena-frontend
echo 2. Upload ALL files to S3:
echo    aws s3 sync . s3://your-bucket-name/ --delete
echo.
echo Or use AWS Console:
echo 1. Select all files in dist\valorant-arena-frontend
echo 2. Drag and drop to S3 bucket
echo.
pause
