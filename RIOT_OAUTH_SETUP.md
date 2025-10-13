# Riot Games OAuth (RSO) Implementation Guide

## 📋 Overview

This guide explains how to implement Riot Sign On (RSO) OAuth 2.0 authentication for your VALORANT Arena application.

## 🔑 Prerequisites

### **1. Apply for RSO Access**

You need to apply for RSO access through the Riot Developer Portal:
1. Go to [developer.riotgames.com](https://developer.riotgames.com)
2. Apply for a **Production API Key**
3. Request **RSO (Riot Sign On)** access
4. Wait for approval (12 hours to 3-4 months depending on review queue)

### **2. Information Riot Needs**

After approval, you'll need to provide:

1. **Contact Emails** - For communication
2. **Company/Service Name** - "VALORANT ARENA"
3. **Logo** - 275x275 to 60x60 (SVG or PNG, works on light/dark backgrounds)
4. **Privacy Policy URL** - `https://yourdomain.com/privacy`
5. **Terms of Service URL** - `https://yourdomain.com/terms`
6. **Redirect URLs** - `http://localhost:4200/auth/riot/callback` (dev), `https://yourdomain.com/auth/riot/callback` (prod)
7. **Post Logout Redirect URLs** - `http://localhost:4200` (dev), `https://yourdomain.com` (prod)
8. **Preferred Client ID** - `valorantarena` (no spaces/punctuation)

### **3. Receive Credentials**

After setup, Riot will email you:
- **Client ID** - Your application identifier
- **Client Secret** OR **Client Assertion** (JWT) - For authentication
- **Link expires in 6 days** or after 3 opens - Save credentials immediately!

## 🔐 OAuth Endpoints

```
Authorization: https://auth.riotgames.com/authorize
Token:         https://auth.riotgames.com/token
JWKS:          https://auth.riotgames.com/jwks.json
UserInfo:      https://auth.riotgames.com/userinfo
```

## 🔄 OAuth Flow

### **Step 1: Redirect User to Riot Login**

```
https://auth.riotgames.com/authorize?
  redirect_uri=http://localhost:4200/auth/riot/callback
  &client_id=valorantarena
  &response_type=code
  &scope=openid
```

**Parameters:**
- `redirect_uri` - Where to send user after login
- `client_id` - Your RSO client ID
- `response_type` - Always `code` for authorization code flow
- `scope` - `openid` (required) + optional scopes

**Optional Scopes:**
- `openid` - Required base scope
- `cpid` - Get player's CPID (Cross-Platform ID)
- `email` - Get player's email
- `profile` - Get player's profile info

### **Step 2: User Logs In**

User enters their Riot credentials on Riot's secure login page.

### **Step 3: Riot Redirects Back**

After successful login:
```
http://localhost:4200/auth/riot/callback?code=AUTHORIZATION_CODE_HERE
```

### **Step 4: Exchange Code for Tokens**

Your backend exchanges the authorization code for tokens:

**Request:**
```http
POST https://auth.riotgames.com/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic BASE64(client_id:client_secret)

grant_type=authorization_code
&code=AUTHORIZATION_CODE_HERE
&redirect_uri=http://localhost:4200/auth/riot/callback
```

**Response:**
```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "Bearer",
  "expires_in": 600,
  "refresh_token": "dXJuOnJpb3Q...",
  "id_token": "eyJhbGciOiJ...",
  "scope": "openid",
  "sub_sid": "vldfsXGdDPoafSKfjS..."
}
```

### **Step 5: Get User Info**

Use the access token to get player information:

**Request:**
```http
GET https://auth.riotgames.com/userinfo
Authorization: Bearer ACCESS_TOKEN_HERE
```

**Response:**
```json
{
  "sub": "encrypted-puuid",
  "acct": {
    "game_name": "PlayerName",
    "tag_line": "NA1"
  },
  "email": "player@example.com",
  "email_verified": true,
  "locale": "en_US"
}
```

## 🏗️ Implementation Architecture

### **Backend (Spring Boot)**

```
AuthController
├── /api/auth/riot/login → Redirect to Riot OAuth
├── /api/auth/riot/callback → Handle OAuth callback
└── RiotOAuthService
    ├── exchangeCodeForTokens()
    ├── getUserInfo()
    └── createOrUpdateUser()
```

### **Frontend (Angular)**

```
Login Page
├── "Sign in with Riot" button
└── Redirects to backend /api/auth/riot/login

Callback Page
├── /auth/riot/callback
├── Receives JWT token from backend
└── Stores token and redirects to dashboard
```

## 🔒 Security Best Practices

1. **Store Client Secret Securely**
   - Use environment variables
   - Never commit to Git
   - Use Spring Boot's `@Value` annotation

2. **Validate State Parameter**
   - Generate random state before redirect
   - Verify state matches on callback
   - Prevents CSRF attacks

3. **Use HTTPS in Production**
   - All redirect URIs must use HTTPS
   - Except localhost for development

4. **Token Storage**
   - Store access tokens securely
   - Use refresh tokens to get new access tokens
   - Access tokens expire in 10 minutes (600 seconds)

5. **Verify ID Token**
   - Validate JWT signature using JWKS
   - Check expiration time
   - Verify audience matches your client_id

## 📊 Database Schema

Update `User` entity to store Riot account data:

```java
@Column(unique = true)
private String riotPuuid; // Riot's unique player ID

@Column
private String riotGameName; // e.g., "PlayerName"

@Column
private String riotTagLine; // e.g., "NA1"

@Column
private String riotAccessToken; // For API calls

@Column
private String riotRefreshToken; // To refresh access token

@Column
private LocalDateTime riotTokenExpiry; // When access token expires
```

## 🎮 User Experience Flow

1. **User clicks "Sign in with Riot"**
2. **Redirected to Riot login page**
3. **Enters Riot credentials**
4. **Approves permissions**
5. **Redirected back to your app**
6. **Automatically logged in**
7. **Riot account linked to profile**

## 🔄 Token Refresh

Access tokens expire in 10 minutes. Use refresh tokens to get new ones:

**Request:**
```http
POST https://auth.riotgames.com/token
Content-Type: application/x-www-form-urlencoded
Authorization: Basic BASE64(client_id:client_secret)

grant_type=refresh_token
&refresh_token=REFRESH_TOKEN_HERE
```

**Response:**
```json
{
  "access_token": "new_access_token",
  "token_type": "Bearer",
  "expires_in": 600,
  "refresh_token": "new_refresh_token",
  "scope": "openid"
}
```

## 🧪 Testing

### **Development URLs:**
- Authorization: `http://localhost:4200/auth/riot/callback`
- Logout: `http://localhost:4200`

### **Test Flow:**
1. Start backend on `localhost:8080`
2. Start frontend on `localhost:4200`
3. Click "Sign in with Riot"
4. Login with test Riot account
5. Verify redirect and token storage
6. Check user data in database

## 📝 Configuration Files

### **application.yml**
```yaml
riot:
  oauth:
    client-id: ${RIOT_CLIENT_ID}
    client-secret: ${RIOT_CLIENT_SECRET}
    redirect-uri: ${RIOT_REDIRECT_URI:http://localhost:4200/auth/riot/callback}
    authorization-uri: https://auth.riotgames.com/authorize
    token-uri: https://auth.riotgames.com/token
    user-info-uri: https://auth.riotgames.com/userinfo
```

### **.env** (Don't commit!)
```
RIOT_CLIENT_ID=valorantarena
RIOT_CLIENT_SECRET=your_secret_here
RIOT_REDIRECT_URI=http://localhost:4200/auth/riot/callback
```

## 🚀 Next Steps

1. **Apply for RSO access** at developer.riotgames.com
2. **Wait for approval** (check email regularly)
3. **Provide required information** to Riot
4. **Receive credentials** and save them securely
5. **Implement backend** OAuth endpoints
6. **Implement frontend** login button and callback
7. **Test** with development credentials
8. **Deploy** with production credentials

## 📚 Resources

- [Riot Developer Portal](https://developer.riotgames.com)
- [RSO Documentation](https://support-developer.riotgames.com/hc/en-us/articles/22897607341075)
- [OAuth 2.0 Specification](https://oauth.net/2/)
- [GitHub RSO Example](https://gist.github.com/Henrik-3/d6b631fb7c61821bc16b17cd347a3811)

## ⚠️ Important Notes

1. **Application Review Time:** 12 hours to 3-4 months
2. **Credentials Expire:** Save within 6 days or 3 opens
3. **Access Token Lifetime:** 10 minutes (600 seconds)
4. **Refresh Token:** Use to get new access tokens
5. **Production Key Required:** RSO needs production API key
6. **HTTPS Required:** All production redirect URIs must use HTTPS

---

**Ready to implement?** Follow the code implementation in the next sections!
