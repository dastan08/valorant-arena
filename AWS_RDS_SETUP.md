# AWS RDS Configuration Guide

## Your RDS Database Details

**RDS Endpoint:** `valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com`  
**Region:** `eu-north-1` (Stockholm)  
**Port:** `3306`  
**Database Name:** `valorant_arena`  
**Master Username:** `admin`

---

## Step 1: Configure Backend for RDS

### Option A: Using Environment Variables (Recommended for Production)

1. **Create production environment file:**
   ```bash
   cd backend
   cp .env.production.example .env.production
   ```

2. **Edit `.env.production` and set:**
   ```bash
   DB_URL=jdbc:mysql://valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
   DB_USERNAME=admin
   DB_PASSWORD=your-rds-master-password
   
   JWT_SECRET=your-production-jwt-secret
   RIOT_API_KEY=your-riot-api-key
   DISCORD_CLIENT_ID=your-discord-client-id
   DISCORD_CLIENT_SECRET=your-discord-client-secret
   DISCORD_BOT_TOKEN=your-discord-bot-token
   
   SPRING_PROFILES_ACTIVE=prod
   ```

3. **Run with environment variables:**
   ```bash
   # Load environment variables
   source .env.production
   
   # Run the application
   mvn spring-boot:run
   ```

### Option B: Using application-secrets.yml (For Local Testing)

1. **Edit `backend/src/main/resources/application-secrets.yml`:**
   ```yaml
   database:
     url: jdbc:mysql://valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
     username: admin
     password: your-rds-master-password
   ```

---

## Step 2: Initialize the Database

### Connect to RDS from Your Local Machine

```bash
# Install MySQL client if not already installed
# Windows: Download from https://dev.mysql.com/downloads/mysql/
# Mac: brew install mysql-client
# Linux: sudo apt-get install mysql-client

# Connect to RDS
mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p
```

### Create Database and Tables

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS valorant_arena;
USE valorant_arena;

-- The Spring Boot application will auto-create tables on first run
-- due to hibernate.ddl-auto=update setting
```

### Run Test Data Scripts (Optional)

```bash
# From your local machine
mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p valorant_arena < test-db.sql
```

---

## Step 3: Configure Security Group

### Allow Your IP to Access RDS

1. **Go to AWS Console → RDS → Databases**
2. **Click on `valorant-arena-db`**
3. **Click on the VPC security group**
4. **Edit Inbound Rules:**
   - Type: `MySQL/Aurora`
   - Protocol: `TCP`
   - Port: `3306`
   - Source: 
     - For testing: `My IP` (your current IP)
     - For production: Your EC2 security group

### Allow EC2 Instance to Access RDS

1. **Get your EC2 security group ID** (e.g., `sg-xxxxxxxxx`)
2. **Add inbound rule to RDS security group:**
   - Type: `MySQL/Aurora`
   - Protocol: `TCP`
   - Port: `3306`
   - Source: `Custom` → Select your EC2 security group

---

## Step 4: Test Connection

### Test from Local Machine

```bash
# Test connection
mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p -e "SELECT 1;"

# If successful, you should see:
# +---+
# | 1 |
# +---+
# | 1 |
# +---+
```

### Test from Spring Boot Application

```bash
cd backend

# Set environment variables
export DB_URL="jdbc:mysql://valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC"
export DB_USERNAME="admin"
export DB_PASSWORD="your-rds-master-password"

# Run application
mvn spring-boot:run

# Check logs for successful connection:
# "HikariPool-1 - Start completed."
```

---

## Step 5: Deploy to EC2

### Build and Package

```bash
# Build backend
cd backend
mvn clean package -DskipTests

# JAR file will be at: target/valorant-arena-backend-1.0.0.jar
```

### Upload to EC2

```bash
# Upload JAR file
scp -i your-key.pem backend/target/valorant-arena-backend-1.0.0.jar ec2-user@your-ec2-ip:/home/ec2-user/

# Upload environment file
scp -i your-key.pem backend/.env.production ec2-user@your-ec2-ip:/home/ec2-user/
```

### Run on EC2

```bash
# SSH into EC2
ssh -i your-key.pem ec2-user@your-ec2-ip

# Load environment variables
source .env.production

# Run application
java -jar valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod

# Or run in background with nohup
nohup java -jar valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod > app.log 2>&1 &
```

---

## Troubleshooting

### Cannot Connect to RDS

**Error:** `Communications link failure`

**Solutions:**
1. Check security group allows your IP
2. Verify RDS endpoint is correct
3. Ensure RDS is publicly accessible (if connecting from outside AWS)
4. Check VPC and subnet configuration

### SSL Connection Issues

**Error:** `SSL connection error`

**Solution:** Download RDS SSL certificate:
```bash
wget https://truststore.pki.rds.amazonaws.com/eu-north-1/eu-north-1-bundle.pem

# Add to connection URL:
?useSSL=true&requireSSL=true&trustCertificateKeyStoreUrl=file:///path/to/eu-north-1-bundle.pem
```

### Connection Timeout

**Error:** `Connection timed out`

**Solutions:**
1. Check if RDS is in the correct VPC
2. Verify subnet group configuration
3. Check route tables and network ACLs
4. Ensure RDS is in a public subnet (if accessing from internet)

---

## Production Best Practices

### 1. Use Parameter Store or Secrets Manager

Instead of `.env` files, use AWS Systems Manager Parameter Store:

```bash
# Store secrets
aws ssm put-parameter --name "/valorant-arena/db-password" --value "your-password" --type "SecureString" --region eu-north-1

# Retrieve in application
aws ssm get-parameter --name "/valorant-arena/db-password" --with-decryption --region eu-north-1
```

### 2. Enable RDS Backups

- Automated backups: 7-35 days retention
- Manual snapshots before major changes
- Enable Multi-AZ for high availability

### 3. Monitor RDS Performance

- Enable Enhanced Monitoring
- Set up CloudWatch alarms for:
  - CPU utilization > 80%
  - Free storage space < 10GB
  - Database connections > 80% of max

### 4. Use Read Replicas (Optional)

For high-traffic applications, create read replicas:
```bash
aws rds create-db-instance-read-replica \
  --db-instance-identifier valorant-arena-db-replica \
  --source-db-instance-identifier valorant-arena-db \
  --region eu-north-1
```

---

## Cost Optimization

- Use `db.t3.micro` or `db.t3.small` for development
- Stop RDS instance when not in use (saves ~50% cost)
- Use Reserved Instances for production (up to 60% savings)
- Monitor storage growth and optimize queries

---

## Quick Reference

**Connection String:**
```
jdbc:mysql://valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
```

**MySQL CLI:**
```bash
mysql -h valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com -u admin -p
```

**Spring Boot Run Command:**
```bash
java -jar valorant-arena-backend-1.0.0.jar --spring.profiles.active=prod
```
