@echo off
echo ========================================
echo   Build and Deploy to S3
echo ========================================
echo.

cd /d "%~dp0"

echo [1/4] Cleaning previous build...
if exist "dist" rmdir /s /q dist

echo.
echo [2/4] Building for production...
call ng build --configuration production --base-href=/ --output-hashing=all

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo [3/4] Fixing file extensions...
cd dist\valorant-arena-frontend

if exist "index.html.html" (
    echo Fixing index.html.html...
    ren index.html.html index.html
)

if not exist "index.html" (
    echo ERROR: index.html not found!
    pause
    exit /b 1
)

echo.
echo [4/4] Files ready for upload:
dir /b

echo.
echo ========================================
echo   Build Complete!
echo ========================================
echo.
echo Now upload to S3:
echo aws s3 sync . s3://valorant-arena-frontend/ --delete --cache-control "public,max-age=31536000,immutable" --exclude "index.html"
echo aws s3 cp index.html s3://valorant-arena-frontend/index.html --cache-control "no-cache"
echo.
echo Or use AWS CLI:
echo cd dist\valorant-arena-frontend
echo aws s3 sync . s3://valorant-arena-frontend/ --delete
echo.
pause
