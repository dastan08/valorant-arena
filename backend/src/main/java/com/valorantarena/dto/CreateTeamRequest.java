package com.valorantarena.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {
    private String name;
    private String tag; // Team tag (e.g., "TSM", "SEN")
    private String description;
    private String logo; // URL or filename
    private Integer maxMembers = 5; // Default 5 for VALORANT
}
