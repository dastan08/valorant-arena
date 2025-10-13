package com.valorantarena.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingResponse {
    private Long queueId;
    private String status;
    private Integer playerElo;
    private String playerRank;
    private LocalDateTime queuedAt;
    private Long estimatedWaitTime; // in seconds
    private MatchDetails matchDetails;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchDetails {
        private Long matchId;
        private List<PlayerInfo> team1;
        private List<PlayerInfo> team2;
        private Integer averageElo;
        private String map;
        private LocalDateTime matchStartTime;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerInfo {
        private Long userId;
        private String username;
        private Integer elo;
        private String rank;
        private String avatar;
    }
}
