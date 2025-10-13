# Sign in with Riot Account Feature

## Overview
Users can now sign in directly using their Riot account without needing to register first. The system automatically creates an account if the Riot ID doesn't exist in the database.

## Features

### One-Click Riot Login
- **No registration required** - Users can sign in immediately with their Riot ID
- **Auto-account creation** - New accounts are created automatically
- **Verified by default** - Riot-authenticated users are marked as verified
- **Optional rank selection** - Users can specify their rank during login

### User Experience
1. User clicks "Sign in with Riot Account" on login page
2. Modal opens requesting Valorant ID
3. User enters Riot ID (e.g., `PlayerName#1234`)
4. Optionally selects current rank
5. System verifies account with Riot API
6. If account doesn't exist, creates new user automatically
7. User is logged in and redirected to dashboard

## How It Works

### Backend Flow

1. **Riot ID Verification**
   - Parse Riot ID into GameName and TAG
   - Call Riot API to verify account exists
   - Retrieve PUUID (Player Universal Unique ID)

2. **User Lookup**
   - Check if user exists with this Riot ID
   - If exists: Log them in
   - If not: Create new account

3. **Auto-Account Creation**
   - Username: `GameName_TAG` (e.g., `PlayerName_1234`)
   - Email: `PUUID@riot.local` (temporary)
   - Password: Encrypted PUUID
   - Riot ID: Stored and verified
   - Rank: User-selected or default (IRON)
   - Verified: `true`

4. **JWT Token Generation**
   - Generate authentication token
   - Return user data and token

### Frontend Flow

1. **Modal Display**
   - Beautiful Valorant-themed modal
   - Input for Riot ID
   - Dropdown for rank selection
   - Real-time validation

2. **API Call**
   - POST to `/api/auth/riot-login`
   - Send Riot ID and optional rank
   - Handle loading states

3. **Success Handling**
   - Store JWT token
   - Show success message
   - Redirect to dashboard

## API Endpoint

### POST /api/auth/riot-login

**Request:**
```json
{
  "valorantId": "PlayerName#1234",
  "rank": "GOLD"  // Optional
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "username": "PlayerName_1234",
    "email": "abc123-def456@riot.local",
    "role": "USER"
  }
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Riot account not found. Please check your Valorant ID.",
  "data": null
}
```

## Security Features

### Account Verification
- **Riot API validation** - Every Riot ID is verified with Riot Games
- **PUUID-based authentication** - Uses Riot's unique player ID
- **Automatic verification** - Riot-authenticated users are pre-verified

### Username Collision Handling
- If username exists, adds numeric suffix
- Example: `PlayerName_1234`, `PlayerName_12341`, `PlayerName_12342`
- Ensures unique usernames

### Password Security
- Password is encrypted PUUID
- Users can't login with traditional email/password
- Must use Riot login for Riot-created accounts

## User Account Details

### Auto-Generated Fields
```javascript
{
  username: "GameName_TAG",
  email: "PUUID@riot.local",
  password: "encrypted_PUUID",
  valorantId: "GameName#TAG",
  rank: "USER_SELECTED or IRON",
  role: "USER",
  isVerified: true,
  avatar: "default-avatar.png"
}
```

### Profile Completion
Users can later update:
- Real email address
- Avatar
- Password (for traditional login)
- Rank
- Other profile information

## UI/UX Features

### Login Page
- **Divider** - "Or continue with" separator
- **Riot button** - Prominent "Sign in with Riot Account" button
- **Shield icon** - Visual Riot branding

### Modal Design
- **Valorant theme** - Dark gray with red accents
- **Clear instructions** - Explains auto-account creation
- **Input validation** - Real-time format checking
- **Rank selector** - Optional dropdown for all ranks
- **Error messages** - Clear, actionable error feedback
- **Loading states** - "Verifying..." during API call

### Visual Elements
```
┌─────────────────────────────────┐
│  Sign in with Riot              │
│  ✕                              │
├─────────────────────────────────┤
│                                 │
│  Enter your Valorant Riot ID... │
│                                 │
│  Valorant ID                    │
│  ┌───────────────────────────┐ │
│  │ GameName#TAG              │ │
│  └───────────────────────────┘ │
│  Example: PlayerName#1234       │
│                                 │
│  Current Rank (Optional)        │
│  ┌───────────────────────────┐ │
│  │ Select your rank ▼        │ │
│  └───────────────────────────┘ │
│                                 │
│  ┌──────────┐  ┌──────────┐   │
│  │ Continue │  │  Cancel  │   │
│  └──────────┘  └──────────┘   │
└─────────────────────────────────┘
```

## Error Handling

### Common Errors

1. **"Please enter your Valorant ID"**
   - User didn't enter Riot ID
   - Solution: Enter valid Riot ID

2. **"Valorant ID must be in format: GameName#TAG"**
   - Missing `#` character
   - Solution: Use correct format

3. **"Riot account not found"**
   - Account doesn't exist in Riot's system
   - Solution: Check spelling and tag

4. **"Failed to sign in with Riot account"**
   - Network error or API issue
   - Solution: Try again or check Riot API key

## Benefits

### For Users
- ✅ **No registration hassle** - Instant access
- ✅ **One less password** - Use Riot credentials
- ✅ **Verified instantly** - No email confirmation
- ✅ **Quick onboarding** - Start playing immediately

### For Platform
- ✅ **Reduced friction** - Higher conversion rate
- ✅ **Verified users** - All Riot logins are verified
- ✅ **Authentic players** - Real Valorant accounts only
- ✅ **Better data** - Linked to actual Riot profiles

## Use Cases

### New User
1. Visits login page
2. Clicks "Sign in with Riot Account"
3. Enters Riot ID
4. Account created automatically
5. Logged in and ready to play

### Existing User (Riot-created)
1. Visits login page
2. Clicks "Sign in with Riot Account"
3. Enters same Riot ID
4. Logged in immediately

### Existing User (Email-created)
1. Can link Riot account from profile
2. Then use either login method

## Future Enhancements

Potential improvements:
1. **Update email** - Allow users to set real email
2. **Fetch rank automatically** - Get rank from Riot API
3. **Import stats** - Pull match history and stats
4. **Avatar sync** - Use Riot account avatar
5. **Friend import** - Import Riot friends list
6. **Match verification** - Verify tournament matches with Riot data

## Technical Implementation

### Backend Files
- `RiotLoginRequest.java` - Request DTO
- `AuthService.loginWithRiot()` - Business logic
- `AuthController.riotLogin()` - API endpoint
- `RiotApiService.verifyAccount()` - Riot API integration

### Frontend Files
- `login.component.html` - UI with modal
- `login.component.ts` - Logic and API calls
- `login.component.css` - Styling

## Testing

### Test Scenarios

1. **New Riot Account**
   - Enter valid Riot ID
   - Verify account created
   - Check auto-login works

2. **Existing Riot Account**
   - Enter previously used Riot ID
   - Verify login without creating duplicate

3. **Invalid Riot ID**
   - Enter non-existent Riot ID
   - Verify error message shown

4. **Format Validation**
   - Try without `#`
   - Verify validation error

5. **Rank Selection**
   - Test with and without rank
   - Verify rank saved correctly

## Configuration

### Required
- Riot API key must be configured
- See `RIOT_API_SETUP.md` for details

### Environment Variables
```yaml
riot:
  api:
    key: RGAPI-your-key-here
    base-url: https://api.riotgames.com
    account-url: https://americas.api.riotgames.com
```

## Notes

- **Email format**: `PUUID@riot.local` is temporary
- **Password**: Users should set real password later
- **Username**: Can be changed in profile settings
- **Verification**: Riot-authenticated users are pre-verified
- **Rate limits**: Subject to Riot API rate limits

## Support

### Common Questions

**Q: Can I change my email later?**
A: Yes, update it in profile settings.

**Q: What if my Riot ID changes?**
A: Update it in profile to re-verify.

**Q: Can I use both login methods?**
A: Yes, if you set a real email and password.

**Q: Is my Riot password stored?**
A: No, we only verify your Riot ID exists.

**Q: What if Riot API is down?**
A: Riot login won't work, use email login instead.

## Related Documentation

- `RIOT_API_SETUP.md` - Riot API configuration
- `RIOT_ACCOUNT_LINKING.md` - Link Riot account from profile
- `README.md` - General project documentation
