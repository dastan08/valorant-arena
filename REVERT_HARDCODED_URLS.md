# Fix Hardcoded URLs - Use Environment Configuration

## Problem
Many services have hardcoded URLs (localhost:8080 or 13.201.185.135:8080) instead of using the environment configuration.

## Solution
All services should use `environment.apiUrl` from the environment files.

## Files That Need to Be Fixed

### Services (should use environment.apiUrl):
- ✅ `valorant.service.ts` - Already fixed
- ✅ `matchmaking.service.ts` - Already fixed
- ❌ `admin.service.ts` - Still has hardcoded URL
- ❌ `auth.service.ts` - Still has hardcoded URL
- ❌ `scrim.service.ts` - Still has hardcoded URL
- ❌ `team.service.ts` - Still has hardcoded URL
- ❌ `tournament.service.ts` - Still has hardcoded URL
- ❌ `tournament-moderator.service.ts` - Still has hardcoded URL

### Components (should use environment.apiUrl):
- ❌ `discord-callback.component.ts`
- ❌ `login.component.ts`
- ❌ `dashboard.component.ts`
- ❌ `profile.component.ts`
- ❌ `teams.component.ts`
- ❌ `team-detail-dialog.component.ts`
- ❌ `tournament-create.component.ts`

### HTML Templates (should use environment.apiUrl in component):
- ❌ `tournament-detail.component.html`
- ❌ `team-management.component.html`
- ❌ `organizer-dashboard.component.html`
- ❌ `tournaments.component.html`

## How to Fix

Replace all instances of:
```typescript
private apiUrl = 'http://localhost:8080/api/...';
```

With:
```typescript
import { environment } from '../core/config/environment';
private apiUrl = `${environment.apiUrl}/...`;
```

## Environment Files
- `environment.ts` - Development: `http://localhost:8080/api`
- `environment.prod.ts` - Production: `http://13.201.185.135:8080/api`

Angular will automatically use the correct environment based on build configuration.
