package com.valorantarena.dto;

import com.valorantarena.entity.User;
import com.valorantarena.enums.TeamRole;
import com.valorantarena.enums.ValorantRank;
import lombok.Data;

@Data
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String valorantId;
    private String region;
    private ValorantRank rank;
    private String role;
    private String avatar;
    private boolean isVerified;
    private TeamRole teamRole;
    private Integer elo;
    
    // Discord fields
    private String discordId;
    private String discordUsername;
    private String discordAvatar;
    private String profilePictureUrl;
    
    // Stats
    private UserStatsDto stats;
    
    // Team info
    private TeamBasicInfo team;
    
    @Data
    public static class TeamBasicInfo {
        private Long id;
        private String name;
        private String tag;
        private String logo;
        private UserBasicInfo captain;
        private UserBasicInfo manager;
        
        public TeamBasicInfo(Long id, String name, String tag, String logo, UserBasicInfo captain, UserBasicInfo manager) {
            this.id = id;
            this.name = name;
            this.tag = tag;
            this.logo = logo;
            this.captain = captain;
            this.manager = manager;
        }
    }
    
    @Data
    public static class UserBasicInfo {
        private Long id;
        private String username;
        
        public UserBasicInfo(Long id, String username) {
            this.id = id;
            this.username = username;
        }
    }
    
    @Data
    public static class UserStatsDto {
        private int matchesPlayed;
        private int wins;
        private int losses;
        private int kills;
        private int deaths;
        private int assists;
        private double headshotPercentage;
        private double averageCombatScore;
    }
    
    public static UserProfileResponse fromUser(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setValorantId(user.getValorantId());
        response.setRegion(user.getRegion());
        response.setRank(user.getRank());
        response.setRole(user.getRole().name());
        response.setAvatar(user.getAvatar());
        response.setVerified(user.getIsVerified());
        response.setTeamRole(user.getTeamRole());
        response.setElo(user.getElo());
        
        // Discord fields
        response.setDiscordId(user.getDiscordId());
        response.setDiscordUsername(user.getDiscordUsername());
        response.setDiscordAvatar(user.getDiscordAvatar());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        
        // Stats
        UserStatsDto statsDto = new UserStatsDto();
        statsDto.setMatchesPlayed(user.getMatchesPlayed());
        statsDto.setWins(user.getWins());
        statsDto.setLosses(user.getLosses());
        statsDto.setKills(user.getKills());
        statsDto.setDeaths(user.getDeaths());
        statsDto.setAssists(user.getAssists());
        statsDto.setHeadshotPercentage(user.getHeadshotPercentage());
        statsDto.setAverageCombatScore(user.getAverageCombatScore());
        response.setStats(statsDto);
        
        // Team info (if user is in a team)
        if (user.getTeam() != null) {
            UserBasicInfo captain = null;
            UserBasicInfo manager = null;
            
            if (user.getTeam().getCaptain() != null) {
                captain = new UserBasicInfo(
                    user.getTeam().getCaptain().getId(),
                    user.getTeam().getCaptain().getUsername()
                );
            }
            
            if (user.getTeam().getManager() != null) {
                manager = new UserBasicInfo(
                    user.getTeam().getManager().getId(),
                    user.getTeam().getManager().getUsername()
                );
            }
            
            response.setTeam(new TeamBasicInfo(
                user.getTeam().getId(),
                user.getTeam().getName(),
                user.getTeam().getTag(),
                user.getTeam().getLogo(),
                captain,
                manager
            ));
        }
        
        return response;
    }
}
