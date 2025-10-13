package com.valorantarena.dto;

import com.valorantarena.model.Tournament.GameMode;
import com.valorantarena.model.Tournament.TournamentFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CreateTournamentDTO {
    
    @NotBlank(message = "Tournament name is required")
    @Size(min = 3, max = 255, message = "Tournament name must be between 3 and 255 characters")
    private String name;
    
    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
    
    private String bannerUrl;
    
    @NotNull(message = "Game mode is required")
    private GameMode gameMode;
    
    @NotNull(message = "Tournament format is required")
    private TournamentFormat format;
    
    @NotNull(message = "Max participants is required")
    @Min(value = 4, message = "Minimum 4 participants required")
    @Max(value = 256, message = "Maximum 256 participants allowed")
    private Integer maxParticipants;
    
    @Min(value = 2, message = "Minimum 2 participants required")
    private Integer minParticipants = 4;
    
    @Min(value = 1, message = "Team size must be at least 1")
    private Integer teamSize = 5;
    
    // Entry Requirements
    private String minRank;
    private String maxRank;
    private String region;
    
    @DecimalMin(value = "0.0", message = "Entry fee cannot be negative")
    private BigDecimal entryFee = BigDecimal.ZERO;
    
    // Prize Pool
    @DecimalMin(value = "0.0", message = "Prize pool cannot be negative")
    private BigDecimal prizePool = BigDecimal.ZERO;
    
    private Map<String, Integer> prizeDistribution;
    
    // Schedule
    @NotNull(message = "Registration start time is required")
    @Future(message = "Registration start must be in the future")
    private LocalDateTime registrationStart;
    
    @NotNull(message = "Registration end time is required")
    @Future(message = "Registration end must be in the future")
    private LocalDateTime registrationEnd;
    
    @NotNull(message = "Check-in start time is required")
    @Future(message = "Check-in start must be in the future")
    private LocalDateTime checkInStart;
    
    @NotNull(message = "Check-in end time is required")
    @Future(message = "Check-in end must be in the future")
    private LocalDateTime checkInEnd;
    
    @NotNull(message = "Tournament start time is required")
    @Future(message = "Tournament start must be in the future")
    private LocalDateTime tournamentStart;
    
    // Rules & Settings
    @NotBlank(message = "Tournament rules are required")
    private String rules;
    
    private String matchFormat = "BO1";
    private List<String> mapPool;
    private Boolean allowSubstitutes = false;
    private Integer maxSubstitutes = 0;
}
