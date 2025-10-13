# Discord OAuth Quick Start Guide

## ✅ What's Been Implemented

### Backend:
- ✅ Discord OAuth DTOs (`DiscordUserDto`, `DiscordTokenDto`)
- ✅ `DiscordOAuthService` - Handles OAuth flow
- ✅ Discord login endpoints in `AuthController`
- ✅ `AuthService.loginWithDiscord()` - Auto-creates users
- ✅ User entity updated with Discord fields
- ✅ Configuration in `application.yml`

### Frontend:
- ✅ "Sign in with Discord" button on login page
- ✅ Discord callback component
- ✅ Automatic token storage and redirect

## 🚀 Setup Steps

### 1. Create Discord Application

1. Go to https://discord.com/developers/applications
2. Click "New Application"
3. Name: "Valorant Arena"
4. Go to "OAuth2" → "General"
5. Add Redirect URL: `http://localhost:4200/auth/discord/callback`
6. Copy **Client ID** and **Client Secret**

### 2. Configure Backend

Update `backend/src/main/resources/application.yml`:

```yaml
discord:
  client-id: YOUR_DISCORD_CLIENT_ID_HERE
  client-secret: YOUR_DISCORD_CLIENT_SECRET_HERE
  redirect-uri: http://localhost:4200/auth/discord/callback
```

### 3. Update Frontend Routing

Add route to `app-routing.module.ts`:

```typescript
import { DiscordCallbackComponent } from './pages/auth/discord-callback/discord-callback.component';

const routes: Routes = [
  // ... existing routes
  { path: 'auth/discord/callback', component: DiscordCallbackComponent },
];
```

### 4. Declare Component

Add to `app.module.ts`:

```typescript
import { DiscordCallbackComponent } from './pages/auth/discord-callback/discord-callback.component';

@NgModule({
  declarations: [
    // ... existing components
    DiscordCallbackComponent,
  ],
})
```

### 5. Update Database

The database will auto-update with new columns when you restart the backend (if using `ddl-auto: update`):

- `discord_id` - Discord user ID (unique)
- `discord_username` - Discord username  
- `discord_avatar` - Discord avatar hash

Or run manually:
```sql
ALTER TABLE users ADD COLUMN discord_id VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN discord_username VARCHAR(255);
ALTER TABLE users ADD COLUMN discord_avatar VARCHAR(255);
```

## 🎯 How It Works

### User Flow:

1. **User clicks "Sign in with Discord"**
   ```
   GET /api/auth/discord/url
   → Returns Discord OAuth URL
   → Redirects to Discord
   ```

2. **User authorizes on Discord**
   ```
   Discord redirects to: http://localhost:4200/auth/discord/callback?code=...
   ```

3. **Frontend sends code to backend**
   ```
   POST /api/auth/discord/callback
   Body: { "code": "AUTHORIZATION_CODE" }
   ```

4. **Backend processes**
   - Exchanges code for access token
   - Fetches Discord user info
   - Creates/updates user in database
   - Returns JWT token

5. **User is logged in**
   - Token stored in localStorage
   - Redirected to dashboard

## 📝 API Endpoints

### GET /api/auth/discord/url
Returns Discord OAuth authorization URL.

**Response:**
```json
{
  "success": true,
  "message": "Discord OAuth URL",
  "data": "https://discord.com/api/oauth2/authorize?client_id=..."
}
```

### POST /api/auth/discord/callback
Exchanges Discord code for JWT token.

**Request:**
```json
{
  "code": "DISCORD_AUTHORIZATION_CODE"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "username": "DiscordUser123",
    "email": "user@example.com",
    "role": "USER"
  }
}
```

## 🔗 Linking Riot ID

After Discord login, users need to link their Riot ID:

### Profile Page Update

Users with `valorantId === "NOT_LINKED"` should see:

```html
<div *ngIf="user.valorantId === 'NOT_LINKED'" class="bg-yellow-900 bg-opacity-50 border border-yellow-500 rounded-md p-4 mb-4">
  <p class="text-yellow-300 mb-2">⚠️ Link your Riot account to participate in tournaments</p>
  <button (click)="linkRiotAccount()" class="bg-valorant-red hover:bg-red-600 text-white font-bold py-2 px-4 rounded">
    Link Riot Account
  </button>
</div>
```

### Link Riot Account Method

```typescript
linkRiotAccount() {
  const riotId = prompt('Enter your Riot ID (e.g., PlayerName#1234)');
  
  if (riotId && riotId.includes('#')) {
    this.http.post('http://localhost:8080/api/user/link-riot', { valorantId: riotId })
      .subscribe({
        next: () => {
          this.toastr.success('Riot account linked successfully!');
          this.loadUserProfile();
        },
        error: (error) => {
          this.toastr.error(error.error?.message || 'Failed to link Riot account');
        }
      });
  } else {
    this.toastr.error('Invalid Riot ID format. Use: GameName#TAG');
  }
}
```

## 🧪 Testing

### Test Discord Login:

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `ng serve`
3. Go to `http://localhost:4200/login`
4. Click "Sign in with Discord"
5. Authorize on Discord
6. Should redirect back and login automatically

### Expected Behavior:

- ✅ Redirects to Discord OAuth
- ✅ User authorizes application
- ✅ Redirects to callback URL
- ✅ Shows loading spinner
- ✅ Stores JWT token
- ✅ Redirects to dashboard
- ✅ User is logged in

## 🐛 Troubleshooting

### "Invalid redirect_uri"
- Check Discord app settings
- Ensure redirect URI matches exactly: `http://localhost:4200/auth/discord/callback`

### "Invalid client"
- Verify client ID and secret in `application.yml`
- Try regenerating client secret in Discord portal

### "Authorization code expired"
- Code is single-use and expires quickly
- Don't refresh the callback page
- Start login process again

### "User already exists"
- Each Discord account can only create one user
- Use different Discord account or login with existing account

## 🎨 UI Features

### Discord Button Style:
- Indigo background (`bg-indigo-600`)
- Discord logo SVG
- Hover effects
- Full width button

### Loading State:
- Spinning loader
- "Completing Discord login..." message
- Automatic redirect on success

## 📚 Related Documentation

- `DISCORD_OAUTH_SETUP.md` - Detailed setup guide
- `README.md` - General project documentation
- [Discord OAuth2 Docs](https://discord.com/developers/docs/topics/oauth2)

## 🚀 Next Steps

1. Set up Discord application
2. Configure client ID and secret
3. Add routing for callback component
4. Test the flow
5. Implement Riot ID linking
6. Deploy to production

## 💡 Tips

- Keep client secret secure (never commit to git)
- Use environment variables in production
- Test with multiple Discord accounts
- Add error handling for edge cases
- Consider adding Discord server verification
- Implement "Link Discord" for existing users

## ✨ Benefits

- ✅ No password management
- ✅ Verified Discord accounts
- ✅ One-click login
- ✅ Community integration ready
- ✅ Industry standard approach
- ✅ Riot ID linking after login
