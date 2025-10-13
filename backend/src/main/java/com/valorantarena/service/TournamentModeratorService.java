package com.valorantarena.service;

import com.valorantarena.dto.TournamentModeratorRequest;
import com.valorantarena.dto.TournamentModeratorResponse;
import com.valorantarena.model.Tournament;
import com.valorantarena.entity.TournamentModerator;
import com.valorantarena.entity.User;
import com.valorantarena.enums.UserRole;
import com.valorantarena.repository.TournamentModeratorRepository;
import com.valorantarena.repository.TournamentRepository;
import com.valorantarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentModeratorService {
    
    private final TournamentModeratorRepository moderatorRepository;
    private final TournamentRepository tournamentRepository;
    private final UserRepository userRepository;
    
    @Transactional
    public TournamentModeratorResponse assignModerator(Long tournamentId, TournamentModeratorRequest request, Long assignedById) {
        // Validate tournament exists
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        
        // Validate user exists
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate user has TOURNAMENT_MODERATOR role
        if (user.getRole() != UserRole.TOURNAMENT_MODERATOR && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("User must have TOURNAMENT_MODERATOR or ADMIN role");
        }
        
        // Check if already assigned
        if (moderatorRepository.existsByTournamentIdAndUserId(tournamentId, request.getUserId())) {
            throw new RuntimeException("User is already assigned as moderator for this tournament");
        }
        
        // Get assigner
        User assignedBy = userRepository.findById(assignedById)
                .orElseThrow(() -> new RuntimeException("Assigner not found"));
        
        // Create assignment
        TournamentModerator moderator = new TournamentModerator();
        moderator.setTournament(tournament);
        moderator.setUser(user);
        moderator.setAssignedBy(assignedBy);
        moderator.setNotes(request.getNotes());
        
        TournamentModerator saved = moderatorRepository.save(moderator);
        
        return mapToResponse(saved);
    }
    
    @Transactional
    public void removeModerator(Long tournamentId, Long userId, Long requesterId) {
        // Validate requester has permission
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        
        // Only organizer (creator) or admin can remove moderators
        if (!tournament.getOrganizer().getId().equals(requesterId) && requester.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only tournament organizer or admin can remove moderators");
        }
        
        moderatorRepository.deleteByTournamentIdAndUserId(tournamentId, userId);
    }
    
    public List<TournamentModeratorResponse> getTournamentModerators(Long tournamentId) {
        return moderatorRepository.findByTournamentId(tournamentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<Long> getUserModeratedTournaments(Long userId) {
        return moderatorRepository.findTournamentIdsByUserId(userId);
    }
    
    public boolean isModeratorForTournament(Long tournamentId, Long userId) {
        return moderatorRepository.existsByTournamentIdAndUserId(tournamentId, userId);
    }
    
    private TournamentModeratorResponse mapToResponse(TournamentModerator moderator) {
        return new TournamentModeratorResponse(
                moderator.getId(),
                moderator.getUser().getId(),
                moderator.getUser().getUsername(),
                moderator.getUser().getEmail(),
                moderator.getAssignedBy().getId(),
                moderator.getAssignedBy().getUsername(),
                moderator.getAssignedAt(),
                moderator.getNotes()
        );
    }
}
