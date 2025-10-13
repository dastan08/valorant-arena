package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.dto.LinkRiotAccountRequest;
import com.valorantarena.dto.PlayerStatsDto;
import com.valorantarena.dto.UserProfileResponse;
import com.valorantarena.entity.User;
import com.valorantarena.security.UserDetailsImpl;
import com.valorantarena.service.RiotApiService;
import com.valorantarena.service.UserService;
import com.valorantarena.service.ValorantApiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    private final RiotApiService riotApiService;
    
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userService.getUserById(userDetails.getId());
            UserProfileResponse response = UserProfileResponse.fromUser(user);
            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/link-riot-account")
    public ResponseEntity<ApiResponse> linkRiotAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @Valid @RequestBody LinkRiotAccountRequest request) {
        try {
            User updatedUser = userService.linkRiotAccount(userDetails.getId(), request);
            return ResponseEntity.ok(ApiResponse.success(
                    "Riot account linked successfully", 
                    updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/unlink-riot-account")
    public ResponseEntity<ApiResponse> unlinkRiotAccount(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User updatedUser = userService.unlinkRiotAccount(userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success(
                    "Riot account unlinked successfully", 
                    updatedUser));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/valorant-stats")
    public ResponseEntity<ApiResponse<PlayerStatsDto>> getValorantStats(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            User user = userService.getUserById(userDetails.getId());
            
            if (user.getValorantId() == null || user.getValorantId().startsWith("NOT_LINKED")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Please link your Valorant account first"));
            }
            
            // Parse Valorant ID
            String[] parts = user.getValorantId().split("#");
            if (parts.length != 2) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid Valorant ID format"));
            }
            
            String gameName = parts[0];
            String tagLine = parts[1];
            
            // Fetch stats using the new ValorantApiService through RiotApiService
            PlayerStatsDto stats = riotApiService.getPlayerStats(gameName, tagLine);
            
            if (stats == null) {
                return ResponseEntity.ok(ApiResponse.success(
                        "Unable to load Valorant stats. Please try again later.", 
                        null));
            }
            
            return ResponseEntity.ok(ApiResponse.success("Stats retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/valorant-matches")
    public ResponseEntity<ApiResponse<?>> getValorantMatches(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "competitive") String mode,
            @RequestParam(defaultValue = "10") int size) {
        try {
            User user = userService.getUserById(userDetails.getId());
            
            if (user.getValorantId() == null || user.getValorantId().startsWith("NOT_LINKED")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Please link your Valorant account first"));
            }
            
            // Parse Valorant ID
            String[] parts = user.getValorantId().split("#");
            if (parts.length != 2) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid Valorant ID format"));
            }
            
            String gameName = parts[0];
            String tagLine = parts[1];
            
            // Get player stats first to determine region
            PlayerStatsDto stats = riotApiService.getPlayerStats(gameName, tagLine);
            if (stats == null || stats.getRegion() == null) {
                return ResponseEntity.ok(ApiResponse.success(
                        "Unable to determine player region", 
                        null));
            }
            
            // Fetch matches using ValorantApiService
            ValorantApiService valorantApi = new ValorantApiService("");
            var matches = valorantApi.getMatches(stats.getRegion(), gameName, tagLine, mode, size).block();
            
            return ResponseEntity.ok(ApiResponse.success("Matches retrieved successfully", matches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
