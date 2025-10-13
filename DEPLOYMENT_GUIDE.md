# Valorant Arena - AWS Free Tier Deployment Guide

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [AWS Free Tier Overview](#aws-free-tier-overview)
3. [Architecture Overview](#architecture-overview)
4. [Step-by-Step Deployment](#step-by-step-deployment)
5. [Database Setup](#database-setup)
6. [Backend Deployment](#backend-deployment)
7. [Frontend Deployment](#frontend-deployment)
8. [Domain & SSL Setup](#domain--ssl-setup)
9. [Monitoring & Maintenance](#monitoring--maintenance)
10. [Cost Optimization](#cost-optimization)
11. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Accounts
- ✅ AWS Account (Free Tier eligible)
- ✅ GitHub Account (for code repository)
- ✅ Domain name (optional, but recommended - can use Freenom for free domains)

### Local Tools Needed
- ✅ AWS CLI installed
- ✅ Git
- ✅ Node.js (v18+)
- ✅ Java JDK 17+
- ✅ Maven

---

## AWS Free Tier Overview

### What's Included (12 months free):
- **EC2**: 750 hours/month of t2.micro or t3.micro instances
- **RDS**: 750 hours/month of db.t2.micro, db.t3.micro, or db.t4g.micro
- **S3**: 5GB storage, 20,000 GET requests, 2,000 PUT requests
- **CloudFront**: 50GB data transfer out, 2,000,000 HTTP/HTTPS requests
- **Route 53**: $0.50/month for hosted zone (not free, but minimal)
- **Elastic Load Balancer**: 750 hours/month (Application Load Balancer)

### Always Free:
- **Lambda**: 1M free requests/month
- **DynamoDB**: 25GB storage
- **CloudWatch**: 10 custom metrics, 10 alarms

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         Internet                             │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
              ┌────────────────┐
              │  Route 53 DNS  │ (Optional - $0.50/month)
              └────────┬───────┘
                       │
                       ▼
              ┌────────────────┐
              │   CloudFront   │ (CDN for Frontend)
              │   (Free Tier)  │
              └────────┬───────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ▼                             ▼
┌───────────────┐           ┌─────────────────┐
│  S3 Bucket    │           │  EC2 Instance   │
│  (Frontend)   │           │  (Backend API)  │
│  Free: 5GB    │           │  t2.micro       │
└───────────────┘           │  750 hrs/month  │
                            └────────┬────────┘
                                     │
                                     ▼
                            ┌─────────────────┐
                            │  RDS MySQL      │
                            │  db.t2.micro    │
                            │  750 hrs/month  │
                            └─────────────────┘
```

---

## Step-by-Step Deployment

### Phase 1: AWS Account Setup

#### 1.1 Create AWS Account
```bash
1. Go to https://aws.amazon.com/
2. Click "Create an AWS Account"
3. Follow the registration process
4. Add payment method (required, but won't be charged if staying in free tier)
5. Verify your identity
```

#### 1.2 Install AWS CLI
```bash
# Windows (PowerShell)
msiexec.exe /i https://awscli.amazonaws.com/AWSCLIV2.msi

# Verify installation
aws --version

# Configure AWS CLI
aws configure
# Enter:
# - AWS Access Key ID
# - AWS Secret Access Key
# - Default region: us-east-1 (or your preferred region)
# - Default output format: json
```

#### 1.3 Create IAM User (Best Practice)
```bash
1. Go to AWS Console → IAM
2. Click "Users" → "Add User"
3. Username: valorant-arena-deployer
4. Access type: Programmatic access
5. Attach policies:
   - AmazonEC2FullAccess
   - AmazonRDSFullAccess
   - AmazonS3FullAccess
   - CloudFrontFullAccess
6. Save Access Key ID and Secret Access Key
```

---

## Database Setup

### Option A: RDS MySQL (Recommended for Production)

#### Step 1: Create RDS Instance
```bash
1. Go to AWS Console → RDS
2. Click "Create database"
3. Choose:
   - Engine: MySQL 8.0
   - Template: Free tier
   - DB instance identifier: valorant-arena-db
   - Master username: admin
   - Master password: [Create strong password]
   - DB instance class: db.t2.micro (free tier eligible)
   - Storage: 20 GB (free tier limit)
   - Enable automatic backups: Yes (7 days retention)
   - VPC: Default VPC
   - Public access: Yes (for initial setup, restrict later)
   - VPC security group: Create new
   - Database name: valorant_arena
4. Click "Create database"
5. Wait 5-10 minutes for creation
```

#### Step 2: Configure Security Group
```bash
1. Go to EC2 → Security Groups
2. Find the RDS security group
3. Edit inbound rules:
   - Type: MySQL/Aurora
   - Protocol: TCP
   - Port: 3306
   - Source: Your EC2 security group (will create later)
4. Save rules
```

#### Step 3: Get Database Endpoint
```bash
1. Go to RDS → Databases
2. Click on valorant-arena-db
3. Copy the Endpoint (e.g., valorant-arena-db.xxxxx.us-east-1.rds.amazonaws.com)
4. Save this for backend configuration
```

#### Step 4: Initialize Database
```bash
# Connect to RDS from your local machine
mysql -h valorant-arena-db.xxxxx.us-east-1.rds.amazonaws.com -u admin -p

# Run your SQL scripts
source /path/to/create_test_users.sql
source /path/to/create_test_tournament.sql
source /path/to/create_test_team.sql
```

### Option B: MySQL on EC2 (Alternative, uses same EC2 instance)
```bash
# Install MySQL on EC2 instance
sudo yum install mysql-server -y
sudo systemctl start mysqld
sudo systemctl enable mysqld

# Secure installation
sudo mysql_secure_installation

# Create database
mysql -u root -p
CREATE DATABASE valorant_arena;
```

---

## Backend Deployment

### Step 1: Create EC2 Instance

```bash
1. Go to AWS Console → EC2
2. Click "Launch Instance"
3. Configure:
   - Name: valorant-arena-backend
   - AMI: Amazon Linux 2023 (Free tier eligible)
   - Instance type: t2.micro (1 vCPU, 1GB RAM)
   - Key pair: Create new key pair
     - Name: valorant-arena-key
     - Type: RSA
     - Format: .pem (for SSH)
     - Download and save securely
   - Network settings:
     - VPC: Default
     - Auto-assign public IP: Enable
     - Security group: Create new
       - Name: valorant-arena-backend-sg
       - Rules:
         * SSH (22) - Your IP
         * HTTP (80) - Anywhere
         * HTTPS (443) - Anywhere
         * Custom TCP (8080) - Anywhere (for Spring Boot)
   - Storage: 8 GB gp3 (free tier: up to 30GB)
4. Click "Launch instance"
```

### Step 2: Connect to EC2 Instance

```bash
# Windows (using PowerShell)
# First, set permissions on key file
icacls "valorant-arena-key.pem" /inheritance:r
icacls "valorant-arena-key.pem" /grant:r "%username%:R"

# Connect via SSH
ssh -i "valorant-arena-key.pem" ec2-user@<EC2-PUBLIC-IP>

# Or use PuTTY:
# 1. Convert .pem to .ppk using PuTTYgen
# 2. Use PuTTY to connect with .ppk file
```

### Step 3: Install Java and Maven on EC2

```bash
# Update system
sudo yum update -y

# Install Java 17
sudo yum install java-17-amazon-corretto-devel -y

# Verify Java installation
java -version

# Install Maven
sudo wget https://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
sudo yum install -y apache-maven

# Verify Maven installation
mvn -version

# Install Git
sudo yum install git -y
```

### Step 4: Deploy Backend Application

```bash
# Clone your repository
cd /home/ec2-user
git clone https://github.com/YOUR_USERNAME/windsurf-project.git
cd windsurf-project/backend

# Create environment file with your configuration
nano .env.production

# Add these values (replace with your actual values):
DB_URL=jdbc:mysql://valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
DB_USERNAME=admin
DB_PASSWORD=YOUR_RDS_PASSWORD

JWT_SECRET=your-production-jwt-secret-minimum-256-bits

# CORS settings - Use your EC2 public IP/DNS (no domain needed)
# Get your EC2 public IP from AWS Console or run: curl http://checkip.amazonaws.com
CORS_ORIGINS=http://223.185.30.87:4200,http://ec2-XX-XX-XX-XX.eu-north-1.compute.amazonaws.com:4200

# Or if testing locally, keep localhost:
# CORS_ORIGINS=http://localhost:4200

RIOT_API_KEY=your-riot-api-key
DISCORD_CLIENT_ID=your-discord-client-id
DISCORD_CLIENT_SECRET=your-discord-client-secret
DISCORD_BOT_TOKEN=your-discord-bot-token

SPRING_PROFILES_ACTIVE=prod

# Load environment variables
source .env.production

# Build the application
mvn clean package -DskipTests

# The JAR file will be in target/valorant-arena-0.0.1-SNAPSHOT.jar
```

### Step 5: Create Systemd Service for Backend

```bash
# Create service file
sudo nano /etc/systemd/system/valorant-arena.service

# Add this content:
```

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

**Note:** The JAR filename is `valorant-arena-backend-1.0.0.jar` (from pom.xml: artifactId + version)

```bash
# Reload systemd, enable and start service
sudo systemctl daemon-reload
sudo systemctl enable valorant-arena
sudo systemctl start valorant-arena

# Check status
sudo systemctl status valorant-arena

# View logs
sudo journalctl -u valorant-arena -f
```

### Step 6: Setup Nginx as Reverse Proxy

```bash
# Install Nginx
sudo yum install nginx -y

# Configure Nginx
sudo nano /etc/nginx/conf.d/valorant-arena.conf

# Add this configuration:
```

```nginx
server {
    listen 80;
    # Use your EC2 public IP instead of domain name
    # Replace with your actual EC2 IP: 223.185.30.87
    server_name 223.185.30.87;
    
    # Or use EC2 public DNS:
    # server_name ec2-223-185-30-87.eu-north-1.compute.amazonaws.com;
    
    # Or use underscore to accept any hostname (for testing):
    # server_name _;

    # Backend API proxy
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # File uploads proxy
    location /uploads/ {
        proxy_pass http://localhost:8080/uploads/;
        proxy_set_header Host $host;
    }
    
    # Swagger UI (optional, for testing)
    location /swagger-ui/ {
        proxy_pass http://localhost:8080/swagger-ui/;
        proxy_set_header Host $host;
    }
    
    # Frontend (if serving from same server)
    # Uncomment if you deploy Angular to /var/www/html
    # location / {
    #     root /var/www/html;
    #     try_files $uri $uri/ /index.html;
    # }
}
```

**Note:** Replace `223.185.30.87` with your actual EC2 public IP, or use `server_name _;` to accept any hostname.

```bash
# Test Nginx configuration
sudo nginx -t

# Start Nginx
sudo systemctl start nginx
sudo systemctl enable nginx

# Check status
sudo systemctl status nginx
```

---

## Frontend Deployment

### Option A: S3 + CloudFront (Recommended - Free Tier)

#### Step 1: Build Frontend

```bash
# On your local machine
cd frontend

# Update environment for production
nano src/environments/environment.prod.ts

# Set API URL to your EC2 public IP or domain:
```

```typescript
export const environment = {
  production: true,
  apiUrl: 'http://YOUR-EC2-PUBLIC-IP/api',
  // Or with domain:
  // apiUrl: 'https://api.yourdomain.com/api'
};
```

```bash
# Build for production
npm run build --prod

# This creates dist/ folder with optimized files
```

#### Step 2: Create S3 Bucket

```bash
1. Go to AWS Console → S3
2. Click "Create bucket"
3. Configure:
   - Bucket name: valorant-arena-frontend (must be globally unique)
   - Region: us-east-1 (or your preferred region)
   - Block all public access: UNCHECK (we need public access)
   - Bucket Versioning: Enable (optional, for rollback)
   - Tags: Add tags for organization
4. Click "Create bucket"
```

#### Step 3: Upload Frontend Files

```bash
# Using AWS CLI
cd dist/valorant-arena

# Upload all files
aws s3 sync . s3://valorant-arena-frontend/ --acl public-read

# Or use AWS Console:
# 1. Open bucket
# 2. Click "Upload"
# 3. Drag and drop all files from dist/valorant-arena
# 4. Set permissions: Grant public read access
```

#### Step 4: Enable Static Website Hosting

```bash
1. Go to bucket → Properties
2. Scroll to "Static website hosting"
3. Click "Edit"
4. Enable static website hosting
5. Index document: index.html
6. Error document: index.html (for Angular routing)
7. Save changes
8. Note the endpoint URL (e.g., http://valorant-arena-frontend.s3-website-us-east-1.amazonaws.com)
```

#### Step 5: Configure Bucket Policy

```bash
1. Go to bucket → Permissions
2. Click "Bucket Policy"
3. Add this policy:
```

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::valorant-arena-frontend/*"
        }
    ]
}
```

#### Step 6: Setup CloudFront Distribution

```bash
1. Go to AWS Console → CloudFront
2. Click "Create Distribution"
3. Configure:
   - Origin domain: Select your S3 bucket
   - Origin path: Leave empty
   - Name: valorant-arena-cdn
   - Origin access: Public
   - Viewer protocol policy: Redirect HTTP to HTTPS
   - Allowed HTTP methods: GET, HEAD, OPTIONS
   - Cache policy: CachingOptimized
   - Price class: Use all edge locations (best performance)
   - Alternate domain names (CNAMEs): yourdomain.com, www.yourdomain.com
   - Custom SSL certificate: Request certificate (see SSL section)
   - Default root object: index.html
4. Click "Create distribution"
5. Wait 15-20 minutes for deployment
6. Note the CloudFront domain name (e.g., d111111abcdef8.cloudfront.net)
```

#### Step 7: Configure Error Pages for Angular Routing

```bash
1. Go to CloudFront → Your distribution
2. Click "Error Pages" tab
3. Create custom error response:
   - HTTP error code: 403
   - Customize error response: Yes
   - Response page path: /index.html
   - HTTP response code: 200
4. Repeat for error code 404
```

### Option B: Nginx on EC2 (Alternative)

```bash
# On EC2 instance
sudo mkdir -p /var/www/valorant-arena

# Upload frontend files (from local machine)
scp -i "valorant-arena-key.pem" -r dist/valorant-arena/* ec2-user@<EC2-IP>:/tmp/

# Move files (on EC2)
sudo mv /tmp/* /var/www/valorant-arena/

# Update Nginx config
sudo nano /etc/nginx/conf.d/valorant-arena.conf

# Add frontend location:
```

```nginx
server {
    listen 80;
    server_name your-domain.com;

    root /var/www/valorant-arena;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        # ... proxy settings
    }
}
```

```bash
# Restart Nginx
sudo systemctl restart nginx
```

---

## Domain & SSL Setup

### Step 1: Get a Free Domain (Optional)

```bash
# Option 1: Freenom (Free domains)
1. Go to https://www.freenom.com
2. Search for available domain (e.g., valorantarena.tk, .ml, .ga, .cf, .gq)
3. Register for free (12 months, renewable)

# Option 2: Use AWS Route 53 ($0.50/month + $0.50/domain)
# Option 3: Buy domain from Namecheap, GoDaddy, etc.
```

### Step 2: Configure Route 53 (If using custom domain)

```bash
1. Go to AWS Console → Route 53
2. Click "Create hosted zone"
3. Domain name: yourdomain.com
4. Type: Public hosted zone
5. Click "Create hosted zone"
6. Note the nameservers (NS records)
7. Update nameservers at your domain registrar
```

#### Create DNS Records:

```bash
# A Record for backend (EC2)
1. Click "Create record"
2. Record name: api
3. Record type: A
4. Value: <EC2-PUBLIC-IP>
5. TTL: 300
6. Create record

# CNAME for frontend (CloudFront)
1. Click "Create record"
2. Record name: www
3. Record type: CNAME
4. Value: <CLOUDFRONT-DOMAIN>
5. Create record

# A Record for root domain (CloudFront)
1. Click "Create record"
2. Record name: (leave empty for root)
3. Record type: A
4. Alias: Yes
5. Route traffic to: CloudFront distribution
6. Select your distribution
7. Create record
```

### Step 3: Setup SSL Certificate (Free with AWS Certificate Manager)

```bash
1. Go to AWS Console → Certificate Manager
2. Click "Request certificate"
3. Certificate type: Public certificate
4. Domain names:
   - yourdomain.com
   - *.yourdomain.com (wildcard for subdomains)
5. Validation method: DNS validation
6. Click "Request"
7. Click on certificate → "Create records in Route 53"
8. Wait 5-30 minutes for validation
```

#### Update CloudFront to use SSL:

```bash
1. Go to CloudFront → Your distribution
2. Click "Edit"
3. Alternate domain names: yourdomain.com, www.yourdomain.com
4. Custom SSL certificate: Select your ACM certificate
5. Save changes
6. Wait for deployment
```

#### Update Nginx for SSL (Backend):

```bash
# Install Certbot
sudo yum install certbot python3-certbot-nginx -y

# Get SSL certificate
sudo certbot --nginx -d api.yourdomain.com

# Follow prompts:
# - Enter email
# - Agree to terms
# - Choose redirect HTTP to HTTPS: Yes

# Auto-renewal is configured automatically
# Test renewal:
sudo certbot renew --dry-run
```

---

## Monitoring & Maintenance

### CloudWatch Monitoring

```bash
1. Go to AWS Console → CloudWatch
2. Create alarms:
   - EC2 CPU Utilization > 80%
   - RDS CPU Utilization > 80%
   - RDS Free Storage Space < 2GB
3. Set SNS notifications to your email
```

### Automated Backups

#### RDS Automated Backups:
```bash
# Already configured during RDS creation
# Retention: 7 days (free tier)
# Backup window: Automatic
```

#### EC2 Snapshots:
```bash
1. Go to EC2 → Elastic Block Store → Snapshots
2. Select your EC2 volume
3. Actions → Create Snapshot
4. Schedule weekly snapshots using AWS Backup (free tier: 10GB)
```

### Log Management

```bash
# View backend logs
sudo journalctl -u valorant-arena -f

# View Nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# Setup CloudWatch Logs (optional)
sudo yum install amazon-cloudwatch-agent -y
# Configure agent to send logs to CloudWatch
```

---

## Cost Optimization

### Free Tier Limits (12 months):
- ✅ EC2: 750 hours/month (1 t2.micro instance 24/7)
- ✅ RDS: 750 hours/month (1 db.t2.micro instance 24/7)
- ✅ S3: 5GB storage, 20K GET, 2K PUT
- ✅ CloudFront: 50GB transfer, 2M requests
- ✅ Data Transfer: 15GB out/month

### Tips to Stay Free:
1. **Use only ONE EC2 instance** (combine backend + database if needed)
2. **Use only ONE RDS instance** (or MySQL on EC2)
3. **Monitor usage** in AWS Billing Dashboard
4. **Set up billing alerts** at $1, $5, $10
5. **Stop instances** when not in use (dev/test)
6. **Use CloudFront** for static content (reduces EC2 bandwidth)
7. **Optimize images** and assets (reduces S3 storage)
8. **Enable S3 lifecycle policies** (delete old logs)

### After 12 Months (Estimated Costs):
- EC2 t2.micro: ~$8-10/month
- RDS db.t2.micro: ~$15-20/month
- S3: ~$0.50-2/month
- CloudFront: ~$1-5/month
- Route 53: $0.50/month
- **Total: ~$25-40/month**

### Cost Reduction Strategies:
1. Use **Reserved Instances** (save 30-70%)
2. Use **Spot Instances** for non-critical workloads
3. Use **AWS Lightsail** ($3.50/month for 512MB RAM)
4. Use **Heroku Free Tier** for backend (500MB RAM)
5. Use **Vercel/Netlify** for frontend (free tier)

---

## Troubleshooting

### Common Issues:

#### 1. Backend not accessible
```bash
# Check if service is running
sudo systemctl status valorant-arena

# Check logs
sudo journalctl -u valorant-arena -n 100

# Check if port 8080 is listening
sudo netstat -tulpn | grep 8080

# Check security group allows port 8080
# AWS Console → EC2 → Security Groups
```

#### 2. Database connection failed
```bash
# Test connection from EC2
mysql -h <RDS-ENDPOINT> -u admin -p

# Check security group allows EC2 IP
# Check RDS endpoint is correct in application.properties
```

#### 3. Frontend not loading
```bash
# Check S3 bucket policy allows public read
# Check CloudFront distribution is deployed
# Clear browser cache
# Check CORS settings in backend
```

#### 4. CORS errors
```bash
# Update backend SecurityConfig.java
# Add your CloudFront domain to allowed origins
# Rebuild and redeploy backend
```

#### 5. SSL certificate issues
```bash
# Check certificate is validated in ACM
# Check CloudFront is using correct certificate
# Check DNS records are correct
# Wait 24-48 hours for DNS propagation
```

---

## Deployment Checklist

### Pre-Deployment:
- [ ] AWS account created and verified
- [ ] IAM user created with necessary permissions
- [ ] AWS CLI installed and configured
- [ ] Code pushed to GitHub repository
- [ ] Database scripts ready
- [ ] Environment variables documented

### Backend Deployment:
- [ ] EC2 instance created and running
- [ ] Security groups configured
- [ ] Java and Maven installed
- [ ] Application built successfully
- [ ] Systemd service created and running
- [ ] Nginx configured as reverse proxy
- [ ] Database connection tested

### Database Deployment:
- [ ] RDS instance created
- [ ] Security group allows EC2 access
- [ ] Database initialized with schemas
- [ ] Test data loaded
- [ ] Backups configured

### Frontend Deployment:
- [ ] Production build created
- [ ] S3 bucket created and configured
- [ ] Files uploaded to S3
- [ ] CloudFront distribution created
- [ ] Error pages configured
- [ ] CORS configured in backend

### Domain & SSL:
- [ ] Domain registered/configured
- [ ] Route 53 hosted zone created
- [ ] DNS records created
- [ ] SSL certificate requested and validated
- [ ] CloudFront using SSL certificate
- [ ] Backend using SSL (Certbot)

### Post-Deployment:
- [ ] All endpoints tested
- [ ] User registration tested
- [ ] Tournament creation tested
- [ ] Team creation tested
- [ ] File uploads tested
- [ ] CloudWatch alarms configured
- [ ] Billing alerts set up
- [ ] Documentation updated

---

## Quick Start Commands

### Deploy Backend:
```bash
# SSH to EC2
ssh -i "valorant-arena-key.pem" ec2-user@<EC2-IP>

# Pull latest code
cd /home/ec2-user/windsurf-project/backend
git pull origin main

# Build and restart
mvn clean package -DskipTests
sudo systemctl restart valorant-arena
sudo systemctl status valorant-arena
```

### Deploy Frontend:
```bash
# Build locally
cd frontend
npm run build --prod

# Upload to S3
aws s3 sync dist/valorant-arena/ s3://valorant-arena-frontend/ --delete

# Invalidate CloudFront cache
aws cloudfront create-invalidation --distribution-id <DISTRIBUTION-ID> --paths "/*"
```

---

## Support & Resources

### AWS Documentation:
- EC2: https://docs.aws.amazon.com/ec2/
- RDS: https://docs.aws.amazon.com/rds/
- S3: https://docs.aws.amazon.com/s3/
- CloudFront: https://docs.aws.amazon.com/cloudfront/

### Useful Tools:
- AWS Free Tier Usage: https://console.aws.amazon.com/billing/home#/freetier
- AWS Calculator: https://calculator.aws/
- SSL Test: https://www.ssllabs.com/ssltest/

### Community:
- AWS Forums: https://forums.aws.amazon.com/
- Stack Overflow: https://stackoverflow.com/questions/tagged/aws
- Reddit: r/aws

---

## Next Steps

1. **Beta Testing Phase**:
   - Share URL with beta testers
   - Collect feedback
   - Monitor performance and errors
   - Fix bugs and optimize

2. **Scaling Preparation**:
   - Monitor AWS Free Tier usage
   - Plan for post-free-tier costs
   - Consider auto-scaling groups
   - Implement caching (Redis/ElastiCache)

3. **Production Readiness**:
   - Implement CI/CD pipeline (GitHub Actions)
   - Add comprehensive logging
   - Set up error tracking (Sentry)
   - Implement rate limiting
   - Add analytics (Google Analytics)

---

**Good luck with your deployment! 🚀**

For questions or issues, refer to the troubleshooting section or AWS documentation.
