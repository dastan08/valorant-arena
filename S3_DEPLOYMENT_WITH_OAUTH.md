# 🌐 S3 Deployment with OAuth Callbacks

## Overview

When deploying your frontend to S3/CloudFront, you need to update OAuth callback URLs for Discord and Riot authentication to work properly.

---

## 🔍 The Problem

**Local Development:**
- Frontend: `http://localhost:4200`
- Callbacks: `http://localhost:4200/auth/discord/callback`

**S3/CloudFront Production:**
- Frontend: `https://d1234567890abc.cloudfront.net`
- Callbacks: Need to be updated to CloudFront URL

---

## ✅ Complete Setup Guide

### **Step 1: Deploy Frontend to S3**

```bash
# Build frontend
cd frontend
npm run build

# Upload to S3
aws s3 sync dist/valorant-arena-frontend s3://your-bucket-name/
```

---

### **Step 2: Setup CloudFront (Recommended)**

1. **Go to AWS Console** → CloudFront → Create Distribution
2. **Origin Domain:** Select your S3 bucket
3. **Viewer Protocol Policy:** Redirect HTTP to HTTPS
4. **Default Root Object:** `index.html`
5. **Error Pages:** Add custom error response
   - HTTP Error Code: `403`
   - Response Page Path: `/index.html`
   - HTTP Response Code: `200`
6. **Create Distribution**

**Your CloudFront URL will be:**
```
https://d1234567890abc.cloudfront.net
```

---

### **Step 3: Update Discord Application**

1. Go to https://discord.com/developers/applications
2. Select your application: **Valorant Arena**
3. Go to **OAuth2** → **General**
4. **Redirects** → Add these URLs:

```
http://localhost:4200/auth/discord/callback
https://d1234567890abc.cloudfront.net/auth/discord/callback
```

Replace `d1234567890abc.cloudfront.net` with your actual CloudFront domain.

5. Click **Save Changes**

---

### **Step 4: Update Riot Application**

1. Go to https://developer.riotgames.com/
2. Login and go to your application
3. **Redirect URIs** → Add:

```
http://localhost:4200/auth/riot/callback
https://d1234567890abc.cloudfront.net/auth/riot/callback
```

4. Save changes

---

### **Step 5: Update Backend Configuration**

#### **Update `.env.production` on EC2:**

```bash
# SSH into EC2
ssh -i your-key.pem ec2-user@223.185.30.87

# Edit environment file
cd /home/ec2-user/valorant-arena/backend
nano .env.production
```

**Add/Update these lines:**

```bash
# Frontend URL (CloudFront)
FRONTEND_URL=https://d1234567890abc.cloudfront.net

# CORS - Allow CloudFront domain
CORS_ORIGINS=https://d1234567890abc.cloudfront.net

# Discord OAuth Callback
DISCORD_REDIRECT_URI=https://d1234567890abc.cloudfront.net/auth/discord/callback

# Riot OAuth Callback
RIOT_REDIRECT_URI=https://d1234567890abc.cloudfront.net/auth/riot/callback
```

**Complete `.env.production` example:**

```bash
# Database
DB_URL=jdbc:mysql://valorant-arena.cxy4ycyyyyvy.ap-south-1.rds.amazonaws.com:3306/valorant_arena?useSSL=true&requireSSL=true&serverTimezone=UTC
DB_USERNAME=admin
DB_PASSWORD=AGYX3h3NinSDKLx7a0D2

# JWT
JWT_SECRET=valorant-arena-super-secret-key-for-jwt-token-generation-minimum-256-bits-required

# CORS
CORS_ORIGINS=https://d1234567890abc.cloudfront.net

# Discord OAuth
DISCORD_CLIENT_ID=1426309142389325824
DISCORD_CLIENT_SECRET=FNRTqv2GcYprTPm6RzLpRKcimtOldQxi
DISCORD_BOT_TOKEN=MTQyNjU3MzU0NDEyMDEyNzY3MQ.Gb29dW.OoJEqvRP-lRuAHta53uTFqQjP5S_7qHbV9PVYw
DISCORD_REDIRECT_URI=https://d1234567890abc.cloudfront.net/auth/discord/callback

# Riot API
RIOT_API_KEY=your-riot-api-key
RIOT_CLIENT_ID=your-riot-client-id
RIOT_CLIENT_SECRET=your-riot-client-secret
RIOT_REDIRECT_URI=https://d1234567890abc.cloudfront.net/auth/riot/callback

# Spring Profile
SPRING_PROFILES_ACTIVE=prod
```

---

### **Step 6: Restart Backend**

```bash
# Reload environment variables
source .env.production

# Restart service
sudo systemctl restart valorant-arena

# Check status
sudo systemctl status valorant-arena
```

---

### **Step 7: Update Frontend Environment**

Update your frontend to use the CloudFront URL:

**`frontend/src/environments/environment.prod.ts`** (or use find-and-replace):

```typescript
export const environment = {
  production: true,
  apiUrl: 'http://223.185.30.87/api',  // Your EC2 backend
  frontendUrl: 'https://d1234567890abc.cloudfront.net'
};
```

**Rebuild and redeploy frontend:**

```bash
cd frontend
npm run build
aws s3 sync dist/valorant-arena-frontend s3://your-bucket-name/ --delete
```

---

## 🔄 Multiple Environment Support

### **For Development:**

`backend/src/main/resources/application-secrets.yml`:
```yaml
discord:
  redirect-uri: http://localhost:4200/auth/discord/callback

riot:
  oauth:
    redirect-uri: http://localhost:4200/auth/riot/callback
```

### **For Production:**

`backend/.env.production` on EC2:
```bash
DISCORD_REDIRECT_URI=https://d1234567890abc.cloudfront.net/auth/discord/callback
RIOT_REDIRECT_URI=https://d1234567890abc.cloudfront.net/auth/riot/callback
```

---

## 🎯 Testing OAuth Flow

### **Test Discord Login:**

1. Open: `https://d1234567890abc.cloudfront.net`
2. Click "Login with Discord"
3. Should redirect to Discord OAuth page
4. After authorization, should redirect back to: `https://d1234567890abc.cloudfront.net/auth/discord/callback`
5. Frontend should receive the auth token

### **Test Riot Login:**

1. Click "Login with Riot"
2. Should redirect to Riot OAuth page
3. After authorization, should redirect back to: `https://d1234567890abc.cloudfront.net/auth/riot/callback`

---

## 🐛 Troubleshooting

### **Issue: "redirect_uri_mismatch" error**

**Cause:** Callback URL doesn't match what's registered in Discord/Riot

**Solution:**
1. Check Discord Developer Portal → OAuth2 → Redirects
2. Make sure it exactly matches: `https://d1234567890abc.cloudfront.net/auth/discord/callback`
3. No trailing slash
4. HTTPS (not HTTP) for CloudFront

---

### **Issue: CORS error after OAuth redirect**

**Cause:** Backend CORS not configured for CloudFront domain

**Solution:**

Update `.env.production`:
```bash
CORS_ORIGINS=https://d1234567890abc.cloudfront.net
```

Restart backend:
```bash
sudo systemctl restart valorant-arena
```

---

### **Issue: OAuth works locally but not on S3**

**Checklist:**
- [ ] CloudFront URL added to Discord/Riot redirect URIs
- [ ] Backend `.env.production` updated with CloudFront URL
- [ ] Backend restarted after updating `.env.production`
- [ ] Frontend rebuilt and redeployed to S3
- [ ] CORS includes CloudFront domain

---

## 📋 Callback URL Formats

### **Local Development:**
```
http://localhost:4200/auth/discord/callback
http://localhost:4200/auth/riot/callback
```

### **S3 Static Website:**
```
http://valorant-arena-frontend.s3-website-ap-south-1.amazonaws.com/auth/discord/callback
http://valorant-arena-frontend.s3-website-ap-south-1.amazonaws.com/auth/riot/callback
```

### **CloudFront (Recommended):**
```
https://d1234567890abc.cloudfront.net/auth/discord/callback
https://d1234567890abc.cloudfront.net/auth/riot/callback
```

### **Custom Domain (Best):**
```
https://valorantarena.com/auth/discord/callback
https://valorantarena.com/auth/riot/callback
```

---

## 🌐 Custom Domain Setup (Optional)

If you want to use your own domain:

### **Step 1: Get a Domain**
- Buy from Route 53, Namecheap, GoDaddy, etc.

### **Step 2: Create SSL Certificate**
1. AWS Certificate Manager → Request Certificate
2. Domain: `valorantarena.com` and `*.valorantarena.com`
3. Validate via DNS

### **Step 3: Update CloudFront**
1. CloudFront → Your Distribution → Edit
2. **Alternate Domain Names (CNAMEs):** `valorantarena.com`, `www.valorantarena.com`
3. **SSL Certificate:** Select your ACM certificate
4. Save changes

### **Step 4: Update DNS**
1. Route 53 (or your DNS provider)
2. Create A record (Alias):
   - Name: `valorantarena.com`
   - Type: A - IPv4 address
   - Alias: Yes
   - Alias Target: Your CloudFront distribution

### **Step 5: Update OAuth Callbacks**

**Discord & Riot:**
```
https://valorantarena.com/auth/discord/callback
https://valorantarena.com/auth/riot/callback
```

**Backend `.env.production`:**
```bash
CORS_ORIGINS=https://valorantarena.com,https://www.valorantarena.com
DISCORD_REDIRECT_URI=https://valorantarena.com/auth/discord/callback
RIOT_REDIRECT_URI=https://valorantarena.com/auth/riot/callback
```

---

## ✅ Deployment Checklist

### **Frontend (S3/CloudFront):**
- [ ] Built with production configuration
- [ ] Uploaded to S3
- [ ] CloudFront distribution created
- [ ] CloudFront URL noted

### **OAuth Applications:**
- [ ] Discord redirect URI updated
- [ ] Riot redirect URI updated
- [ ] Both include CloudFront URL

### **Backend (EC2):**
- [ ] `.env.production` updated with CloudFront URL
- [ ] CORS includes CloudFront domain
- [ ] Discord/Riot redirect URIs set
- [ ] Backend restarted

### **Testing:**
- [ ] Frontend loads from CloudFront
- [ ] Discord login works
- [ ] Riot login works
- [ ] API calls work (CORS)
- [ ] No console errors

---

## 🎉 Success!

When everything is configured:

1. **User visits:** `https://d1234567890abc.cloudfront.net`
2. **Clicks "Login with Discord"**
3. **Redirects to:** `https://discord.com/oauth2/authorize?client_id=...&redirect_uri=https://d1234567890abc.cloudfront.net/auth/discord/callback`
4. **User authorizes**
5. **Discord redirects to:** `https://d1234567890abc.cloudfront.net/auth/discord/callback?code=...`
6. **Frontend exchanges code for token with backend**
7. **User is logged in!** ✅

---

## 📚 Related Guides

- `DEPLOYMENT_GUIDE.md` - Complete AWS deployment
- `PRODUCTION_BUILD_GUIDE.md` - Building production JAR
- `CORS_CONFIGURATION.md` - CORS setup
- `CONFIGURATION_GUIDE.md` - Configuration management
