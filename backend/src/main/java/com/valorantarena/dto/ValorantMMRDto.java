package com.valorantarena.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValorantMMRDto {
    
    private String status;
    
    @JsonProperty("data")
    private MMRData data;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MMRData {
        @JsonProperty("currenttier")
        private Integer currentTier;
        
        @JsonProperty("currenttierpatched")
        private String currentTierPatched;
        
        @JsonProperty("ranking_in_tier")
        private Integer rankedRating;
        
        @JsonProperty("mmr_change_to_last_game")
        private Integer mmrChangeToLastGame;
        
        @JsonProperty("elo")
        private Integer elo;
        
        @JsonProperty("games_needed_for_rating")
        private Integer gamesNeededForRating;
        
        @JsonProperty("old")
        private Boolean old;
    }
}
