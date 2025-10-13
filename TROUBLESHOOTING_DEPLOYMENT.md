# 🔧 Deployment Troubleshooting Guide

## Error: "Unable to access jarfile"

### Symptoms
```
Error: Unable to access jarfile /home/ec2-user/valorant-arena/backend/target/valorant-arena-0.1-SNAPSHOT.jar
Main process exited, code=exited, status=1/FAILURE
```

### Root Cause
The systemd service file has the wrong JAR file path.

---

## ✅ Solution: Fix the JAR Path

### Step 1: Find the Actual JAR File

```bash
# SSH into your EC2 instance
ssh -i your-key.pem ec2-user@YOUR_EC2_IP

# Find your JAR file
find /home/ec2-user -name "*.jar" -type f

# Or check the target directory
ls -lh /home/ec2-user/valorant-arena/backend/target/*.jar
```

**Expected output:**
```
/home/ec2-user/valorant-arena/backend/target/valorant-arena-backend-1.0.0.jar
```

---

### Step 2: Update Systemd Service File

```bash
# Stop the service first
sudo systemctl stop valorant-arena

# Edit the service file
sudo nano /etc/systemd/system/valorant-arena.service
```

**Update the `ExecStart` line with the CORRECT path:**

```ini
[Unit]
Description=Valorant Arena Spring Boot Application
After=syslog.target network.target

[Service]
User=ec2-user
WorkingDirectory=/home/ec2-user/valorant-arena/backend
EnvironmentFile=/home/ec2-user/valorant-arena/backend/.env.production
ExecStart=/usr/bin/java -jar /home/ec2-user/valorant-arena/backend/target/valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod
SuccessExitStatus=143
StandardOutput=journal
StandardError=journal
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**Key changes:**
- ✅ Correct JAR path: `/home/ec2-user/valorant-arena/backend/target/valorant-arena-0.1-SNAPSHOT.jar`
- ✅ Added `WorkingDirectory`
- ✅ Added `EnvironmentFile` to load your `.env.production`
- ✅ Added `--spring.profiles.active=prod`
- ✅ Added logging to journal

---

### Step 3: Reload and Restart Service

```bash
# Reload systemd configuration
sudo systemctl daemon-reload

# Start the service
sudo systemctl start valorant-arena

# Check status
sudo systemctl status valorant-arena

# If it shows "active (running)" - SUCCESS! ✅
```

---

### Step 4: Check Logs

```bash
# View real-time logs
sudo journalctl -u valorant-arena -f

# View last 100 lines
sudo journalctl -u valorant-arena -n 100

# View logs with timestamps
sudo journalctl -u valorant-arena --since "5 minutes ago"
```

**Look for these SUCCESS messages:**
```
✅ Started ValorantArenaApplication
✅ HikariPool-1 - Start completed
✅ Tomcat started on port(s): 8080
```

---

## Common Issues & Solutions

### Issue 1: Environment Variables Not Loaded

**Error:**
```
Could not resolve placeholder 'DB_PASSWORD' in value "${DB_PASSWORD}"
```

**Solution:**
```bash
# Make sure .env.production exists
ls -lh /home/ec2-user/valorant-arena/backend/.env.production

# If missing, create it:
cd /home/ec2-user/valorant-arena/backend
nano .env.production

# Add your configuration (see below)
```

**Required `.env.production` content:**
```bash
DB_URL=jdbc:mysql://valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
DB_USERNAME=admin
DB_PASSWORD=your-rds-password

JWT_SECRET=your-jwt-secret-minimum-256-bits

CORS_ORIGINS=http://223.185.30.87:4200

RIOT_API_KEY=your-riot-api-key
DISCORD_CLIENT_ID=your-discord-client-id
DISCORD_CLIENT_SECRET=your-discord-client-secret
DISCORD_BOT_TOKEN=your-discord-bot-token

SPRING_PROFILES_ACTIVE=prod
```

---

### Issue 2: Cannot Connect to RDS

**Error:**
```
Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago
```

**Solution:**

1. **Check RDS Security Group:**
   ```bash
   # Go to AWS Console → RDS → valorant-arena-db → Connectivity & security
   # Click on the VPC security group
   # Edit Inbound Rules → Add Rule:
   # Type: MySQL/Aurora
   # Port: 3306
   # Source: Your EC2 security group (sg-xxxxxxxxx)
   ```

2. **Test connection from EC2:**
   ```bash
   # Install MySQL client
   sudo yum install mysql -y
   
   # Test connection
   mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p
   
   # If successful, create database:
   CREATE DATABASE IF NOT EXISTS valorant_arena;
   EXIT;
   ```

---

### Issue 3: Port 8080 Already in Use

**Error:**
```
Port 8080 was already in use
```

**Solution:**
```bash
# Find what's using port 8080
sudo lsof -i :8080

# Kill the process
sudo kill -9 PID_NUMBER

# Or change the port in .env.production:
SERVER_PORT=8081
```

---

### Issue 4: Permission Denied

**Error:**
```
Permission denied: /var/log/valorant-arena/application.log
```

**Solution:**
```bash
# Create log directory with correct permissions
sudo mkdir -p /var/log/valorant-arena
sudo chown ec2-user:ec2-user /var/log/valorant-arena
sudo chmod 755 /var/log/valorant-arena

# Restart service
sudo systemctl restart valorant-arena
```

---

### Issue 5: Java Not Found

**Error:**
```
/usr/bin/java: No such file or directory
```

**Solution:**
```bash
# Check Java installation
java -version

# If not installed:
sudo yum install java-17-amazon-corretto -y

# Find Java path
which java

# Update service file with correct path
sudo nano /etc/systemd/system/valorant-arena.service
# Change ExecStart to use the correct java path
```

---

## Quick Diagnostic Commands

### Check Service Status
```bash
sudo systemctl status valorant-arena
```

### View Recent Logs
```bash
sudo journalctl -u valorant-arena -n 50 --no-pager
```

### Test Backend Manually
```bash
# Stop the service
sudo systemctl stop valorant-arena

# Run manually to see errors
cd /home/ec2-user/valorant-arena/backend
source .env.production
java -jar target/valorant-arena-backend-1.0.0.jar
```

### Check if Backend is Running
```bash
# Check if port 8080 is listening
sudo netstat -tlnp | grep 8080

# Or
sudo lsof -i :8080

# Test API endpoint
curl http://localhost:8080/api/health
```

### Check Database Connection
```bash
mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p
```

---

## Complete Restart Procedure

If everything is messed up, start fresh:

```bash
# 1. Stop the service
sudo systemctl stop valorant-arena
sudo systemctl disable valorant-arena

# 2. Remove old service file
sudo rm /etc/systemd/system/valorant-arena.service

# 3. Create new service file with correct paths
sudo nano /etc/systemd/system/valorant-arena.service

# 4. Paste the corrected configuration (see Step 2 above)

# 5. Reload systemd
sudo systemctl daemon-reload

# 6. Enable and start service
sudo systemctl enable valorant-arena
sudo systemctl start valorant-arena

# 7. Check status
sudo systemctl status valorant-arena

# 8. Watch logs
sudo journalctl -u valorant-arena -f
```

---

## Success Checklist

✅ JAR file exists at the correct path  
✅ `.env.production` file exists with all required variables  
✅ RDS security group allows EC2 connection  
✅ Database `valorant_arena` exists in RDS  
✅ Service file has correct paths  
✅ Java 17 is installed  
✅ Port 8080 is not in use  
✅ Logs show "Started ValorantArenaApplication"  
✅ `curl http://localhost:8080/api/health` returns 200 OK  

---

## Your Current Setup

**RDS Endpoint:** `valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com`  
**Region:** `eu-north-1`  
**Your IP:** `223.185.30.87`  
**Repository Path:** `/home/ec2-user/valorant-arena/`  
**JAR Path:** `/home/ec2-user/valorant-arena/backend/target/valorant-arena-backend-1.0.0.jar`  
**JAR Name:** `valorant-arena-backend-1.0.0.jar` (from pom.xml: artifactId=valorant-arena-backend, version=1.0.0)  

---

## Need More Help?

1. **Check full logs:**
   ```bash
   sudo journalctl -u valorant-arena --since today --no-pager > ~/logs.txt
   cat ~/logs.txt
   ```

2. **Check application.yml:**
   ```bash
   cat /home/ec2-user/valorant-arena/backend/src/main/resources/application.yml
   ```

3. **Check .env.production:**
   ```bash
   cat /home/ec2-user/valorant-arena/backend/.env.production
   ```

4. **Test RDS connection:**
   ```bash
   mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p -e "SHOW DATABASES;"
   ```
