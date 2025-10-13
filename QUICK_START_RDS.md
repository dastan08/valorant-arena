# 🚀 Quick Start - Connect to Your RDS Database

## Your RDS Configuration

✅ **RDS Endpoint:** `valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com`  
✅ **Region:** `eu-north-1` (Stockholm)  
✅ **Database:** `valorant_arena`  
✅ **Username:** `admin`  
✅ **Port:** `3306`

---

## Step 1: Test RDS Connection (2 minutes)

### Install MySQL Client (if not installed)

**Windows:**
```powershell
# Download from: https://dev.mysql.com/downloads/mysql/
# Or use Chocolatey:
choco install mysql
```

**Mac:**
```bash
brew install mysql-client
```

**Linux:**
```bash
sudo apt-get install mysql-client
```

### Connect to RDS

```bash
mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p
# Enter your RDS master password when prompted
```

### Create Database

```sql
CREATE DATABASE IF NOT EXISTS valorant_arena;
USE valorant_arena;
SHOW TABLES;  -- Should be empty initially
EXIT;
```

---

## Step 2: Configure Your Backend (3 minutes)

### Option A: Quick Local Testing

1. **Create your secrets file:**
   ```bash
   cd backend/src/main/resources
   cp application-secrets.yml.template application-secrets.yml
   ```

2. **Edit `application-secrets.yml`:**
   ```yaml
   database:
     url: jdbc:mysql://valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
     username: admin
     password: YOUR_RDS_MASTER_PASSWORD_HERE
   
   jwt:
     secret: valorant-arena-super-secret-key-for-jwt-token-generation-minimum-256-bits-required
   
   riot:
     api:
       key: your-riot-api-key
   
   discord:
     client-id: your-discord-client-id
     client-secret: your-discord-client-secret
     bot:
       token: your-discord-bot-token
   ```

3. **Run the application:**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

### Option B: Using Environment Variables (Recommended)

1. **Create `.env.production`:**
   ```bash
   cd backend
   cp .env.production.example .env.production
   ```

2. **Edit `.env.production`:**
   ```bash
   DB_URL=jdbc:mysql://valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
   DB_USERNAME=admin
   DB_PASSWORD=YOUR_RDS_MASTER_PASSWORD
   
   JWT_SECRET=your-jwt-secret
   RIOT_API_KEY=your-riot-api-key
   DISCORD_CLIENT_ID=your-discord-client-id
   DISCORD_CLIENT_SECRET=your-discord-client-secret
   DISCORD_BOT_TOKEN=your-discord-bot-token
   ```

3. **Run with environment variables:**
   ```bash
   # Windows PowerShell
   Get-Content .env.production | ForEach-Object { if($_ -match '^([^=]+)=(.*)$') { [Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process') } }
   mvn spring-boot:run
   
   # Mac/Linux
   source .env.production
   mvn spring-boot:run
   ```

---

## Step 3: Verify Connection (1 minute)

### Check Application Logs

Look for these success messages:

```
✅ HikariPool-1 - Starting...
✅ HikariPool-1 - Start completed.
✅ Initialized JPA EntityManagerFactory for persistence unit 'default'
✅ Started ValorantArenaApplication in X.XXX seconds
```

### Test API

```bash
# Check if backend is running
curl http://localhost:8080/api/auth/health

# Or open in browser:
# http://localhost:8080/swagger-ui.html
```

---

## Step 4: Initialize Test Data (Optional)

```bash
# Connect to RDS
mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p valorant_arena

# Run test scripts
source test-db.sql
source create_test_users.sql
source create_test_tournament.sql
source create_test_team.sql
```

---

## Troubleshooting

### ❌ Cannot Connect to RDS

**Error:** `Communications link failure`

**Fix:** Update RDS Security Group
1. Go to AWS Console → RDS → Databases → valorant-arena-db
2. Click on VPC security group
3. Edit Inbound Rules → Add Rule:
   - Type: `MySQL/Aurora`
   - Port: `3306`
   - Source: `My IP` (or `0.0.0.0/0` for testing only)

### ❌ Access Denied

**Error:** `Access denied for user 'admin'@'...'`

**Fix:** Check your password
- Verify you're using the correct RDS master password
- Reset password in AWS Console if needed

### ❌ Unknown Database

**Error:** `Unknown database 'valorant_arena'`

**Fix:** Create the database
```sql
mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p
CREATE DATABASE valorant_arena;
```

---

## Next Steps

1. ✅ **Test locally** - Verify backend connects to RDS
2. 📦 **Build for production** - Run `mvn clean package`
3. 🚀 **Deploy to EC2** - Follow `AWS_RDS_SETUP.md` for full deployment
4. 🌐 **Configure frontend** - Update API endpoints
5. 🔒 **Secure secrets** - Rotate Discord bot token and API keys

---

## Important Security Notes

⚠️ **Never commit these files:**
- `application-secrets.yml`
- `.env.production`
- `*_accessKeys.csv`

✅ **Already protected by `.gitignore`**

⚠️ **Rotate your exposed secrets:**
- Discord bot token (from previous push)
- Discord client secret
- Riot API key

---

## Quick Commands Reference

**Connect to RDS:**
```bash
mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p
```

**Run Backend:**
```bash
cd backend
mvn spring-boot:run
```

**Build for Production:**
```bash
cd backend
mvn clean package -DskipTests
```

**Test Connection:**
```bash
curl http://localhost:8080/swagger-ui.html
```

---

## Need Help?

📖 **Full Documentation:**
- `AWS_RDS_SETUP.md` - Complete RDS setup guide
- `DEPLOYMENT_GUIDE.md` - Full AWS deployment guide
- `README.md` - Project overview and setup

🔧 **Configuration Files:**
- `backend/src/main/resources/application.yml` - Main config
- `backend/src/main/resources/application-prod.yml` - Production config
- `backend/.env.production.example` - Environment variables template
