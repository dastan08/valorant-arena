# Riot OAuth Implementation - Complete Code

## ⚠️ IMPORTANT: Prerequisites

Before implementing, you MUST:

1. **Apply for RSO access** at [developer.riotgames.com](https://developer.riotgames.com)
2. **Wait for approval** (can take 12 hours to 3-4 months)
3. **Receive credentials** (Client ID and Client Secret)
4. **Add credentials to environment variables**

Without RSO credentials, the OAuth flow will NOT work!

## 📋 Implementation Summary

This implementation adds "Sign in with Riot" functionality to your VALORANT Arena application, allowing users to:
- Login with their Riot account
- Automatically link their VALORANT profile
- Skip manual Riot ID entry
- Get verified instantly

## 🔧 Backend Implementation

### **1. Update application.yml** ✅ DONE

Already added Riot OAuth configuration.

### **2. Create RiotOAuthService.java**

Create: `backend/src/main/java/com/valorantarena/service/RiotOAuthService.java`

```java
package com.valorantarena.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.valorantarena.entity.User;
import com.valorantarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiotOAuthService {
    
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${riot.oauth.client-id}")
    private String clientId;
    
    @Value("${riot.oauth.client-secret}")
    private String clientSecret;
    
    @Value("${riot.oauth.redirect-uri}")
    private String redirectUri;
    
    @Value("${riot.oauth.token-uri}")
    private String tokenUri;
    
    @Value("${riot.oauth.user-info-uri}")
    private String userInfoUri;
    
    /**
     * Exchange authorization code for access token
     */
    public JsonNode exchangeCodeForTokens(String code) {
        try {
            log.info("Exchanging authorization code for tokens");
            
            // Prepare request body
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", redirectUri);
            
            // Prepare headers with Basic Auth
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            // Make request
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode tokenData = objectMapper.readTree(response.getBody());
                log.info("Successfully exchanged code for tokens");
                return tokenData;
            } else {
                log.error("Failed to exchange code: {}", response.getStatusCode());
                throw new RuntimeException("Failed to exchange authorization code");
            }
            
        } catch (Exception e) {
            log.error("Error exchanging code for tokens", e);
            throw new RuntimeException("Failed to exchange authorization code: " + e.getMessage());
        }
    }
    
    /**
     * Get user info from Riot using access token
     */
    public JsonNode getUserInfo(String accessToken) {
        try {
            log.info("Fetching user info from Riot");
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                userInfoUri,
                HttpMethod.GET,
                request,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode userInfo = objectMapper.readTree(response.getBody());
                log.info("Successfully fetched user info");
                return userInfo;
            } else {
                log.error("Failed to fetch user info: {}", response.getStatusCode());
                throw new RuntimeException("Failed to fetch user info");
            }
            
        } catch (Exception e) {
            log.error("Error fetching user info", e);
            throw new RuntimeException("Failed to fetch user info: " + e.getMessage());
        }
    }
    
    /**
     * Create or update user from Riot OAuth data
     */
    public User createOrUpdateUser(JsonNode tokenData, JsonNode userInfo) {
        try {
            // Extract data
            String puuid = userInfo.get("sub").asText();
            String gameName = userInfo.get("acct").get("game_name").asText();
            String tagLine = userInfo.get("acct").get("tag_line").asText();
            String accessToken = tokenData.get("access_token").asText();
            String refreshToken = tokenData.get("refresh_token").asText();
            int expiresIn = tokenData.get("expires_in").asInt();
            
            log.info("Creating/updating user for Riot account: {}#{}", gameName, tagLine);
            
            // Check if user exists by PUUID
            Optional<User> existingUser = userRepository.findByRiotPuuid(puuid);
            
            User user;
            if (existingUser.isPresent()) {
                // Update existing user
                user = existingUser.get();
                log.info("Updating existing user: {}", user.getUsername());
            } else {
                // Create new user
                user = new User();
                user.setUsername(gameName + "_" + tagLine.replace("-", ""));
                user.setEmail(gameName + "@riot.local"); // Placeholder email
                user.setPassword("RIOT_OAUTH_USER"); // No password for OAuth users
                user.setValorantId(gameName + "#" + tagLine);
                user.setIsVerified(true); // Riot OAuth users are auto-verified
                log.info("Creating new user from Riot OAuth");
            }
            
            // Update Riot OAuth data
            user.setRiotPuuid(puuid);
            user.setRiotGameName(gameName);
            user.setRiotTagLine(tagLine);
            user.setRiotAccessToken(accessToken);
            user.setRiotRefreshToken(refreshToken);
            user.setRiotTokenExpiry(LocalDateTime.now().plusSeconds(expiresIn));
            
            return userRepository.save(user);
            
        } catch (Exception e) {
            log.error("Error creating/updating user from Riot OAuth", e);
            throw new RuntimeException("Failed to create/update user: " + e.getMessage());
        }
    }
    
    /**
     * Refresh access token using refresh token
     */
    public JsonNode refreshAccessToken(String refreshToken) {
        try {
            log.info("Refreshing Riot access token");
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("refresh_token", refreshToken);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            String auth = clientId + ":" + clientSecret;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            headers.set("Authorization", "Basic " + encodedAuth);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode tokenData = objectMapper.readTree(response.getBody());
                log.info("Successfully refreshed access token");
                return tokenData;
            } else {
                log.error("Failed to refresh token: {}", response.getStatusCode());
                throw new RuntimeException("Failed to refresh access token");
            }
            
        } catch (Exception e) {
            log.error("Error refreshing access token", e);
            throw new RuntimeException("Failed to refresh access token: " + e.getMessage());
        }
    }
}
```

### **3. Update UserRepository.java**

Add method to find user by Riot PUUID:

```java
Optional<User> findByRiotPuuid(String riotPuuid);
```

### **4. Update AuthController.java**

Add Riot OAuth endpoints:

```java
@Autowired
private RiotOAuthService riotOAuthService;

@Autowired
private JwtUtils jwtUtils;

/**
 * Redirect to Riot OAuth login
 */
@GetMapping("/riot/login")
public ResponseEntity<?> riotLogin() {
    String authorizationUrl = String.format(
        "%s?redirect_uri=%s&client_id=%s&response_type=code&scope=openid",
        "https://auth.riotgames.com/authorize",
        URLEncoder.encode(riotRedirectUri, StandardCharsets.UTF_8),
        riotClientId
    );
    
    return ResponseEntity.ok(ApiResponse.success("Redirect to Riot OAuth", 
        Map.of("url", authorizationUrl)));
}

/**
 * Handle Riot OAuth callback
 */
@PostMapping("/riot/callback")
public ResponseEntity<?> riotCallback(@RequestBody Map<String, String> request) {
    try {
        String code = request.get("code");
        
        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Authorization code is required"));
        }
        
        log.info("Received Riot OAuth callback with code");
        
        // Exchange code for tokens
        JsonNode tokenData = riotOAuthService.exchangeCodeForTokens(code);
        String accessToken = tokenData.get("access_token").asText();
        
        // Get user info
        JsonNode userInfo = riotOAuthService.getUserInfo(accessToken);
        
        // Create or update user
        User user = riotOAuthService.createOrUpdateUser(tokenData, userInfo);
        
        // Generate JWT token
        String jwtToken = jwtUtils.generateToken(user.getUsername());
        
        // Return response
        AuthResponse authResponse = new AuthResponse(
            jwtToken,
            "Bearer",
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole().name()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Riot login successful", authResponse));
        
    } catch (Exception e) {
        log.error("Riot OAuth callback error", e);
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("Riot login failed: " + e.getMessage()));
    }
}
```

## 🎨 Frontend Implementation

### **1. Update Login Page**

Add "Sign in with Riot" button to `login.component.html`:

```html
<!-- After Discord button -->
<button (click)="loginWithRiot()" 
        class="w-full flex items-center justify-center px-4 py-3 border border-transparent text-sm font-medium rounded-md text-white bg-gradient-to-r from-red-600 to-orange-600 hover:from-red-700 hover:to-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 transition-all transform hover:scale-105">
  <svg class="w-5 h-5 mr-2" fill="currentColor" viewBox="0 0 24 24">
    <path d="M12 2L2 7v10c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V7l-10-5z"/>
  </svg>
  Sign in with Riot
</button>
```

### **2. Update Login Component TypeScript**

Add method to `login.component.ts`:

```typescript
loginWithRiot(): void {
  // Redirect to backend which will redirect to Riot OAuth
  window.location.href = 'http://localhost:8080/api/auth/riot/login';
}
```

### **3. Create Riot Callback Component**

Create: `frontend/src/app/pages/auth/riot-callback/riot-callback.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-riot-callback',
  template: `
    <div class="min-h-screen bg-valorant-dark flex items-center justify-center">
      <div class="text-center">
        <div class="animate-spin rounded-full h-16 w-16 border-b-2 border-valorant-red mx-auto"></div>
        <p class="mt-4 text-white text-lg">Completing Riot login...</p>
      </div>
    </div>
  `
})
export class RiotCallbackComponent implements OnInit {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private http: HttpClient,
    private toastr: ToastrService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const code = this.route.snapshot.queryParams['code'];
    const error = this.route.snapshot.queryParams['error'];

    if (error) {
      this.toastr.error('Riot authorization failed', 'Error');
      this.router.navigate(['/login']);
      return;
    }

    if (!code) {
      this.toastr.error('No authorization code received', 'Error');
      this.router.navigate(['/login']);
      return;
    }

    console.log('Sending code to backend:', code);
    this.http.post<any>(`${this.apiUrl}/riot/callback`, { code }).subscribe({
      next: (response) => {
        console.log('Backend response:', response);
        if (response.success && response.data) {
          localStorage.setItem('token', response.data.token);
          
          // Refresh user profile
          this.authService.refreshUserProfile();
          
          this.toastr.success('Login successful!', 'Welcome ' + response.data.username);
          
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 500);
        } else {
          console.error('Login failed - no data in response');
          this.toastr.error('Login failed', 'Error');
          this.router.navigate(['/login']);
        }
      },
      error: (error) => {
        console.error('Riot login error:', error);
        this.toastr.error(error.error?.message || 'Riot login failed', 'Error');
        this.router.navigate(['/login']);
      }
    });
  }
}
```

### **4. Update App Routing**

Add route to `app-routing.module.ts`:

```typescript
{
  path: 'auth/riot/callback',
  component: RiotCallbackComponent
}
```

### **5. Register Component**

Add to `app.module.ts`:

```typescript
import { RiotCallbackComponent } from './pages/auth/riot-callback/riot-callback.component';

declarations: [
  // ... other components
  RiotCallbackComponent
]
```

## 🔐 Environment Variables

Create `.env` file in backend root (DON'T commit!):

```
RIOT_CLIENT_ID=your-riot-client-id-here
RIOT_CLIENT_SECRET=your-riot-client-secret-here
RIOT_REDIRECT_URI=http://localhost:4200/auth/riot/callback
```

## 🧪 Testing

1. **Start Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

2. **Start Frontend:**
```bash
cd frontend
npm start
```

3. **Test Flow:**
   - Go to http://localhost:4200/login
   - Click "Sign in with Riot"
   - Login with Riot credentials
   - Should redirect back and auto-login

## 📝 Next Steps

1. **Apply for RSO** - Go to developer.riotgames.com
2. **Wait for approval** - Check email regularly
3. **Get credentials** - Save Client ID and Secret
4. **Add to .env** - Update environment variables
5. **Test** - Try the login flow
6. **Deploy** - Update redirect URIs for production

## ⚠️ Important Notes

- **RSO approval required** - Won't work without credentials
- **Production redirect URI** - Must use HTTPS in production
- **Token expiry** - Access tokens expire in 10 minutes
- **Refresh tokens** - Use to get new access tokens
- **User linking** - Automatically links Riot account to profile

---

**Implementation complete!** Follow the setup guide in `RIOT_OAUTH_SETUP.md` to get your credentials.
