package com.valorantarena.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerStatsDto {
    
    private String name;
    private String tag;
    private String region;
    
    // Rank and MMR data
    private Integer currentTier;
    private String currentTierPatched;
    private Integer rankedRating;
    private Integer elo;
    
    @JsonProperty("mmr_change_to_last_game")
    private Integer mmrChangeToLastGame;
    
    @JsonProperty("games_needed_for_rating")
    private Integer gamesNeededForRating;
    
    // Account data
    private Integer accountLevel;
    private CardDto card;
    
    private Boolean old;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardDto {
        private String small;
        private String large;
        private String wide;
        private String id;
    }
}
