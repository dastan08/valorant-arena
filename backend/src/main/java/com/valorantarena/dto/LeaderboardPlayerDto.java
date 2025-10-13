package com.valorantarena.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardPlayerDto {
    
    @JsonProperty("leaderboard_rank")
    private Integer leaderboardRank;
    
    @JsonProperty("rankedRating")
    private Integer rankedRating;
    
    @JsonProperty("numberOfWins")
    private Integer numberOfWins;
    
    @JsonProperty("gameName")
    private String gameName;
    
    @JsonProperty("tagLine")
    private String tagLine;
    
    @JsonProperty("puuid")
    private String puuid;
}
