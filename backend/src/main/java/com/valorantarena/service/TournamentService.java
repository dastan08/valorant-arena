
package com.valorantarena.service;

import com.valorantarena.dto.CreateTournamentDTO;
import com.valorantarena.dto.TournamentRegistrationDTO;
import com.valorantarena.entity.Team;
import com.valorantarena.entity.User;
import com.valorantarena.model.*;
import com.valorantarena.model.Tournament.TournamentStatus;
import com.valorantarena.model.TournamentEntry.EntryStatus;
import com.valorantarena.model.TournamentEntry.EntryType;
import com.valorantarena.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentService {
    
    private final TournamentRepository tournamentRepository;
    private final OrganizerRepository organizerRepository;
    private final TournamentEntryRepository entryRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    
    @Transactional
    public Tournament createTournament(CreateTournamentDTO dto, Long userId) {
        // Get or create organizer
        Organizer organizer = organizerRepository.findByUserId(userId)
            .orElseGet(() -> {
                // Auto-create organizer for users with ORGANIZER role
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
                
                Organizer newOrganizer = new Organizer();
                newOrganizer.setUser(user);
                newOrganizer.setOrganizationName(user.getUsername() + "'s Organization");
                newOrganizer.setVerificationStatus(Organizer.VerificationStatus.VERIFIED);
                return organizerRepository.save(newOrganizer);
            });
        
        if (organizer.getVerificationStatus() != Organizer.VerificationStatus.VERIFIED) {
            throw new RuntimeException("Organizer is not verified");
        }
        
        // Validate dates
        validateTournamentDates(dto);
        
        // Create tournament
        Tournament tournament = new Tournament();
        tournament.setOrganizer(organizer);
        tournament.setName(dto.getName());
        tournament.setDescription(dto.getDescription());
        tournament.setBannerUrl(dto.getBannerUrl());
        tournament.setGameMode(dto.getGameMode());
        tournament.setFormat(dto.getFormat());
        tournament.setMaxParticipants(dto.getMaxParticipants());
        tournament.setMinParticipants(dto.getMinParticipants());
        tournament.setTeamSize(dto.getTeamSize());
        tournament.setMinRank(dto.getMinRank());
        tournament.setMaxRank(dto.getMaxRank());
        tournament.setRegion(dto.getRegion());
        tournament.setEntryFee(dto.getEntryFee());
        tournament.setPrizePool(dto.getPrizePool());
        tournament.setPrizeDistribution(dto.getPrizeDistribution());
        tournament.setRegistrationStart(dto.getRegistrationStart());
        tournament.setRegistrationEnd(dto.getRegistrationEnd());
        tournament.setCheckInStart(dto.getCheckInStart());
        tournament.setCheckInEnd(dto.getCheckInEnd());
        tournament.setTournamentStart(dto.getTournamentStart());
        tournament.setRules(dto.getRules());
        tournament.setMatchFormat(dto.getMatchFormat());
        tournament.setMapPool(dto.getMapPool());
        tournament.setAllowSubstitutes(dto.getAllowSubstitutes());
        tournament.setMaxSubstitutes(dto.getMaxSubstitutes());
        tournament.setStatus(TournamentStatus.DRAFT);
        
        return tournamentRepository.save(tournament);
    }
    
    @Transactional(readOnly = true)
    public List<Tournament> getTournamentsByOrganizer(Long userId) {
        Organizer organizer = organizerRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Organizer not found"));
        List<Tournament> tournaments = tournamentRepository.findByOrganizerId(organizer.getId());
        // Force initialization of lazy-loaded relationships
        tournaments.forEach(t -> {
            t.getOrganizer().getOrganizationName(); // Initialize organizer
            if (t.getOrganizer().getUser() != null) {
                t.getOrganizer().getUser().getUsername(); // Initialize user
            }
        });
        return tournaments;
    }
    
    @Transactional
    public Tournament updateTournament(Long tournamentId, CreateTournamentDTO dto, Long userId) {
        Tournament tournament = getTournamentById(tournamentId);
        
        // Verify ownership
        if (!tournament.getOrganizer().getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this tournament");
        }
        
        // Can only update if in DRAFT status
        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new RuntimeException("Can only update tournaments in DRAFT status");
        }
        
        validateTournamentDates(dto);
        
        // Update fields
        tournament.setName(dto.getName());
        tournament.setDescription(dto.getDescription());
        tournament.setBannerUrl(dto.getBannerUrl());
        tournament.setGameMode(dto.getGameMode());
        tournament.setFormat(dto.getFormat());
        tournament.setMaxParticipants(dto.getMaxParticipants());
        tournament.setMinParticipants(dto.getMinParticipants());
        tournament.setMinRank(dto.getMinRank());
        tournament.setMaxRank(dto.getMaxRank());
        tournament.setRegion(dto.getRegion());
        tournament.setPrizePool(dto.getPrizePool());
        tournament.setPrizeDistribution(dto.getPrizeDistribution());
        tournament.setRegistrationStart(dto.getRegistrationStart());
        tournament.setRegistrationEnd(dto.getRegistrationEnd());
        tournament.setCheckInStart(dto.getCheckInStart());
        tournament.setCheckInEnd(dto.getCheckInEnd());
        tournament.setTournamentStart(dto.getTournamentStart());
        tournament.setRules(dto.getRules());
        tournament.setMatchFormat(dto.getMatchFormat());
        tournament.setMapPool(dto.getMapPool());
        
        return tournamentRepository.save(tournament);
    }
    
    @Transactional
    public void publishTournament(Long tournamentId, Long userId) {
        Tournament tournament = getTournamentById(tournamentId);
        
        // Load organizer with user to verify ownership
        Organizer organizer = tournament.getOrganizer();
        if (organizer.getUser() != null) {
            organizer.getUser().getId(); // Force initialization
        }
        
        // Verify ownership
        if (organizer.getUser() == null || !organizer.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't have permission to publish this tournament");
        }
        
        if (tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new RuntimeException("Tournament is not in DRAFT status");
        }
        
        tournament.setStatus(TournamentStatus.OPEN);
        tournamentRepository.save(tournament);
        
        // TODO: Send notifications to followers
    }
    
    @Transactional
    public TournamentEntry registerForTournament(Long tournamentId, TournamentRegistrationDTO dto, Long userId) {
        Tournament tournament = getTournamentById(tournamentId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Validate tournament status
        if (tournament.getStatus() != TournamentStatus.OPEN) {
            throw new RuntimeException("Tournament registration is not open");
        }
        
        // Check registration deadline
        if (LocalDateTime.now().isAfter(tournament.getRegistrationEnd())) {
            throw new RuntimeException("Registration deadline has passed");
        }
        
        // Check if tournament is full
        Long currentEntries = entryRepository.countByTournamentId(tournamentId);
        if (currentEntries >= tournament.getMaxParticipants()) {
            throw new RuntimeException("Tournament is full");
        }
        
        // Check if already registered
        if (dto.getEntryType() == EntryType.SOLO) {
            if (entryRepository.existsByTournamentIdAndUserId(tournamentId, userId)) {
                throw new RuntimeException("You are already registered for this tournament");
            }
        } else if (dto.getEntryType() == EntryType.TEAM) {
            if (dto.getTeamId() == null) {
                throw new RuntimeException("Team ID is required for team registration");
            }
            if (entryRepository.existsByTournamentIdAndTeamId(tournamentId, dto.getTeamId())) {
                throw new RuntimeException("This team is already registered for this tournament");
            }
        }
        
        // Create entry
        TournamentEntry entry = new TournamentEntry();
        entry.setTournament(tournament);
        entry.setEntryType(dto.getEntryType());
        entry.setStatus(EntryStatus.REGISTERED);
        
        if (dto.getEntryType() == EntryType.SOLO) {
            entry.setUser(user);
        } else {
            Team team = teamRepository.findById(dto.getTeamId())
                .orElseThrow(() -> new RuntimeException("Team not found"));
            
            // Verify user is captain
            if (!team.getCaptain().getId().equals(userId)) {
                throw new RuntimeException("Only team captain can register the team");
            }
            
            entry.setTeam(team);
        }
        
        return entryRepository.save(entry);
    }
    
    @Transactional
    public void checkInForTournament(Long tournamentId, Long userId) {
        Tournament tournament = getTournamentById(tournamentId);
        
        // Validate check-in window
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(tournament.getCheckInStart())) {
            throw new RuntimeException("Check-in has not started yet");
        }
        if (now.isAfter(tournament.getCheckInEnd())) {
            throw new RuntimeException("Check-in deadline has passed");
        }
        
        // Find user's entry
        TournamentEntry entry = entryRepository.findByTournamentIdAndUserId(tournamentId, userId)
            .orElseThrow(() -> new RuntimeException("You are not registered for this tournament"));
        
        if (entry.getCheckedIn()) {
            throw new RuntimeException("You have already checked in");
        }
        
        entry.setCheckedIn(true);
        entry.setCheckedInAt(LocalDateTime.now());
        entry.setStatus(EntryStatus.CHECKED_IN);
        entryRepository.save(entry);
    }
    
    @Transactional(readOnly = true)
    public Tournament getTournamentById(Long id) {
        Tournament tournament = tournamentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Tournament not found"));
        
        // Force initialization of lazy-loaded relationships
        if (tournament.getOrganizer() != null) {
            tournament.getOrganizer().getOrganizationName(); // Initialize organizer
            if (tournament.getOrganizer().getUser() != null) {
                tournament.getOrganizer().getUser().getUsername(); // Initialize user
            }
        }
        
        return tournament;
    }
    
    public Page<Tournament> getTournaments(TournamentStatus status, String region, Pageable pageable) {
        if (status != null && region != null) {
            return tournamentRepository.findByStatusAndRegion(status, region, pageable);
        } else if (status != null) {
            return tournamentRepository.findByStatus(status, pageable);
        } else {
            return tournamentRepository.findAll(pageable);
        }
    }
    
    @Transactional(readOnly = true)
    public List<Tournament> getUpcomingTournaments() {
        List<Tournament> tournaments = tournamentRepository.findUpcomingTournaments(LocalDateTime.now());
        // Force initialization of lazy-loaded relationships
        tournaments.forEach(t -> {
            if (t.getOrganizer() != null) {
                t.getOrganizer().getOrganizationName();
                if (t.getOrganizer().getUser() != null) {
                    t.getOrganizer().getUser().getUsername();
                }
            }
        });
        return tournaments;
    }
    
    @Transactional(readOnly = true)
    public List<Tournament> getFeaturedTournaments() {
        return tournamentRepository.findFeaturedTournaments();
    }
    
    @Transactional(readOnly = true)
    public List<TournamentEntry> getTournamentParticipants(Long tournamentId) {
        // Use JOIN FETCH to eagerly load teams in a single query
        return entryRepository.findByTournamentIdWithTeam(tournamentId);
    }
    
    public List<TournamentEntry> getUserTournaments(Long userId) {
        return entryRepository.findByUserId(userId);
    }
    
    private void validateTournamentDates(CreateTournamentDTO dto) {
        if (dto.getRegistrationEnd().isBefore(dto.getRegistrationStart())) {
            throw new RuntimeException("Registration end must be after registration start");
        }
        if (dto.getCheckInStart().isBefore(dto.getRegistrationEnd())) {
            throw new RuntimeException("Check-in start must be after registration end");
        }
        if (dto.getCheckInEnd().isBefore(dto.getCheckInStart())) {
            throw new RuntimeException("Check-in end must be after check-in start");
        }
        if (dto.getTournamentStart().isBefore(dto.getCheckInEnd())) {
            throw new RuntimeException("Tournament start must be after check-in end");
        }
    }
}
