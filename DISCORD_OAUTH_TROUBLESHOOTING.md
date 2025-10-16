# Discord OAuth 400 Bad Request - Troubleshooting Guide

## Error
```
Failed to exchange Discord authorization code: 400 Bad Request from POST https://discord.com/api/v10/oauth2/token
```

## Common Causes & Solutions

### 1. ✅ Redirect URI Mismatch (MOST COMMON)
The redirect URI in your code **MUST EXACTLY match** what's configured in Discord Developer Portal.

**Check:**
1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Select your application
3. Go to OAuth2 → General
4. Check the "Redirects" section

**Your current config:**
```yaml
discord:
  redirect-uri: ${DISCORD_REDIRECT_URI:http://valorant-arena-frontend.s3-website.ap-south-1.amazonaws.com/auth/discord/callback}
```

**Common issues:**
- ❌ `http://localhost:4200/auth/discord/callback` (local dev)
- ❌ `https://valorant-arena-frontend.s3-website.ap-south-1.amazonaws.com/auth/discord/callback` (https vs http)
- ❌ Trailing slash difference
- ❌ Different port number

**Fix:**
Make sure your environment variable `DISCORD_REDIRECT_URI` matches EXACTLY what's in Discord portal.

---

### 2. ✅ Authorization Code Already Used
Discord authorization codes can only be used **once**. If you refresh the callback page, it will fail.

**Symptoms:**
- Works first time
- Fails on page refresh
- Error: "invalid_grant"

**Fix:**
- Don't refresh the callback page
- Start the OAuth flow from the beginning

---

### 3. ✅ Missing or Invalid Client Credentials

**Check your environment variables:**
```bash
# Windows
echo %DISCORD_CLIENT_ID%
echo %DISCORD_CLIENT_SECRET%
echo %DISCORD_REDIRECT_URI%

# Linux/Mac
echo $DISCORD_CLIENT_ID
echo $DISCORD_CLIENT_SECRET
echo $DISCORD_REDIRECT_URI
```

**Verify in Discord Portal:**
1. Go to OAuth2 → General
2. Copy Client ID
3. Reset Client Secret if needed
4. Update your environment variables or `application-secrets.yml`

---

### 4. ✅ URL Encoding Issues

The redirect URI might need proper URL encoding, especially if it contains special characters.

**Current implementation:** ✅ Should be fine (Spring handles this)

---

### 5. ✅ Scope Mismatch

**Your authorization URL:**
```java
"https://discord.com/api/oauth2/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=identify%%20email"
```

**Make sure:**
- Scopes match what's requested during authorization
- Scopes are URL encoded (`%20` for space)

---

## 🔍 Debugging Steps

### Step 1: Check Logs
With the updated code, you should now see:
```
Client ID: 123456789
Redirect URI: http://...
Discord API error response: {"error": "invalid_request", "error_description": "..."}
```

### Step 2: Verify Discord Portal Configuration

1. **Application ID** = `DISCORD_CLIENT_ID`
2. **Client Secret** = `DISCORD_CLIENT_SECRET`
3. **Redirects** must include your exact redirect URI

### Step 3: Test with Postman/cURL

```bash
curl -X POST https://discord.com/api/v10/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "grant_type=authorization_code" \
  -d "code=YOUR_AUTH_CODE" \
  -d "redirect_uri=YOUR_EXACT_REDIRECT_URI"
```

---

## 🛠️ Quick Fixes

### Fix 1: Update Redirect URI in Discord Portal
1. Go to Discord Developer Portal
2. OAuth2 → General → Redirects
3. Add: `http://valorant-arena-frontend.s3-website.ap-south-1.amazonaws.com/auth/discord/callback`
4. Save Changes

### Fix 2: Set Environment Variables
Create `application-secrets.yml`:
```yaml
discord:
  client-id: "YOUR_ACTUAL_CLIENT_ID"
  client-secret: "YOUR_ACTUAL_CLIENT_SECRET"
  redirect-uri: "http://valorant-arena-frontend.s3-website.ap-south-1.amazonaws.com/auth/discord/callback"
```

### Fix 3: For Local Development
If testing locally, add to Discord portal:
```
http://localhost:4200/auth/discord/callback
http://localhost:8080/auth/discord/callback
```

And set:
```yaml
discord:
  redirect-uri: "http://localhost:4200/auth/discord/callback"
```

---

## 📝 Expected Error Response Format

Discord will return JSON like:
```json
{
  "error": "invalid_request",
  "error_description": "Invalid \"redirect_uri\" in request."
}
```

or

```json
{
  "error": "invalid_grant",
  "error_description": "Invalid \"code\" in request."
}
```

---

## ✅ Verification Checklist

- [ ] Discord Client ID is correct
- [ ] Discord Client Secret is correct
- [ ] Redirect URI in code matches Discord portal EXACTLY
- [ ] Redirect URI is added to Discord portal's allowed redirects
- [ ] Not reusing an authorization code
- [ ] Scopes match between authorization and token exchange
- [ ] No typos in environment variables
- [ ] `application-secrets.yml` exists and is loaded

---

## 🚀 Next Steps

1. **Run the app again** - The new logging will show the exact error from Discord
2. **Check the logs** for "Discord API error response"
3. **Compare redirect URIs** - Code vs Discord Portal
4. **Update this document** with the actual error message you see

---

## 📞 Still Having Issues?

If you see the detailed error message in logs, update this section with:
- The exact error from Discord API
- Your redirect URI configuration
- Whether it's local dev or production
