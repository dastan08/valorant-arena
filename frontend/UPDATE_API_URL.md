# Update API URL for Production Deployment

## Current Situation

Your Angular services have hardcoded API URLs like:
```typescript
private apiUrl = 'http://localhost:8080/api/auth';
```

## Solution: Two Options

### Option 1: Quick Fix - Find and Replace (Easiest)

Before building for production, use find and replace:

**Find:**
```
http://localhost:8080/api
```

**Replace with:**
```
http://223.185.30.87/api
```

**In VS Code / Windsurf:**
1. Press `Ctrl + Shift + H` (Find and Replace in Files)
2. Find: `http://localhost:8080/api`
3. Replace: `http://223.185.30.87/api`
4. Files to include: `src/app/**/*.ts`
5. Click "Replace All"

**Then build:**
```bash
cd frontend
npm run build
```

---

### Option 2: Use Environment Configuration (Better)

I've created environment config files for you:
- `src/app/core/config/environment.ts` (development)
- `src/app/core/config/environment.prod.ts` (production)

**Step 1: Update your services to use the config**

Example for `auth.service.ts`:

**Before:**
```typescript
private apiUrl = 'http://localhost:8080/api/auth';
```

**After:**
```typescript
import { environment } from '../config/environment';

private apiUrl = `${environment.apiUrl}/auth`;
```

**Step 2: Update all services**

Services that need updating:
- `src/app/core/services/auth.service.ts`
- `src/app/core/services/team.service.ts`
- `src/app/core/services/tournament.service.ts`
- `src/app/core/services/scrim.service.ts`
- `src/app/core/services/tournament-moderator.service.ts`
- `src/app/services/matchmaking.service.ts`
- `src/app/services/valorant.service.ts`

**Step 3: Configure Angular to use production config**

Update `angular.json` to replace environment file during production build:

```json
"configurations": {
  "production": {
    "fileReplacements": [
      {
        "replace": "src/app/core/config/environment.ts",
        "with": "src/app/core/config/environment.prod.ts"
      }
    ],
    ...
  }
}
```

---

## Quick Deployment Steps

### For Testing (Option 1 - Fastest)

```bash
cd frontend

# 1. Find and replace API URL
# Use your editor to replace:
# http://localhost:8080/api → http://223.185.30.87/api

# 2. Build
npm run build

# 3. Deploy to S3 or EC2
aws s3 sync dist/valorant-arena-frontend s3://your-bucket/
```

---

### For Production (Option 2 - Recommended)

```bash
cd frontend

# 1. Update environment.prod.ts with your production API URL
nano src/app/core/config/environment.prod.ts

# Change apiUrl to your EC2 IP or domain:
# apiUrl: 'http://223.185.30.87/api'

# 2. Update services to use environment config (one-time setup)
# See Option 2 above

# 3. Build for production
npm run build --configuration=production

# 4. Deploy
aws s3 sync dist/valorant-arena-frontend s3://your-bucket/
```

---

## Your Current Setup

**EC2 Public IP:** `223.185.30.87`  
**Backend API:** `http://223.185.30.87:8080/api` (direct) or `http://223.185.30.87/api` (via Nginx)  
**Frontend Build Output:** `dist/valorant-arena-frontend/`

---

## Testing After Deployment

```bash
# Test API endpoint
curl http://223.185.30.87/api/auth/health

# Should return: {"status":"OK"}
```

---

## When You Get a Domain

Just update the API URL:

**In environment.prod.ts:**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://yourdomain.com/api'  // HTTPS with domain
};
```

**Rebuild and redeploy:**
```bash
npm run build --configuration=production
aws s3 sync dist/valorant-arena-frontend s3://your-bucket/
```

---

## Alternative: Use Build-Time Environment Variables

Create a build script that replaces the API URL:

**package.json:**
```json
{
  "scripts": {
    "build:prod": "ng build --configuration=production",
    "build:ec2": "API_URL=http://223.185.30.87/api ng build --configuration=production"
  }
}
```

Then use:
```bash
npm run build:ec2
```
