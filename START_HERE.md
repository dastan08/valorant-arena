# 🚀 Quick Start - Run Backend Locally

## Option 1: Double-Click to Run (Easiest)

### Step 1: Setup Database (One Time Only)
1. Make sure MySQL is running
2. Double-click: `backend/setup-database.bat`
3. It will create the `valorant_arena` database

### Step 2: Run Backend
1. Double-click: `backend/run-backend.bat`
2. Wait for "Started ValorantArenaApplication" message
3. Open browser: http://localhost:8080/swagger-ui/index.html

---

## Option 2: Manual Commands

### Step 1: Create Database
```bash
mysql -u root -p
```

```sql
CREATE DATABASE IF NOT EXISTS valorant_arena;
EXIT;
```

### Step 2: Run Backend
```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run
```

---

## ⚠️ Before You Start

### Check Prerequisites

1. **Java 17 Installed**
   ```bash
   java -version
   # Should show: java version "17.x.x"
   ```

2. **Maven Installed**
   ```bash
   mvn -version
   # Should show: Apache Maven 3.x.x
   ```

3. **MySQL Running**
   ```bash
   # Check if MySQL service is running
   sc query MySQL80
   
   # If not running, start it:
   net start MySQL80
   ```

---

## 🔧 If MySQL Password is Not 'abcd'

### Option A: Update Configuration File

Create `backend/src/main/resources/application-secrets.yml`:

```yaml
database:
  password: YOUR_MYSQL_PASSWORD
```

### Option B: Use Environment Variable

**PowerShell:**
```powershell
$env:DB_PASSWORD="YOUR_MYSQL_PASSWORD"
cd backend
mvn spring-boot:run
```

**CMD:**
```cmd
set DB_PASSWORD=YOUR_MYSQL_PASSWORD
cd backend
mvn spring-boot:run
```

---

## ✅ Verify Backend is Running

### Test 1: Check Console
Look for this message:
```
Started ValorantArenaApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

### Test 2: Open Swagger UI
http://localhost:8080/swagger-ui/index.html

### Test 3: Test API
```bash
curl http://localhost:8080/api/health
```

---

## 🚨 Common Issues

### Issue 1: "Port 8080 already in use"
```bash
# Find and kill process using port 8080
netstat -ano | findstr :8080
taskkill /PID <PID_NUMBER> /F
```

### Issue 2: "Communications link failure"
- MySQL is not running → Start MySQL service
- Wrong password → Update configuration
- Database doesn't exist → Run `setup-database.bat`

### Issue 3: "Access denied for user 'root'"
- Update password in `application-secrets.yml`
- Or use environment variable (see above)

---

## 📁 Project Structure

```
valorant-arena/
├── backend/
│   ├── run-backend.bat          ← Double-click to run
│   ├── setup-database.bat       ← Double-click to setup DB
│   ├── src/
│   ├── pom.xml
│   └── target/
├── frontend/
└── START_HERE.md                ← You are here
```

---

## 🎯 Quick Commands Reference

**Build:**
```bash
cd backend
mvn clean install
```

**Run:**
```bash
mvn spring-boot:run
```

**Build JAR:**
```bash
mvn clean package
java -jar target/valorant-arena-backend-1.0.0.jar
```

**Skip Tests:**
```bash
mvn clean install -DskipTests
```

---

## 🎉 Success!

When you see this, you're ready:
- ✅ Backend running on http://localhost:8080
- ✅ Swagger UI at http://localhost:8080/swagger-ui/index.html
- ✅ Database connected
- ✅ Ready to develop!

---

## 📚 Next Steps

1. **Test API Endpoints** - Use Swagger UI
2. **Run Frontend** - See `frontend/README.md`
3. **Deploy to AWS** - See `DEPLOYMENT_GUIDE.md`

---

## 💡 Tips

- Keep the backend terminal open while developing
- Use Swagger UI to test APIs
- Check `backend/logs/valorant-arena.log` for detailed logs
- Press `Ctrl+C` in terminal to stop the backend

---

## 🆘 Need Help?

Check these files:
- `TROUBLESHOOTING_DEPLOYMENT.md` - Common issues and solutions
- `AWS_RDS_SETUP.md` - Database setup
- `CORS_CONFIGURATION.md` - CORS issues
- `README.md` - Project overview
