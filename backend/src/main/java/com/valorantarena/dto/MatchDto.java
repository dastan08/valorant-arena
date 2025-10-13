package com.valorantarena.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchDto {
    
    @JsonProperty("match_id")
    private String matchId;
    
    private String map;
    
    @JsonProperty("game_start")
    private Long gameStart;
    
    @JsonProperty("game_length")
    private Long gameLength;
    
    private String mode;
    
    @JsonProperty("rounds_played")
    private Integer roundsPlayed;
    
    private String season;
    
    private String region;
    
    private String cluster;
    
    private PlayerMatchStats player;
    
    private TeamStats red;
    
    private TeamStats blue;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerMatchStats {
        private String name;
        private String tag;
        private String team;
        private String character;
        private Integer kills;
        private Integer deaths;
        private Integer assists;
        
        @JsonProperty("damage_made")
        private Integer damageMade;
        
        @JsonProperty("damage_received")
        private Integer damageReceived;
        
        @JsonProperty("headshots")
        private Integer headshots;
        
        @JsonProperty("bodyshots")
        private Integer bodyshots;
        
        @JsonProperty("legshots")
        private Integer legshots;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamStats {
        
        @JsonProperty("has_won")
        private Boolean hasWon;
        
        @JsonProperty("rounds_won")
        private Integer roundsWon;
        
        @JsonProperty("rounds_lost")
        private Integer roundsLost;
    }
}
