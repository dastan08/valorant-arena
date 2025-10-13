package com.valorantarena.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.valorantarena.enums.UserRole;
import com.valorantarena.enums.ValorantRank;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email"),
    @UniqueConstraint(columnNames = "valorantId")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 30)
    @Column(nullable = false, unique = true)
    private String username;
    
    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;
    
    @NotBlank
    @Size(min = 6)
    @JsonIgnore
    @Column(nullable = false)
    private String password;
    
    @NotBlank
    @Column(nullable = false, unique = true)
    private String valorantId;
    
    @Column(length = 20)
    private String region; // Riot account region (americas, europe, asia, sea)
    
    @Column(unique = true)
    private String discordId; // Discord user ID
    
    @Column
    private String discordUsername; // Discord username
    
    @Column
    private String discordAvatar; // Discord avatar hash
    
    @Column(unique = true)
    private String riotPuuid; // Riot's unique player ID (from RSO)
    
    @Column
    private String riotGameName; // Riot game name (e.g., "PlayerName")
    
    @Column
    private String riotTagLine; // Riot tag line (e.g., "NA1")
    
    @Column(length = 1000)
    @JsonIgnore
    private String riotAccessToken; // For Riot API calls
    
    @Column(length = 1000)
    @JsonIgnore
    private String riotRefreshToken; // To refresh access token
    
    @Column
    @JsonIgnore
    private LocalDateTime riotTokenExpiry; // When access token expires
    
    @Enumerated(EnumType.STRING)
    @Column(name = "`rank`", nullable = false)
    private ValorantRank rank;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "`role`", nullable = false)
    private UserRole role = UserRole.USER;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private com.valorantarena.enums.UserStatus status = com.valorantarena.enums.UserStatus.ACTIVE;
    
    @Column(nullable = false)
    private String avatar = "default-avatar.png";
    
    @Transient // Not stored in database, computed on the fly
    private String profilePictureUrl;
    
    @Column(nullable = false)
    private Boolean isVerified = false;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    @JsonIgnore
    private Team team;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "team_role")
    private com.valorantarena.enums.TeamRole teamRole;
    
    /**
     * Get the profile picture URL
     * Returns Discord avatar if available and no custom avatar is set
     */
    public String getProfilePictureUrl() {
        // If user has custom avatar (not default), use it
        if (avatar != null && !avatar.equals("default-avatar.png")) {
            return "/uploads/avatars/" + avatar;
        }
        
        // If Discord is linked and has avatar, use Discord CDN
        if (discordId != null && discordAvatar != null) {
            return "https://cdn.discordapp.com/avatars/" + discordId + "/" + discordAvatar + ".png";
        }
        
        // Fallback to default avatar
        return "/assets/default-avatar.png";
    }
    
    // Public getters for stats (for DTO access)
    public Integer getMatchesPlayed() { return stats.getMatchesPlayed(); }
    public Integer getWins() { return stats.getWins(); }
    public Integer getLosses() { return stats.getLosses(); }
    public Integer getKills() { return stats.getKills(); }
    public Integer getDeaths() { return stats.getDeaths(); }
    public Integer getAssists() { return stats.getAssists(); }
    public Double getHeadshotPercentage() { return stats.getHeadshotPercentage(); }
    public Integer getAverageCombatScore() { return stats.getAverageCombatScore(); }
    
    @Embedded
    private UserStats stats = new UserStats();
    
    @Column(nullable = false)
    private Integer elo = 1000;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper method to update stats after a match
    public void updateStats(boolean isWin, MatchStats matchStats) {
        this.stats.setMatchesPlayed(this.stats.getMatchesPlayed() + 1);
        
        if (isWin) {
            this.stats.setWins(this.stats.getWins() + 1);
            this.elo += 25; // Base ELO gain for win
        } else {
            this.stats.setLosses(this.stats.getLosses() + 1);
            this.elo = Math.max(0, this.elo - 15); // Base ELO loss, never go below 0
        }
        
        // Update combat stats
        this.stats.setKills(this.stats.getKills() + matchStats.getKills());
        this.stats.setDeaths(this.stats.getDeaths() + matchStats.getDeaths());
        this.stats.setAssists(this.stats.getAssists() + matchStats.getAssists());
        
        // Calculate new averages
        int totalKills = this.stats.getKills();
        int totalDeaths = this.stats.getDeaths();
        
        if (totalDeaths > 0) {
            int avgCombatScore = (this.stats.getKills() * 150 + this.stats.getAssists() * 50) 
                                / this.stats.getMatchesPlayed();
            this.stats.setAverageCombatScore(avgCombatScore);
            
            if (matchStats.getHeadshots() > 0 && matchStats.getShots() > 0) {
                double matchHSRate = (matchStats.getHeadshots() * 100.0) / matchStats.getShots();
                double newHSRate = (this.stats.getHeadshotPercentage() * (this.stats.getMatchesPlayed() - 1) 
                                   + matchHSRate) / this.stats.getMatchesPlayed();
                this.stats.setHeadshotPercentage(newHSRate);
            }
        }
    }
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class UserStats {
    @Column(nullable = false)
    private Integer matchesPlayed = 0;
    
    @Column(nullable = false)
    private Integer wins = 0;
    
    @Column(nullable = false)
    private Integer losses = 0;
    
    @Column(nullable = false)
    private Integer kills = 0;
    
    @Column(nullable = false)
    private Integer deaths = 0;
    
    @Column(nullable = false)
    private Integer assists = 0;
    
    @Column(nullable = false)
    private Double headshotPercentage = 0.0;
    
    @Column(nullable = false)
    private Integer averageCombatScore = 0;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class MatchStats {
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer headshots;
    private Integer shots;
}
