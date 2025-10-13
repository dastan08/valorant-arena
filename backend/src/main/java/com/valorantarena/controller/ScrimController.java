package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.entity.Scrim;
import com.valorantarena.security.UserDetailsImpl;
import com.valorantarena.service.ScrimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/scrims")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class ScrimController {
    
    private final ScrimService scrimService;
    
    /**
     * Challenge another team to a scrim
     */
    @PostMapping("/challenge")
    public ResponseEntity<ApiResponse<Scrim>> challengeTeam(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> request) {
        try {
            Long challengerTeamId = Long.valueOf(request.get("challengerTeamId").toString());
            Long opponentTeamId = Long.valueOf(request.get("opponentTeamId").toString());
            LocalDateTime scheduledTime = LocalDateTime.parse(request.get("scheduledTime").toString());
            String message = request.get("message") != null ? request.get("message").toString() : null;
            String map = request.get("map") != null ? request.get("map").toString() : "TBD";
            Integer bestOf = request.get("bestOf") != null ? Integer.valueOf(request.get("bestOf").toString()) : 1;
            
            Scrim scrim = scrimService.challengeTeam(
                challengerTeamId, opponentTeamId, userDetails.getId(),
                scheduledTime, message, map, bestOf
            );
            
            return ResponseEntity.ok(ApiResponse.success("Scrim challenge sent successfully", scrim));
        } catch (Exception e) {
            log.error("Error challenging team", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Accept a scrim challenge
     */
    @PostMapping("/{scrimId}/accept")
    public ResponseEntity<ApiResponse<Scrim>> acceptScrim(
            @PathVariable Long scrimId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            Scrim scrim = scrimService.acceptScrim(scrimId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Scrim accepted successfully", scrim));
        } catch (Exception e) {
            log.error("Error accepting scrim", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Decline a scrim challenge
     */
    @PostMapping("/{scrimId}/decline")
    public ResponseEntity<ApiResponse<Scrim>> declineScrim(
            @PathVariable Long scrimId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String reason = request != null ? request.get("reason") : null;
            Scrim scrim = scrimService.declineScrim(scrimId, userDetails.getId(), reason);
            return ResponseEntity.ok(ApiResponse.success("Scrim declined", scrim));
        } catch (Exception e) {
            log.error("Error declining scrim", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Cancel a scrim
     */
    @PostMapping("/{scrimId}/cancel")
    public ResponseEntity<ApiResponse<Scrim>> cancelScrim(
            @PathVariable Long scrimId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            String reason = request != null ? request.get("reason") : null;
            Scrim scrim = scrimService.cancelScrim(scrimId, userDetails.getId(), reason);
            return ResponseEntity.ok(ApiResponse.success("Scrim cancelled", scrim));
        } catch (Exception e) {
            log.error("Error cancelling scrim", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get all scrims for a team
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<List<Scrim>>> getTeamScrims(@PathVariable Long teamId) {
        try {
            List<Scrim> scrims = scrimService.getTeamScrims(teamId);
            return ResponseEntity.ok(ApiResponse.success("Scrims retrieved successfully", scrims));
        } catch (Exception e) {
            log.error("Error getting team scrims", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get pending scrim requests for a team
     */
    @GetMapping("/team/{teamId}/pending")
    public ResponseEntity<ApiResponse<List<Scrim>>> getPendingScrims(@PathVariable Long teamId) {
        try {
            List<Scrim> scrims = scrimService.getPendingScrims(teamId);
            return ResponseEntity.ok(ApiResponse.success("Pending scrims retrieved successfully", scrims));
        } catch (Exception e) {
            log.error("Error getting pending scrims", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get upcoming scrims for a team
     */
    @GetMapping("/team/{teamId}/upcoming")
    public ResponseEntity<ApiResponse<List<Scrim>>> getUpcomingScrims(@PathVariable Long teamId) {
        try {
            List<Scrim> scrims = scrimService.getUpcomingScrims(teamId);
            return ResponseEntity.ok(ApiResponse.success("Upcoming scrims retrieved successfully", scrims));
        } catch (Exception e) {
            log.error("Error getting upcoming scrims", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * Get scrim by ID
     */
    @GetMapping("/{scrimId}")
    public ResponseEntity<ApiResponse<Scrim>> getScrimById(@PathVariable Long scrimId) {
        try {
            Scrim scrim = scrimService.getScrimById(scrimId);
            return ResponseEntity.ok(ApiResponse.success("Scrim retrieved successfully", scrim));
        } catch (Exception e) {
            log.error("Error getting scrim", e);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
