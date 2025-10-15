# 🚀 Production Build Guide

## Overview

This guide explains how to build a production-ready JAR file that uses:
- **`application-prod.yml`** - Production configuration
- **`application-secrets.yml`** - Your sensitive credentials (gitignored)

---

## 📋 Prerequisites

1. ✅ RDS database created and accessible
2. ✅ `application-secrets.yml` created with your credentials
3. ✅ Maven installed
4. ✅ Java 17 installed

---

## 🔧 Step 1: Verify application-secrets.yml

Make sure `backend/src/main/resources/application-secrets.yml` exists with your RDS credentials:

```yaml
# RDS Database Configuration
spring:
  datasource:
    url: jdbc:mysql://valorant-arena.cxy4ycyyyyvy.ap-south-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
    username: admin
    password: AGYX3h3NinSDKLx7a0D2

# Discord OAuth Configuration
discord:
  client-id: 1426309142389325824
  client-secret: FNRTqv2GcYprTPm6RzLpRKcimtOldQxi
  redirect-uri: http://223.185.30.87:4200/auth/discord/callback
  bot:
    token: MTQyNjU3MzU0NDEyMDEyNzY3MQ.Gb29dW.OoJEqvRP-lRuAHta53uTFqQjP5S_7qHbV9PVYw

# JWT Secret
jwt:
  secret: valorant-arena-super-secret-key-for-jwt-token-generation-minimum-256-bits-required

# CORS Configuration
cors:
  allowed-origins: http://223.185.30.87:4200

# Riot API Configuration (Optional)
riot:
  api:
    key: your-riot-api-key
  oauth:
    client-id: your-riot-client-id
    client-secret: your-riot-client-secret
```

---

## 🏗️ Step 2: Build Production JAR

### **Option 1: Double-Click (Easiest)**

```
backend/build-prod-jar.bat
```

### **Option 2: Command Line**

```bash
cd backend
mvn clean package -DskipTests -Dspring.profiles.active=prod
```

---

## 📦 Step 3: Verify JAR Created

Check that the JAR was created:

```
backend/target/valorant-arena-backend-1.0.0.jar
```

Size should be around 80-100 MB (includes all dependencies).

---

## 🚀 Step 4: Deploy to EC2

### **Upload JAR to EC2**

```bash
# From your local machine
scp -i your-key.pem backend/target/valorant-arena-backend-1.0.0.jar ec2-user@223.185.30.87:/home/ec2-user/valorant-arena/backend/target/
```

---

### **Create .env.production on EC2**

```bash
# SSH into EC2
ssh -i your-key.pem ec2-user@223.185.30.87

# Create environment file
cd /home/ec2-user/valorant-arena/backend
nano .env.production
```

**Add these environment variables:**

```bash
# Database Configuration
DB_URL=jdbc:mysql://valorant-arena.cxy4ycyyyyvy.ap-south-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
DB_USERNAME=admin
DB_PASSWORD=AGYX3h3NinSDKLx7a0D2

# JWT Configuration
JWT_SECRET=valorant-arena-super-secret-key-for-jwt-token-generation-minimum-256-bits-required

# CORS Configuration
CORS_ORIGINS=http://223.185.30.87:4200

# Discord OAuth
DISCORD_CLIENT_ID=1426309142389325824
DISCORD_CLIENT_SECRET=FNRTqv2GcYprTPm6RzLpRKcimtOldQxi
DISCORD_BOT_TOKEN=MTQyNjU3MzU0NDEyMDEyNzY3MQ.Gb29dW.OoJEqvRP-lRuAHta53uTFqQjP5S_7qHbV9PVYw
DISCORD_REDIRECT_URI=http://223.185.30.87:4200/auth/discord/callback

# Riot API (Optional)
RIOT_API_KEY=your-riot-api-key
RIOT_CLIENT_ID=your-riot-client-id
RIOT_CLIENT_SECRET=your-riot-client-secret
RIOT_REDIRECT_URI=http://223.185.30.87:4200/auth/riot/callback

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

---

### **Run the JAR**

```bash
# Load environment variables
source .env.production

# Run the JAR
java -jar target/valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod
```

---

## 🔄 Step 5: Setup Systemd Service (Auto-start)

Update your systemd service file:

```bash
sudo nano /etc/systemd/system/valorant-arena.service
```

**Update with:**

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

**Reload and start:**

```bash
sudo systemctl daemon-reload
sudo systemctl enable valorant-arena
sudo systemctl start valorant-arena
sudo systemctl status valorant-arena
```

---

## ✅ Step 6: Verify Deployment

### **Check if backend is running:**

```bash
# From EC2
curl http://localhost:8080/api/health

# From your local machine
curl http://223.185.30.87:8080/api/health
```

### **Check logs:**

```bash
sudo journalctl -u valorant-arena -f
```

**Look for:**
```
Started ValorantArenaApplication in X.XXX seconds
HikariPool-1 - Start completed
Tomcat started on port(s): 8080 (http)
```

---

## 🔍 Configuration Loading Order

When you run with `--spring.profiles.active=prod`, Spring Boot loads:

1. **`application.yml`** (base configuration)
2. **`application-prod.yml`** (production overrides)
3. **`application-secrets.yml`** (imported via spring.config.import)
4. **Environment variables** (highest priority)

**Example:**
- `application.yml` has: `url: ${DB_URL:jdbc:mysql://localhost:3306/...}`
- `application-prod.yml` has: `url: ${DB_URL:jdbc:mysql://valorant-arena...}`
- `.env.production` has: `DB_URL=jdbc:mysql://valorant-arena...`
- **Result:** Uses the value from `.env.production`

---

## 🐛 Troubleshooting

### **Issue: JAR doesn't connect to RDS**

**Check:**
1. RDS security group allows EC2 IP
2. `.env.production` is loaded: `source .env.production`
3. Profile is set: `--spring.profiles.active=prod`
4. Database credentials are correct

**Test connection:**
```bash
mysql -h valorant-arena.cxy4ycyyyyvy.ap-south-1.rds.amazonaws.com -u admin -p
```

---

### **Issue: "Could not resolve placeholder"**

**Cause:** Environment variable not set

**Solution:**
```bash
# Make sure you loaded .env.production
source .env.production

# Or set manually
export DB_PASSWORD="AGYX3h3NinSDKLx7a0D2"
```

---

### **Issue: Wrong configuration loaded**

**Check which profile is active:**
```bash
# Look for this in logs:
The following profiles are active: prod
```

**Force profile:**
```bash
java -jar target/valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod
```

---

## 📊 Build Comparison

### **Development Build (Local)**
```bash
# Uses: application.yml + application-local.yml
# Database: localhost MySQL
# Profile: local or default
mvn spring-boot:run
```

### **Production Build (EC2/RDS)**
```bash
# Uses: application.yml + application-prod.yml + application-secrets.yml
# Database: RDS (ap-south-1)
# Profile: prod
mvn clean package -DskipTests -Dspring.profiles.active=prod
```

---

## 🔐 Security Best Practices

### ✅ DO:
- Keep `application-secrets.yml` gitignored
- Use environment variables on EC2
- Use different credentials for dev/prod
- Enable SSL for RDS connections
- Rotate secrets regularly

### ❌ DON'T:
- Commit `application-secrets.yml` to Git
- Hardcode credentials in `application.yml` or `application-prod.yml`
- Use same credentials for dev and prod
- Share credentials in chat/email

---

## 📁 File Structure

```
backend/
├── src/main/resources/
│   ├── application.yml                    # Base config (committed)
│   ├── application-local.yml              # Local dev (committed)
│   ├── application-prod.yml               # Production (committed)
│   ├── application-secrets.yml            # Your secrets (gitignored)
│   └── application-secrets.yml.template   # Template (committed)
├── build-jar.bat                          # Build for local testing
├── build-prod-jar.bat                     # Build for production
└── target/
    └── valorant-arena-backend-1.0.0.jar   # Production JAR
```

---

## 🎯 Quick Commands Reference

**Build production JAR:**
```bash
cd backend
mvn clean package -DskipTests -Dspring.profiles.active=prod
```

**Upload to EC2:**
```bash
scp -i key.pem target/valorant-arena-backend-1.0.0.jar ec2-user@223.185.30.87:/home/ec2-user/valorant-arena/backend/target/
```

**Run on EC2:**
```bash
source .env.production
java -jar target/valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod
```

**Check status:**
```bash
sudo systemctl status valorant-arena
sudo journalctl -u valorant-arena -f
```

---

## 🎉 Success Checklist

- [ ] `application-secrets.yml` created with RDS credentials
- [ ] Production JAR built successfully
- [ ] JAR uploaded to EC2
- [ ] `.env.production` created on EC2
- [ ] Systemd service configured
- [ ] Backend running and accessible
- [ ] RDS connection successful
- [ ] API endpoints responding

---

## 📚 Related Guides

- `DEPLOYMENT_GUIDE.md` - Complete AWS deployment
- `CONFIGURATION_GUIDE.md` - Configuration management
- `TROUBLESHOOTING_DEPLOYMENT.md` - Common issues
- `START_HERE.md` - Quick start guide
