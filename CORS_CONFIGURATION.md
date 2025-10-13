# CORS Configuration Guide

## What is CORS?

CORS (Cross-Origin Resource Sharing) allows your frontend (Angular) to communicate with your backend (Spring Boot) when they're hosted on different domains/IPs.

---

## Configuration Options (No Domain Required)

### Option 1: Using EC2 Public IP (Recommended for Testing)

```bash
# Get your EC2 public IP
curl http://checkip.amazonaws.com

# Example output: 13.51.234.123

# Set CORS in .env.production:
CORS_ORIGINS=http://13.51.234.123:4200,http://13.51.234.123:8080
```

**Frontend URL:** `http://13.51.234.123:4200`  
**Backend URL:** `http://13.51.234.123:8080`

---

### Option 2: Using EC2 Public DNS

```bash
# Get your EC2 public DNS from AWS Console
# Example: ec2-13-51-234-123.eu-north-1.compute.amazonaws.com

# Set CORS in .env.production:
CORS_ORIGINS=http://ec2-13-51-234-123.eu-north-1.compute.amazonaws.com:4200
```

**Frontend URL:** `http://ec2-13-51-234-123.eu-north-1.compute.amazonaws.com:4200`  
**Backend URL:** `http://ec2-13-51-234-123.eu-north-1.compute.amazonaws.com:8080`

---

### Option 3: Localhost (For Local Development)

```bash
# Set CORS in .env.production or application-secrets.yml:
CORS_ORIGINS=http://localhost:4200,http://localhost:8080
```

**Frontend URL:** `http://localhost:4200`  
**Backend URL:** `http://localhost:8080`

---

### Option 4: Allow All Origins (⚠️ ONLY for Development/Testing)

```bash
# DANGER: This allows ANY website to access your API
# NEVER use this in production!
CORS_ORIGINS=*
```

---

## How to Set CORS

### Method 1: Environment Variables (Recommended)

**In `backend/.env.production`:**
```bash
CORS_ORIGINS=http://YOUR_EC2_IP:4200
```

**Then run:**
```bash
source .env.production
java -jar valorant-arena-backend-1.0.0.jar
```

---

### Method 2: Application Configuration

**In `backend/src/main/resources/application-secrets.yml`:**
```yaml
cors:
  allowed-origins: http://YOUR_EC2_IP:4200
```

---

### Method 3: System Environment Variables (EC2)

**On EC2 instance:**
```bash
# Add to ~/.bashrc or ~/.bash_profile
export CORS_ORIGINS="http://13.51.234.123:4200"

# Reload
source ~/.bashrc
```

---

## Multiple Origins

You can allow multiple origins separated by commas:

```bash
# Allow multiple IPs/domains
CORS_ORIGINS=http://localhost:4200,http://13.51.234.123:4200,http://ec2-13-51-234-123.eu-north-1.compute.amazonaws.com:4200

# Allow both HTTP and HTTPS
CORS_ORIGINS=http://yourdomain.com,https://yourdomain.com

# Allow subdomains
CORS_ORIGINS=https://app.yourdomain.com,https://api.yourdomain.com
```

---

## When You Get a Domain Later

### Step 1: Update DNS

1. Buy domain (e.g., from Namecheap, GoDaddy, Route53)
2. Create A record pointing to your EC2 IP:
   ```
   Type: A
   Name: @ (or www)
   Value: 13.51.234.123
   TTL: 300
   ```

### Step 2: Update CORS Configuration

```bash
# Update .env.production
CORS_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Restart backend
sudo systemctl restart valorant-arena
```

### Step 3: Update Frontend Configuration

**In `frontend/src/environments/environment.prod.ts`:**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://yourdomain.com/api'
};
```

### Step 4: Setup SSL/HTTPS (Required for Production)

```bash
# Install Certbot for free SSL certificate
sudo yum install certbot python3-certbot-nginx -y

# Get SSL certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Auto-renewal is configured automatically
```

---

## Common CORS Errors & Solutions

### Error: "Access-Control-Allow-Origin header is missing"

**Cause:** Backend CORS not configured or wrong origin

**Solution:**
```bash
# Check your CORS setting matches your frontend URL exactly
# Frontend: http://13.51.234.123:4200
# CORS must be: http://13.51.234.123:4200 (not https, not different port)
```

---

### Error: "CORS policy: No 'Access-Control-Allow-Origin' header"

**Cause:** Backend not running or wrong URL

**Solution:**
```bash
# Verify backend is running
curl http://YOUR_EC2_IP:8080/api/health

# Check backend logs
tail -f /var/log/valorant-arena/application.log
```

---

### Error: "Mixed Content" (HTTP/HTTPS mismatch)

**Cause:** Frontend is HTTPS but backend is HTTP

**Solution:**
```bash
# Both must be the same protocol
# Either both HTTP or both HTTPS

# For production, use HTTPS for both:
CORS_ORIGINS=https://yourdomain.com
```

---

## Testing CORS Configuration

### Test 1: Check Backend CORS Headers

```bash
# Replace with your EC2 IP
curl -H "Origin: http://13.51.234.123:4200" \
     -H "Access-Control-Request-Method: GET" \
     -X OPTIONS \
     http://13.51.234.123:8080/api/auth/health -v

# Look for these headers in response:
# Access-Control-Allow-Origin: http://13.51.234.123:4200
# Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS
```

### Test 2: Test from Frontend

**In browser console (F12):**
```javascript
fetch('http://YOUR_EC2_IP:8080/api/auth/health')
  .then(response => response.json())
  .then(data => console.log('Success:', data))
  .catch(error => console.error('CORS Error:', error));
```

---

## Quick Reference

### For Local Development
```bash
CORS_ORIGINS=http://localhost:4200
```

### For EC2 Testing (No Domain)
```bash
# Get your EC2 IP first
curl http://checkip.amazonaws.com

# Then use it
CORS_ORIGINS=http://YOUR_EC2_IP:4200
```

### For Production (With Domain)
```bash
CORS_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

### For Multiple Environments
```bash
CORS_ORIGINS=http://localhost:4200,http://YOUR_EC2_IP:4200,https://yourdomain.com
```

---

## Security Best Practices

✅ **DO:**
- Use specific origins (not `*`)
- Use HTTPS in production
- List only trusted domains
- Keep CORS configuration in environment variables

❌ **DON'T:**
- Use `*` in production
- Allow HTTP in production
- Hardcode CORS in source code
- Allow untrusted domains

---

## Current Configuration

Your current setup:
- **RDS Endpoint:** `valorant-arena-db.cf66qis4ur83.eu-north-1.rds.amazonaws.com`
- **Region:** `eu-north-1` (Stockholm)
- **Backend Port:** `8080`
- **Frontend Port:** `4200`

**Recommended CORS for testing:**
```bash
# Get your EC2 public IP and use:
CORS_ORIGINS=http://YOUR_EC2_PUBLIC_IP:4200
```

**When you deploy frontend to S3 + CloudFront later:**
```bash
CORS_ORIGINS=https://YOUR_CLOUDFRONT_URL
```
