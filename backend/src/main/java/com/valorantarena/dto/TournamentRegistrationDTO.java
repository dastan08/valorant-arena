package com.valorantarena.dto;

import com.valorantarena.model.TournamentEntry.EntryType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TournamentRegistrationDTO {
    
    @NotNull(message = "Entry type is required")
    private EntryType entryType;
    
    private Long teamId; // Required if entryType is TEAM
}
