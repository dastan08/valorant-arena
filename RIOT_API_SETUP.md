# Riot API Integration Setup

## Overview
This application integrates with the Riot Games API to verify Valorant player accounts during registration.

## Getting Your Riot API Key

### Development API Key (24-hour expiration)
1. Visit [Riot Developer Portal](https://developer.riotgames.com/)
2. Sign in with your Riot Games account
3. Click on "REGENERATE API KEY" under "Development API Key"
4. Copy the generated API key

### Production API Key (Permanent)
For production use, you need to apply for a production API key:
1. Go to [Riot Developer Portal](https://developer.riotgames.com/)
2. Click on "Register Product"
3. Fill out the application form with your project details
4. Wait for approval from Riot Games

## Configuration

### Environment Variable (Recommended)
Set the `RIOT_API_KEY` environment variable:

**Windows (PowerShell):**
```powershell
$env:RIOT_API_KEY="RGAPI-your-actual-api-key-here"
```

**Windows (Command Prompt):**
```cmd
set RIOT_API_KEY=RGAPI-your-actual-api-key-here
```

**Linux/Mac:**
```bash
export RIOT_API_KEY="RGAPI-your-actual-api-key-here"
```

### Application Configuration
Alternatively, update `application.yml`:
```yaml
riot:
  api:
    key: RGAPI-your-actual-api-key-here
```

## How It Works

### Registration Flow
1. User enters their Valorant ID (format: `GameName#TAG`)
2. Backend parses the Riot ID and calls Riot API
3. Riot API verifies if the account exists
4. If verification is enabled and account doesn't exist, registration fails
5. If verification succeeds, user account is created

### Current Behavior
- **Verification is OPTIONAL** by default
- If Riot API fails or account not found, registration still proceeds
- Logs warnings but doesn't block registration

### Enable Strict Verification
To enforce Riot account verification, uncomment these lines in `AuthService.java`:

```java
if (riotAccount == null) {
    log.warn("Riot account not found: {}", request.getValorantId());
    throw new RuntimeException("Error: Riot account not found. Please check your Valorant ID.");
}
```

## API Endpoints Used

### Account Verification
```
GET https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}
```

**Example:**
- Riot ID: `PlayerName#1234`
- API Call: `GET /riot/account/v1/accounts/by-riot-id/PlayerName/1234`

**Response:**
```json
{
  "puuid": "player-unique-id",
  "gameName": "PlayerName",
  "tagLine": "1234"
}
```

### Get Account by PUUID
```
GET https://americas.api.riotgames.com/riot/account/v1/accounts/by-puuid/{puuid}
```

## Rate Limits

### Development API Key
- 20 requests per second
- 100 requests per 2 minutes

### Production API Key
- Varies based on your approved application
- Typically much higher limits

## Regions

The application uses the `americas` region by default. Available regions:
- `americas` - North America, Latin America, Brazil
- `asia` - Asia
- `europe` - Europe, Middle East, Africa
- `sea` - Southeast Asia

To change region, update `application.yml`:
```yaml
riot:
  api:
    account-url: https://asia.api.riotgames.com  # Change region here
```

## Testing

### Test with Real Riot Account
1. Get a valid Riot ID from a Valorant player
2. Use format: `GameName#TAG` (e.g., `Shroud#NA1`)
3. Register with that Riot ID
4. Check logs for verification status

### Test without API Key
- Registration will work but skip verification
- Check logs for error messages

## Troubleshooting

### Error: "Failed to verify Riot account"
- Check if API key is set correctly
- Verify API key hasn't expired (dev keys expire after 24 hours)
- Check if you've exceeded rate limits

### Error: "Riot account not found"
- Verify the Riot ID format is correct (`GameName#TAG`)
- Make sure the account exists in Valorant
- Check if you're using the correct region

### API Key Not Working
- Regenerate your development API key
- Make sure there are no extra spaces in the key
- Verify the key starts with `RGAPI-`

## Future Enhancements

Potential features to add:
1. **Match History Integration** - Fetch player's match history
2. **Rank Verification** - Auto-populate rank from Riot API
3. **Stats Display** - Show player statistics
4. **Account Linking** - Allow users to link multiple Riot accounts
5. **OAuth Integration** - Use Riot Sign-On (RSO) for direct login

## Resources

- [Riot Developer Portal](https://developer.riotgames.com/)
- [Riot API Documentation](https://developer.riotgames.com/apis)
- [Account-V1 API Reference](https://developer.riotgames.com/apis#account-v1)
- [Riot API Policies](https://developer.riotgames.com/policies)

## Important Notes

⚠️ **API Key Security**
- Never commit API keys to version control
- Use environment variables in production
- Rotate keys regularly
- Don't share your API key publicly

⚠️ **Terms of Service**
- Follow Riot's API Terms of Service
- Don't abuse rate limits
- Properly attribute Riot Games in your application
- Include required legal disclaimers

## Legal Disclaimer

This application isn't endorsed by Riot Games and doesn't reflect the views or opinions of Riot Games or anyone officially involved in producing or managing Riot Games properties. Riot Games, and all associated properties are trademarks or registered trademarks of Riot Games, Inc.
