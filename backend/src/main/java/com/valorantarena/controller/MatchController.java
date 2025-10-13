package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.dto.ReportScoreDTO;
import com.valorantarena.model.TournamentMatch;
import com.valorantarena.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/{matchId}")
    public ResponseEntity<ApiResponse<TournamentMatch>> getMatch(@PathVariable Long matchId) {
        try {
            TournamentMatch match = matchService.getMatchById(matchId);
            return ResponseEntity.ok(ApiResponse.success("Match retrieved successfully", match));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tournament/{tournamentId}")
    public ResponseEntity<ApiResponse<List<TournamentMatch>>> getTournamentMatches(
            @PathVariable Long tournamentId) {
        try {
            List<TournamentMatch> matches = matchService.getMatchesByTournament(tournamentId);
            return ResponseEntity.ok(ApiResponse.success("Matches retrieved successfully", matches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tournament/{tournamentId}/round/{roundNumber}")
    public ResponseEntity<ApiResponse<List<TournamentMatch>>> getRoundMatches(
            @PathVariable Long tournamentId,
            @PathVariable Integer roundNumber) {
        try {
            List<TournamentMatch> matches = matchService.getMatchesByRound(tournamentId, roundNumber);
            return ResponseEntity.ok(ApiResponse.success("Matches retrieved successfully", matches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/my-matches")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TournamentMatch>>> getMyMatches(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            List<TournamentMatch> matches = matchService.getUserMatches(userId);
            return ResponseEntity.ok(ApiResponse.success("Matches retrieved successfully", matches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{matchId}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TournamentMatch>> startMatch(
            @PathVariable Long matchId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            TournamentMatch match = matchService.startMatch(matchId, userId);
            return ResponseEntity.ok(ApiResponse.success("Match started successfully", match));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{matchId}/report-score")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TournamentMatch>> reportScore(
            @PathVariable Long matchId,
            @Valid @RequestBody ReportScoreDTO dto,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            TournamentMatch match = matchService.reportScore(matchId, dto, userId);
            return ResponseEntity.ok(ApiResponse.success("Score reported successfully", match));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{matchId}/confirm-score")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TournamentMatch>> confirmScore(
            @PathVariable Long matchId,
            Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            TournamentMatch match = matchService.confirmScore(matchId, userId);
            return ResponseEntity.ok(ApiResponse.success("Score confirmed successfully", match));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
