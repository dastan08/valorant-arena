# Tournament Banner & Logo URL Fix

## Problem
Tournament banners and team logos were trying to load from S3 bucket (localhost) instead of EC2 backend server, resulting in 404 errors.

## Root Cause
HTML templates had hardcoded `http://localhost:8080` URLs for images. When deployed to S3, these relative paths were trying to load from the S3 bucket instead of the EC2 backend where the images are actually stored.

## Solution
Created helper methods in components to construct full URLs using `environment.apiUrl`:

```typescript
getBannerUrl(bannerUrl: string | undefined): string {
  if (!bannerUrl) return '';
  if (bannerUrl.startsWith('http')) return bannerUrl;
  return `${environment.apiUrl.replace('/api', '')}${bannerUrl}`;
}

getTeamLogoUrl(logoUrl: string | undefined): string {
  if (!logoUrl || logoUrl === 'default-team-logo.png') return 'assets/default-team-logo.png';
  if (logoUrl.startsWith('http')) return logoUrl;
  return `${environment.apiUrl.replace('/api', '')}/uploads/${logoUrl}`;
}
```

## Files Fixed

### Components (TypeScript):
1. ✅ `tournaments.component.ts` - Added `getBannerUrl()` method
2. ✅ `organizer-dashboard.component.ts` - Added `getBannerUrl()` method
3. ✅ `tournament-detail.component.ts` - Added `getBannerUrl()` and `getTeamLogoUrl()` methods
4. ✅ `team-management.component.ts` - Added `getTeamLogoUrl()` method

### Templates (HTML):
1. ✅ `tournaments.component.html` - Changed to use `getBannerUrl()`
2. ✅ `organizer-dashboard.component.html` - Changed to use `getBannerUrl()`
3. ✅ `tournament-detail.component.html` - Changed to use `getBannerUrl()` and `getTeamLogoUrl()`
4. ✅ `team-management.component.html` - Changed to use `getTeamLogoUrl()`

## How It Works

**Before (Hardcoded):**
```html
<img [src]="'http://localhost:8080' + tournament.banner">
```
Result: `http://localhost:8080/uploads/tournament-banners/abc.png`

**After (Environment-based):**
```html
<img [src]="getBannerUrl(tournament.banner)">
```
Result: 
- Development: `http://localhost:8080/uploads/tournament-banners/abc.png`
- Production: `http://13.201.185.135:8080/uploads/tournament-banners/abc.png`

## Deploy
```bash
cd frontend
ng build --configuration production
cd dist/valorant-arena-frontend
aws s3 sync . s3://valorant-arena-frontend/ --delete
```

Clear browser cache (Ctrl+Shift+R) and test!

## Verification
1. Open tournament list - banners should load from EC2
2. Open tournament detail - banner and team logos should load from EC2
3. Check Network tab - all image requests should go to `13.201.185.135:8080`
