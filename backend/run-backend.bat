@echo off
echo ========================================
echo   Valorant Arena Backend Startup
echo ========================================
echo.

REM Navigate to backend directory
cd /d "%~dp0"

echo [1/3] Checking MySQL connection...
mysql -u root -pabcd -e "CREATE DATABASE IF NOT EXISTS valorant_arena;" 2>nul
if %errorlevel% neq 0 (
    echo WARNING: Could not connect to MySQL with default password 'abcd'
    echo Please make sure MySQL is running and update the password if needed
    echo.
)

echo [2/3] Building backend...
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo [3/3] Starting backend server...
echo Backend will be available at: http://localhost:8080
echo Swagger UI: http://localhost:8080/swagger-ui/index.html
echo.
echo Using profile: local
echo Press Ctrl+C to stop the server
echo.

REM Set environment variables for local development
set DISCORD_CLIENT_ID=1426309142389325824
set DISCORD_CLIENT_SECRET=FNRTqv2GcYprTPm6RzLpRKcimtOldQxi
set DISCORD_BOT_TOKEN=MTQyNjU3MzU0NDEyMDEyNzY3MQ.Gb29dW.OoJEqvRP-lRuAHta53uTFqQjP5S_7qHbV9PVYw

call mvn spring-boot:run -Dspring-boot.run.profiles=local

pause
