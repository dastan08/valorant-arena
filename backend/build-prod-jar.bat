@echo off
echo ========================================
echo   Building Production JAR
echo ========================================
echo.

REM Navigate to backend directory
cd /d "%~dp0"

echo Checking if application-secrets.yml exists...
if not exist "src\main\resources\application-secrets.yml" (
    echo.
    echo WARNING: application-secrets.yml not found!
    echo Please create it with your RDS credentials.
    echo.
    echo Example content:
    echo spring:
    echo   datasource:
    echo     url: jdbc:mysql://valorant-arena.cxy4ycyyyyvy.ap-south-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true^&requireSSL=true^&serverTimezone=UTC
    echo     username: admin
    echo     password: AGYX3h3NinSDKLx7a0D2
    echo.
    pause
    exit /b 1
)

echo.
echo Building JAR with production profile...
echo This will use application-prod.yml and application-secrets.yml
echo.

REM Build with prod profile
call mvn clean package -DskipTests -Dspring.profiles.active=prod

if %errorlevel% neq 0 (
    echo.
    echo ERROR: Build failed!
    echo Please check the error messages above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   BUILD SUCCESS!
echo ========================================
echo.
echo JAR file created at:
echo target\valorant-arena-backend-1.0.0.jar
echo.
echo This JAR is configured for PRODUCTION use with:
echo - Profile: prod
echo - Database: RDS (ap-south-1)
echo - Configuration: application-prod.yml + application-secrets.yml
echo.
echo To run the JAR on EC2:
echo 1. Upload JAR to EC2
echo 2. Create .env.production with your credentials
echo 3. Run: source .env.production
echo 4. Run: java -jar valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod
echo.
pause
