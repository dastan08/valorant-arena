@echo off
echo ========================================
echo   Building Backend JAR (Skip Tests)
echo ========================================
echo.

REM Navigate to backend directory
cd /d "%~dp0"

echo Building JAR file...
echo This will skip tests to avoid database connection issues
echo.

call mvn clean package -DskipTests

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
echo To run the JAR:
echo java -jar target\valorant-arena-backend-1.0.0.jar
echo.
pause
