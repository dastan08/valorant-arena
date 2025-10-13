package com.valorantarena.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TournamentModeratorRequest {
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String notes;
}
