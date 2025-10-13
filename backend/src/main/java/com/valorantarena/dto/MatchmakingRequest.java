package com.valorantarena.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingRequest {
    private String region; // e.g., "NA", "EU", "ASIA"
}
