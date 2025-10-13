package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.dto.AuthResponse;
import com.valorantarena.dto.LoginRequest;
import com.valorantarena.dto.RegisterRequest;
import com.valorantarena.dto.RiotLoginRequest;
import com.valorantarena.dto.DiscordTokenDto;
import com.valorantarena.dto.DiscordUserDto;
import com.valorantarena.service.AuthService;
import com.valorantarena.service.DiscordOAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
    
    private final AuthService authService;
    private final DiscordOAuthService discordOAuthService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid credentials"));
        }
    }
    
    @PostMapping("/riot-login")
    @Operation(summary = "Login or register with Riot account")
    public ResponseEntity<ApiResponse<AuthResponse>> riotLogin(@Valid @RequestBody RiotLoginRequest request) {
        try {
            AuthResponse response = authService.loginWithRiot(request.getValorantId(), request.getRank());
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/discord/url")
    @Operation(summary = "Get Discord OAuth authorization URL")
    public ResponseEntity<ApiResponse<String>> getDiscordAuthUrl() {
        try {
            String url = discordOAuthService.getAuthorizationUrl();
            return ResponseEntity.ok(ApiResponse.success("Discord OAuth URL", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate Discord OAuth URL"));
        }
    }
    
    @PostMapping("/discord/callback")
    @Operation(summary = "Handle Discord OAuth callback")
    public ResponseEntity<ApiResponse<AuthResponse>> discordCallback(@RequestBody java.util.Map<String, String> request) {
        try {
            String code = request.get("code");
            if (code == null || code.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Authorization code is required"));
            }
            
            log.info("Processing Discord callback with code: {}", code.substring(0, Math.min(10, code.length())) + "...");
            
            // Exchange code for access token
            DiscordTokenDto token = discordOAuthService.exchangeCode(code);
            log.info("Discord token received successfully");
            
            // Get Discord user information
            DiscordUserDto discordUser = discordOAuthService.getDiscordUser(token.getAccessToken());
            log.info("Discord user info retrieved: {}", discordUser.getUsername());
            
            // Login or register user
            AuthResponse response = authService.loginWithDiscord(discordUser);
            log.info("Discord login successful for user: {}", response.getUsername());
            
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            log.error("Discord authentication failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Discord authentication failed: " + e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(ApiResponse.success("Auth service is running"));
    }
}
