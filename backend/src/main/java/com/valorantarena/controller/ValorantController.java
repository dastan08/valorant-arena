package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.dto.LeaderboardPlayerDto;
import com.valorantarena.dto.MatchDto;
import com.valorantarena.dto.PlayerStatsDto;
import com.valorantarena.service.RiotApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/valorant")
@RequiredArgsConstructor
@Tag(name = "Valorant", description = "Valorant player stats and data APIs")
public class ValorantController {
    
    private final RiotApiService riotApiService;
    
    @GetMapping("/stats/{gameName}/{tagLine}")
    @Operation(summary = "Get player stats and MMR")
    public ResponseEntity<ApiResponse<PlayerStatsDto>> getPlayerStats(
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        try {
            PlayerStatsDto stats = riotApiService.getPlayerStats(gameName, tagLine);
            if (stats != null) {
                return ResponseEntity.ok(ApiResponse.success("Player stats retrieved", stats));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch player stats: " + e.getMessage()));
        }
    }
    
    @GetMapping("/matches/{gameName}/{tagLine}")
    @Operation(summary = "Get match history")
    public ResponseEntity<ApiResponse<List<MatchDto>>> getMatchHistory(
            @PathVariable String gameName,
            @PathVariable String tagLine,
            @RequestParam(defaultValue = "na") String region) {
        try {
            List<MatchDto> matches = riotApiService.getMatchHistory(gameName, tagLine, region);
            return ResponseEntity.ok(ApiResponse.success("Match history retrieved", matches));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch match history: " + e.getMessage()));
        }
    }
    
    @GetMapping("/leaderboard/{region}")
    @Operation(summary = "Get regional leaderboard")
    public ResponseEntity<ApiResponse<List<LeaderboardPlayerDto>>> getLeaderboard(
            @PathVariable String region) {
        try {
            List<LeaderboardPlayerDto> leaderboard = riotApiService.getLeaderboard(region);
            return ResponseEntity.ok(ApiResponse.success("Leaderboard retrieved", leaderboard));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to fetch leaderboard: " + e.getMessage()));
        }
    }
}
