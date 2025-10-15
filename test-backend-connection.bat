@echo off
echo ========================================
echo   Testing Backend Connection
echo ========================================
echo.

echo Testing backend at: http://13.201.185.135:8080
echo.

echo Test 1: Health check (if exists)
echo ========================================
curl -v http://13.201.185.135:8080/api/health
echo.
echo.

echo Test 2: Tournaments endpoint
echo ========================================
curl -v http://13.201.185.135:8080/api/tournaments/upcoming
echo.
echo.

echo Test 3: Check if port 8080 is open
echo ========================================
powershell -Command "Test-NetConnection -ComputerName 13.201.185.135 -Port 8080"
echo.

echo ========================================
echo   Test Complete
echo ========================================
echo.
echo If all tests failed, the backend might not be running or accessible.
echo.
echo To fix:
echo 1. SSH to EC2: ssh -i your-key.pem ec2-user@13.201.185.135
echo 2. Check if Java is running: ps aux | grep java
echo 3. Check security group allows port 8080 from your IP
echo.
pause
