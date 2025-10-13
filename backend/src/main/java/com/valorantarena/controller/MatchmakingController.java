package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.dto.MatchmakingRequest;
import com.valorantarena.dto.MatchmakingResponse;
import com.valorantarena.security.UserDetailsImpl;
import com.valorantarena.service.MatchmakingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matchmaking")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class MatchmakingController {
    
    private final MatchmakingService matchmakingService;
    
    /**
     * Join matchmaking queue
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<MatchmakingResponse>> joinQueue(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody MatchmakingRequest request) {
        try {
            String region = request.getRegion() != null ? request.getRegion() : "NA";
            MatchmakingResponse response = matchmakingService.joinQueue(userDetails.getId(), region);
            return ResponseEntity.ok(ApiResponse.success("Joined matchmaking queue", response));
        } catch (Exception e) {
            log.error("Error joining queue", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to join queue: " + e.getMessage()));
        }
    }
    
    /**
     * Leave matchmaking queue
     */
    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<Void>> leaveQueue(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            matchmakingService.leaveQueue(userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Left matchmaking queue", null));
        } catch (Exception e) {
            log.error("Error leaving queue", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to leave queue: " + e.getMessage()));
        }
    }
    
    /**
     * Get current queue status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<MatchmakingResponse>> getQueueStatus(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            MatchmakingResponse response = matchmakingService.getQueueStatus(userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Queue status retrieved", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Not in queue"));
        }
    }
    
    /**
     * Get queue statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<MatchmakingService.MatchmakingStats>> getQueueStats(
            @RequestParam(defaultValue = "NA") String region) {
        try {
            MatchmakingService.MatchmakingStats stats = matchmakingService.getQueueStats(region);
            return ResponseEntity.ok(ApiResponse.success("Queue stats retrieved", stats));
        } catch (Exception e) {
            log.error("Error getting queue stats", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("Failed to get queue stats: " + e.getMessage()));
        }
    }
}
