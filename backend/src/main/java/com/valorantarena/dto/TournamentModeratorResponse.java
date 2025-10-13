package com.valorantarena.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentModeratorResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private Long assignedById;
    private String assignedByUsername;
    private LocalDateTime assignedAt;
    private String notes;
}
