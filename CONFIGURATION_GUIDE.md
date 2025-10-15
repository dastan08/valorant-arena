# 🔐 Configuration Management Guide

## Overview

The application uses Spring Boot profiles to manage different configurations:

- **`application.yml`** - Base configuration with default values
- **`application-local.yml`** - Local development configuration
- **`application-prod.yml`** - Production configuration (AWS)
- **`application-secrets.yml`** - Your personal secrets (gitignored)

---

## 🏠 Local Development Setup

### Method 1: Using application-local.yml (Recommended)

**Step 1: Copy the template**
```bash
cd backend/src/main/resources
cp application-local.yml application-secrets.yml
```

**Step 2: Update `application-secrets.yml` with your credentials:**
```yaml
spring:
  datasource:
    password: YOUR_MYSQL_PASSWORD

discord:
  client-id: YOUR_DISCORD_CLIENT_ID
  client-secret: YOUR_DISCORD_CLIENT_SECRET
  bot:
    token: YOUR_DISCORD_BOT_TOKEN

riot:
  api:
    key: YOUR_RIOT_API_KEY
```

**Step 3: Run with local profile:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

Or just double-click `run-backend.bat` (already configured).

---

### Method 2: Using Environment Variables

**Windows PowerShell:**
```powershell
$env:DISCORD_CLIENT_ID="1426309142389325824"
$env:DISCORD_CLIENT_SECRET="FNRTqv2GcYprTPm6RzLpRKcimtOldQxi"
$env:DISCORD_BOT_TOKEN="MTQyNjU3MzU0NDEyMDEyNzY3MQ.Gb29dW.OoJEqvRP-lRuAHta53uTFqQjP5S_7qHbV9PVYw"

cd backend
mvn spring-boot:run
```

**Windows CMD:**
```cmd
set DISCORD_CLIENT_ID=1426309142389325824
set DISCORD_CLIENT_SECRET=FNRTqv2GcYprTPm6RzLpRKcimtOldQxi
set DISCORD_BOT_TOKEN=MTQyNjU3MzU0NDEyMDEyNzY3MQ.Gb29dW.OoJEqvRP-lRuAHta53uTFqQjP5S_7qHbV9PVYw

cd backend
mvn spring-boot:run
```

---

## ☁️ Production (AWS EC2) Setup

### Using .env.production file

**On EC2, create `.env.production`:**
```bash
cd /home/ec2-user/valorant-arena/backend
nano .env.production
```

**Add your production credentials:**
```bash
# Database
DB_URL=jdbc:mysql://valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
DB_USERNAME=admin
DB_PASSWORD=your-rds-password

# JWT
JWT_SECRET=your-production-jwt-secret-minimum-256-bits

# CORS
CORS_ORIGINS=http://223.185.30.87:4200

# Discord
DISCORD_CLIENT_ID=1426309142389325824
DISCORD_CLIENT_SECRET=FNRTqv2GcYprTPm6RzLpRKcimtOldQxi
DISCORD_BOT_TOKEN=MTQyNjU3MzU0NDEyMDEyNzY3MQ.Gb29dW.OoJEqvRP-lRuAHta53uTFqQjP5S_7qHbV9PVYw
DISCORD_REDIRECT_URI=http://223.185.30.87:4200/auth/discord/callback

# Riot API
RIOT_API_KEY=your-riot-api-key
RIOT_CLIENT_ID=your-riot-client-id
RIOT_CLIENT_SECRET=your-riot-client-secret
RIOT_REDIRECT_URI=http://223.185.30.87:4200/auth/riot/callback

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

**Load and run:**
```bash
source .env.production
java -jar target/valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod
```

---

## 📋 Configuration Priority

Spring Boot loads configurations in this order (later overrides earlier):

1. `application.yml` (base configuration)
2. `application-{profile}.yml` (profile-specific)
3. `application-secrets.yml` (imported via spring.config.import)
4. Environment variables
5. Command line arguments

**Example:**
```yaml
# application.yml
discord:
  client-id: ${DISCORD_CLIENT_ID:default-value}

# application-local.yml (overrides if profile=local)
discord:
  client-id: 1426309142389325824

# Environment variable (overrides all)
DISCORD_CLIENT_ID=9999999999999999999
```

---

## 🔑 Environment Variables Reference

### Database
- `DB_URL` - Database connection URL
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

### JWT
- `JWT_SECRET` - Secret key for JWT token generation (min 256 bits)

### CORS
- `CORS_ORIGINS` - Comma-separated list of allowed origins

### Discord OAuth
- `DISCORD_CLIENT_ID` - Discord application client ID
- `DISCORD_CLIENT_SECRET` - Discord application client secret
- `DISCORD_BOT_TOKEN` - Discord bot token (optional)
- `DISCORD_REDIRECT_URI` - OAuth redirect URI

### Riot API
- `RIOT_API_KEY` - Riot Games API key
- `RIOT_CLIENT_ID` - Riot OAuth client ID
- `RIOT_CLIENT_SECRET` - Riot OAuth client secret
- `RIOT_REDIRECT_URI` - Riot OAuth redirect URI

### Spring
- `SPRING_PROFILES_ACTIVE` - Active profile (local, prod, dev)

---

## 🎯 Quick Commands

### Run with specific profile

**Local:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

**Production:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**Multiple profiles:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local,secrets
```

---

### Run with environment variables

**Set and run (PowerShell):**
```powershell
$env:DISCORD_CLIENT_ID="your-id"
$env:DISCORD_CLIENT_SECRET="your-secret"
mvn spring-boot:run
```

**One-liner (CMD):**
```cmd
set DISCORD_CLIENT_ID=your-id && set DISCORD_CLIENT_SECRET=your-secret && mvn spring-boot:run
```

---

## 🔒 Security Best Practices

### ✅ DO:
- Keep secrets in `application-secrets.yml` (gitignored)
- Use environment variables in production
- Use different credentials for dev/prod
- Rotate secrets regularly
- Use strong JWT secrets (min 256 bits)

### ❌ DON'T:
- Commit secrets to Git
- Hardcode secrets in `application.yml`
- Share secrets in chat/email
- Use weak or default passwords
- Reuse secrets across environments

---

## 📁 File Structure

```
backend/src/main/resources/
├── application.yml              # Base config (committed)
├── application-local.yml        # Local dev template (committed)
├── application-prod.yml         # Production config (committed)
├── application-secrets.yml      # Your secrets (gitignored)
└── application-secrets.yml.template  # Template (committed)
```

---

## 🐛 Troubleshooting

### Issue: "Could not resolve placeholder"

**Error:**
```
Could not resolve placeholder 'DISCORD_CLIENT_ID' in value "${DISCORD_CLIENT_ID}"
```

**Solutions:**

1. **Set environment variable:**
   ```bash
   set DISCORD_CLIENT_ID=your-value
   ```

2. **Create application-secrets.yml:**
   ```yaml
   discord:
     client-id: your-value
   ```

3. **Use default value in application.yml:**
   ```yaml
   discord:
     client-id: ${DISCORD_CLIENT_ID:default-value}
   ```

---

### Issue: Configuration not loading

**Check:**
1. File exists: `application-secrets.yml`
2. Correct profile: `-Dspring-boot.run.profiles=local`
3. Environment variables are set
4. No typos in property names

**Debug:**
```bash
# Show loaded properties
mvn spring-boot:run -Dspring-boot.run.arguments=--debug
```

---

### Issue: Wrong configuration in production

**Check:**
1. Correct profile is active: `SPRING_PROFILES_ACTIVE=prod`
2. Environment variables are loaded: `source .env.production`
3. Systemd service has `EnvironmentFile` configured

---

## 🎉 Quick Setup Checklist

### Local Development
- [ ] MySQL installed and running
- [ ] Database `valorant_arena` created
- [ ] `application-secrets.yml` created with your credentials
- [ ] Discord application created and credentials added
- [ ] Run `run-backend.bat` or `mvn spring-boot:run`

### Production (EC2)
- [ ] RDS database created
- [ ] `.env.production` file created with all credentials
- [ ] Systemd service configured with `EnvironmentFile`
- [ ] Environment variables loaded
- [ ] Profile set to `prod`

---

## 📚 Additional Resources

- [Spring Boot Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Discord Developer Portal](https://discord.com/developers/applications)
- [Riot Games Developer Portal](https://developer.riotgames.com/)
