@echo off
echo ========================================
echo   Finding Remaining localhost References
echo ========================================
echo.

cd /d "%~dp0"

echo Searching for "localhost:8080" in TypeScript files...
echo.

findstr /s /n /i "localhost:8080" src\*.ts src\*.html 2>nul

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Found localhost references above!
    echo   These need to be fixed before deploying.
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   No localhost references found!
    echo   Ready to build and deploy.
    echo ========================================
)

echo.
pause
