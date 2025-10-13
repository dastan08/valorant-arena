package com.valorantarena.dto;

import com.valorantarena.model.Tournament;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class TournamentResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String bannerUrl;
    private String gameMode;
    private String format;
    private Integer maxParticipants;
    private Integer minParticipants;
    private Integer teamSize;
    private String region;
    private BigDecimal entryFee;
    private BigDecimal prizePool;
    private Map<String, Integer> prizeDistribution;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime checkInStart;
    private LocalDateTime checkInEnd;
    private LocalDateTime tournamentStart;
    private LocalDateTime startDate; // Alias for frontend compatibility
    private LocalDateTime tournamentEnd;
    private String rules;
    private String matchFormat;
    private List<String> mapPool;
    private Boolean allowSubstitutes;
    private Integer maxSubstitutes;
    private String status;
    private Integer currentRound;
    private Integer totalRounds;
    private Boolean isFeatured;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Organizer info (flattened)
    private Long organizerId;
    private String organizerName;
    private String organizationName;
    
    // Frontend compatibility aliases
    private String banner; // Alias for bannerUrl
    private Integer maxTeams; // Alias for maxParticipants
    private List<Object> registeredTeams; // Empty list for now, will be populated later
    
    public static TournamentResponseDTO fromEntity(Tournament tournament) {
        TournamentResponseDTO dto = new TournamentResponseDTO();
        dto.setId(tournament.getId());
        dto.setName(tournament.getName());
        dto.setDescription(tournament.getDescription());
        dto.setBannerUrl(tournament.getBannerUrl());
        dto.setGameMode(tournament.getGameMode().name());
        dto.setFormat(tournament.getFormat().name());
        dto.setMaxParticipants(tournament.getMaxParticipants());
        dto.setMinParticipants(tournament.getMinParticipants());
        dto.setTeamSize(tournament.getTeamSize());
        dto.setRegion(tournament.getRegion());
        dto.setEntryFee(tournament.getEntryFee());
        dto.setPrizePool(tournament.getPrizePool());
        dto.setPrizeDistribution(tournament.getPrizeDistribution());
        dto.setRegistrationStart(tournament.getRegistrationStart());
        dto.setRegistrationEnd(tournament.getRegistrationEnd());
        dto.setCheckInStart(tournament.getCheckInStart());
        dto.setCheckInEnd(tournament.getCheckInEnd());
        dto.setTournamentStart(tournament.getTournamentStart());
        dto.setStartDate(tournament.getTournamentStart()); // Alias for frontend
        dto.setTournamentEnd(tournament.getTournamentEnd());
        dto.setRules(tournament.getRules());
        dto.setMatchFormat(tournament.getMatchFormat());
        dto.setMapPool(tournament.getMapPool());
        dto.setAllowSubstitutes(tournament.getAllowSubstitutes());
        dto.setMaxSubstitutes(tournament.getMaxSubstitutes());
        dto.setStatus(tournament.getStatus().name());
        dto.setCurrentRound(tournament.getCurrentRound());
        dto.setTotalRounds(tournament.getTotalRounds());
        dto.setIsFeatured(tournament.getIsFeatured());
        dto.setIsVerified(tournament.getIsVerified());
        dto.setCreatedAt(tournament.getCreatedAt());
        dto.setUpdatedAt(tournament.getUpdatedAt());
        
        // Flatten organizer info
        if (tournament.getOrganizer() != null) {
            dto.setOrganizerId(tournament.getOrganizer().getId());
            dto.setOrganizationName(tournament.getOrganizer().getOrganizationName());
            if (tournament.getOrganizer().getUser() != null) {
                dto.setOrganizerName(tournament.getOrganizer().getUser().getUsername());
            }
        }
        
        // Set frontend compatibility aliases
        dto.setBanner(tournament.getBannerUrl());
        dto.setMaxTeams(tournament.getMaxParticipants());
        dto.setRegisteredTeams(new java.util.ArrayList<>()); // Empty list for now
        
        return dto;
    }
}
