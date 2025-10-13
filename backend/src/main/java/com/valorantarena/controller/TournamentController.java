package com.valorantarena.controller;

import com.valorantarena.dto.ApiResponse;
import com.valorantarena.dto.CreateTournamentDTO;
import com.valorantarena.dto.TournamentRegistrationDTO;
import com.valorantarena.dto.TournamentResponseDTO;
import com.valorantarena.entity.User;
import com.valorantarena.model.Tournament;
import com.valorantarena.security.UserDetailsImpl;
import com.valorantarena.model.Tournament.TournamentStatus;
import com.valorantarena.model.TournamentEntry;
import com.valorantarena.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    
    private final TournamentService tournamentService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Tournament>> createTournament(
            @Valid @RequestBody CreateTournamentDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Tournament tournament = tournamentService.createTournament(dto, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Tournament created successfully", tournament));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TournamentResponseDTO>> getTournament(@PathVariable Long id) {
        Tournament tournament = tournamentService.getTournamentById(id);
        TournamentResponseDTO dto = TournamentResponseDTO.fromEntity(tournament);
        
        try {
            // Get registered teams/participants
            List<TournamentEntry> entries = tournamentService.getTournamentParticipants(id);
            System.out.println("Found " + entries.size() + " tournament entries for tournament " + id);
            
            List<Object> registeredTeams = new java.util.ArrayList<>();
            
            for (TournamentEntry entry : entries) {
                try {
                    System.out.println("Processing entry: " + entry.getId() + ", Team: " + (entry.getTeam() != null ? entry.getTeam().getName() : "null"));
                    
                    if (entry.getTeam() != null) {
                        // Add team info
                        java.util.Map<String, Object> teamInfo = new java.util.HashMap<>();
                        teamInfo.put("id", entry.getTeam().getId());
                        teamInfo.put("name", entry.getTeam().getName());
                        teamInfo.put("tag", entry.getTeam().getTag());
                        teamInfo.put("logo", entry.getTeam().getLogo());
                        registeredTeams.add(teamInfo);
                        System.out.println("Added team: " + entry.getTeam().getName());
                    }
                } catch (Exception e) {
                    // Skip this entry if there's an error
                    System.err.println("Error processing entry: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Total registered teams: " + registeredTeams.size());
            dto.setRegisteredTeams(registeredTeams);
        } catch (Exception e) {
            // If there's an error fetching entries, just return empty list
            System.err.println("Error fetching tournament entries: " + e.getMessage());
            e.printStackTrace();
            dto.setRegisteredTeams(new java.util.ArrayList<>());
        }
        
        return ResponseEntity.ok(ApiResponse.success("Tournament retrieved successfully", dto));
    }
    
    @GetMapping("/my-tournaments")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<TournamentResponseDTO>>> getMyTournaments(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<Tournament> tournaments = tournamentService.getTournamentsByOrganizer(userDetails.getId());
        List<TournamentResponseDTO> dtos = tournaments.stream()
                .map(TournamentResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Tournaments retrieved successfully", dtos));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Tournament>> updateTournament(
            @PathVariable Long id,
            @Valid @RequestBody CreateTournamentDTO dto,
            @AuthenticationPrincipal User user) {
        Tournament tournament = tournamentService.updateTournament(id, dto, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Tournament updated successfully", tournament));
    }
    
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> publishTournament(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        tournamentService.publishTournament(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Tournament published successfully", null));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Tournament>>> getTournaments(
            @RequestParam(required = false) TournamentStatus status,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "tournamentStart") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<Tournament> tournaments = tournamentService.getTournaments(status, region, pageable);
        return ResponseEntity.ok(ApiResponse.success("Tournaments retrieved successfully", tournaments));
    }
    
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<TournamentResponseDTO>>> getUpcomingTournaments() {
        List<Tournament> tournaments = tournamentService.getUpcomingTournaments();
        List<TournamentResponseDTO> dtos = tournaments.stream()
                .map(TournamentResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Upcoming tournaments retrieved successfully", dtos));
    }
    
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<TournamentResponseDTO>>> getFeaturedTournaments() {
        List<Tournament> tournaments = tournamentService.getFeaturedTournaments();
        List<TournamentResponseDTO> dtos = tournaments.stream()
                .map(TournamentResponseDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Featured tournaments retrieved successfully", dtos));
    }
    
    @PostMapping("/{id}/register")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TournamentEntry>> registerForTournament(
            @PathVariable Long id,
            @Valid @RequestBody TournamentRegistrationDTO dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        TournamentEntry entry = tournamentService.registerForTournament(id, dto, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Successfully registered for tournament", entry));
    }
    
    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> checkInForTournament(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        tournamentService.checkInForTournament(id, userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Successfully checked in", null));
    }
    
    @GetMapping("/{id}/participants")
    public ResponseEntity<ApiResponse<List<TournamentEntry>>> getTournamentParticipants(@PathVariable Long id) {
        List<TournamentEntry> participants = tournamentService.getTournamentParticipants(id);
        return ResponseEntity.ok(ApiResponse.success("Participants retrieved successfully", participants));
    }
    
    @GetMapping("/my-registrations")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TournamentEntry>>> getMyRegistrations(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<TournamentEntry> tournaments = tournamentService.getUserTournaments(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success("Your tournaments retrieved successfully", tournaments));
    }
}
