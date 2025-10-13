package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.model.Bracket;
import com.valorantarena.service.BracketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brackets")
@RequiredArgsConstructor
public class BracketController {

    private final BracketService bracketService;

    @PostMapping("/tournaments/{tournamentId}/generate")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> generateBracket(@PathVariable Long tournamentId) {
        try {
            bracketService.generateBracket(tournamentId);
            return ResponseEntity.ok(ApiResponse.success("Bracket generated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tournaments/{tournamentId}")
    public ResponseEntity<ApiResponse<List<Bracket>>> getTournamentBracket(@PathVariable Long tournamentId) {
        try {
            List<Bracket> brackets = bracketService.getTournamentBracket(tournamentId);
            return ResponseEntity.ok(ApiResponse.success("Bracket retrieved successfully", brackets));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/tournaments/{tournamentId}/type/{bracketType}")
    public ResponseEntity<ApiResponse<List<Bracket>>> getBracketByType(
            @PathVariable Long tournamentId,
            @PathVariable Bracket.BracketType bracketType) {
        try {
            List<Bracket> brackets = bracketService.getBracketByType(tournamentId, bracketType);
            return ResponseEntity.ok(ApiResponse.success("Bracket retrieved successfully", brackets));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(e.getMessage()));
        }
    }
}
