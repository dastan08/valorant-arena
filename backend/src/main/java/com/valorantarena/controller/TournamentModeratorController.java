package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.dto.TournamentModeratorRequest;
import com.valorantarena.dto.TournamentModeratorResponse;
import com.valorantarena.security.UserDetailsImpl;
import com.valorantarena.service.TournamentModeratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}/moderators")
@RequiredArgsConstructor
@Tag(name = "Tournament Moderators", description = "Tournament moderator management endpoints")
public class TournamentModeratorController {
    
    private final TournamentModeratorService moderatorService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Operation(summary = "Assign moderator to tournament")
    public ResponseEntity<ApiResponse<TournamentModeratorResponse>> assignModerator(
            @PathVariable Long tournamentId,
            @Valid @RequestBody TournamentModeratorRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            TournamentModeratorResponse response = moderatorService.assignModerator(
                    tournamentId, request, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Moderator assigned successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    @Operation(summary = "Remove moderator from tournament")
    public ResponseEntity<ApiResponse<Void>> removeModerator(
            @PathVariable Long tournamentId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            moderatorService.removeModerator(tournamentId, userId, userDetails.getId());
            return ResponseEntity.ok(ApiResponse.success("Moderator removed successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'TOURNAMENT_MODERATOR', 'ADMIN')")
    @Operation(summary = "Get tournament moderators")
    public ResponseEntity<ApiResponse<List<TournamentModeratorResponse>>> getTournamentModerators(
            @PathVariable Long tournamentId) {
        try {
            List<TournamentModeratorResponse> moderators = moderatorService.getTournamentModerators(tournamentId);
            return ResponseEntity.ok(ApiResponse.success("Moderators retrieved successfully", moderators));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @GetMapping("/check/{userId}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'TOURNAMENT_MODERATOR', 'ADMIN')")
    @Operation(summary = "Check if user is moderator for tournament")
    public ResponseEntity<ApiResponse<Boolean>> checkModerator(
            @PathVariable Long tournamentId,
            @PathVariable Long userId) {
        try {
            boolean isModerator = moderatorService.isModeratorForTournament(tournamentId, userId);
            return ResponseEntity.ok(ApiResponse.success("Check completed", isModerator));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
