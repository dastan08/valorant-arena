@echo off
echo ========================================
echo   MySQL Database Setup
echo ========================================
echo.

echo Creating database 'valorant_arena'...
echo.

REM Try with default password 'abcd'
mysql -u root -pabcd -e "CREATE DATABASE IF NOT EXISTS valorant_arena; SHOW DATABASES;" 2>nul

if %errorlevel% neq 0 (
    echo.
    echo Could not connect with default password 'abcd'
    echo.
    echo Please enter your MySQL root password:
    mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS valorant_arena; SHOW DATABASES;"
)

echo.
echo Database setup complete!
echo.
pause
