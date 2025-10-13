# Discord OAuth Setup Guide

## Overview
This guide explains how to set up Discord OAuth authentication for the Valorant Arena platform.

## Why Discord OAuth?

Since Riot Games doesn't provide OAuth/SSO for third-party applications, we use Discord OAuth as the primary authentication method. This is the industry standard for Valorant tournament platforms.

### Benefits:
- ✅ **No password management** - Users login with Discord
- ✅ **Verified accounts** - Discord handles verification
- ✅ **Community integration** - Link to Discord servers
- ✅ **Trusted platform** - Users already have Discord accounts
- ✅ **Riot ID linking** - Users can link their Riot ID after login

## Step 1: Create Discord Application

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Click "New Application"
3. Enter application name: "Valorant Arena"
4. Click "Create"

## Step 2: Configure OAuth2

1. In your application, go to "OAuth2" → "General"
2. Add Redirect URLs:
   - Development: `http://localhost:4200/auth/discord/callback`
   - Production: `https://yourdomain.com/auth/discord/callback`
3. Copy your **Client ID**
4. Copy your **Client Secret** (click "Reset Secret" if needed)

## Step 3: Configure Backend

Update `application.yml`:

```yaml
discord:
  client-id: YOUR_DISCORD_CLIENT_ID
  client-secret: YOUR_DISCORD_CLIENT_SECRET
  redirect-uri: http://localhost:4200/auth/discord/callback
```

Or use environment variables:
```bash
export DISCORD_CLIENT_ID=your_client_id
export DISCORD_CLIENT_SECRET=your_client_secret
export DISCORD_REDIRECT_URI=http://localhost:4200/auth/discord/callback
```

## Step 4: How It Works

### User Flow:

1. **User clicks "Login with Discord"**
   - Frontend redirects to Discord OAuth URL
   - User authorizes the application on Discord

2. **Discord redirects back with code**
   - URL: `http://localhost:4200/auth/discord/callback?code=AUTHORIZATION_CODE`
   - Frontend sends code to backend

3. **Backend exchanges code for token**
   - POST to Discord API with code
   - Receives access token

4. **Backend fetches user info**
   - GET Discord user data with access token
   - Returns: Discord ID, username, email, avatar

5. **Backend creates/updates user**
   - If Discord ID exists: Login existing user
   - If new: Create user account automatically
   - Valorant ID set to "NOT_LINKED"

6. **Backend returns JWT token**
   - User is logged in
   - Can now link Riot ID from profile

### API Endpoints:

#### GET /api/auth/discord/url
Returns the Discord OAuth authorization URL.

**Response:**
```json
{
  "url": "https://discord.com/api/oauth2/authorize?client_id=..."
}
```

#### POST /api/auth/discord/callback
Exchanges Discord authorization code for JWT token.

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

## Step 5: Frontend Implementation

### Login Page

Add "Login with Discord" button:

```typescript
loginWithDiscord() {
  // Get Discord OAuth URL from backend
  this.http.get<any>('http://localhost:8080/api/auth/discord/url')
    .subscribe(response => {
      // Redirect to Discord
      window.location.href = response.url;
    });
}
```

### Callback Page

Create `discord-callback.component.ts`:

```typescript
ngOnInit() {
  // Get code from URL
  const code = this.route.snapshot.queryParams['code'];
  
  if (code) {
    // Send code to backend
    this.http.post<any>('http://localhost:8080/api/auth/discord/callback', { code })
      .subscribe(response => {
        if (response.success) {
          // Store JWT token
          localStorage.setItem('token', response.data.token);
          // Redirect to dashboard
          this.router.navigate(['/dashboard']);
        }
      });
  }
}
```

## Step 6: Linking Riot ID

After Discord login, users can link their Riot ID:

### Profile Page

```html
<div *ngIf="user.valorantId === 'NOT_LINKED'">
  <p>Link your Riot account to participate in tournaments</p>
  <button (click)="linkRiotAccount()">Link Riot Account</button>
</div>
```

### Link Riot Account

```typescript
linkRiotAccount() {
  const riotId = prompt('Enter your Riot ID (e.g., PlayerName#1234)');
  
  this.http.post('http://localhost:8080/api/user/link-riot', { valorantId: riotId })
    .subscribe(() => {
      this.toastr.success('Riot account linked successfully!');
      this.loadUserProfile();
    });
}
```

## Database Schema

### User Table Updates

```sql
ALTER TABLE users ADD COLUMN discord_id VARCHAR(255) UNIQUE;
ALTER TABLE users ADD COLUMN discord_username VARCHAR(255);
ALTER TABLE users ADD COLUMN discord_avatar VARCHAR(255);
```

### User Fields:

- `discordId` - Discord user ID (unique)
- `discordUsername` - Discord username
- `discordAvatar` - Discord avatar hash
- `valorantId` - Riot ID (can be "NOT_LINKED")
- `email` - From Discord or generated
- `username` - From Discord (with suffix if duplicate)

## Security Considerations

### 1. Client Secret Protection
- **Never expose** client secret in frontend
- Store in environment variables
- Use backend-only

### 2. State Parameter
- Add CSRF protection with state parameter
- Generate random state on frontend
- Verify on callback

### 3. Token Storage
- Store JWT in localStorage or httpOnly cookies
- Never store Discord access token in frontend
- Use short-lived JWT tokens

### 4. Scopes
Current scopes: `identify email`
- `identify` - Get Discord ID, username, avatar
- `email` - Get user's email address

## Testing

### Test Discord Login:

1. Start backend: `mvn spring-boot:run`
2. Start frontend: `ng serve`
3. Go to `http://localhost:4200/login`
4. Click "Login with Discord"
5. Authorize on Discord
6. Should redirect back and login

### Test Riot ID Linking:

1. Login with Discord
2. Go to profile page
3. Click "Link Riot Account"
4. Enter Riot ID: `PlayerName#1234`
5. Should update profile

## Troubleshooting

### Error: "Invalid redirect_uri"
- Check Discord application settings
- Ensure redirect URI matches exactly
- Include protocol (http:// or https://)

### Error: "Invalid client"
- Check client ID and secret
- Ensure they're correctly configured
- Try regenerating client secret

### Error: "User already exists"
- Discord ID is unique
- User can only have one account per Discord
- Use different Discord account

### Error: "Valorant ID already linked"
- Each Riot ID can only be linked once
- User must unlink first
- Or use different Riot ID

## Production Deployment

### 1. Update Redirect URI
```yaml
discord:
  redirect-uri: https://yourdomain.com/auth/discord/callback
```

### 2. Add Production URL to Discord
- Go to Discord Developer Portal
- Add production redirect URI
- Save changes

### 3. Use Environment Variables
```bash
export DISCORD_CLIENT_ID=prod_client_id
export DISCORD_CLIENT_SECRET=prod_client_secret
export DISCORD_REDIRECT_URI=https://yourdomain.com/auth/discord/callback
```

### 4. Enable HTTPS
- Discord requires HTTPS in production
- Use SSL certificate
- Configure reverse proxy (nginx)

## Additional Features

### 1. Discord Server Integration
- Add bot to Discord server
- Verify users are in server
- Assign roles based on rank

### 2. Discord Rich Presence
- Show in-game status
- Display current match
- Show tournament participation

### 3. Discord Notifications
- Match reminders via DM
- Tournament updates
- Team invitations

## Related Documentation

- [Discord OAuth2 Documentation](https://discord.com/developers/docs/topics/oauth2)
- [Discord API Reference](https://discord.com/developers/docs/reference)
- `RIOT_API_SETUP.md` - Riot API configuration
- `README.md` - General project documentation

## Support

For issues with Discord OAuth:
1. Check Discord Developer Portal
2. Verify redirect URIs
3. Check backend logs
4. Test with different Discord account
5. Regenerate client secret if needed
