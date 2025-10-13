package com.valorantarena.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "teams", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name"),
    @UniqueConstraint(columnNames = "tag")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false, unique = true)
    private String name;
    
    @NotBlank
    @Size(min = 2, max = 5)
    @Column(nullable = false, unique = true)
    private String tag;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private String logo = "default-team-logo.png";
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "captain_id", nullable = false)
    @JsonIgnoreProperties({"team", "password", "riotAccessToken", "riotRefreshToken", "discordAccessToken", "discordRefreshToken"})
    private User captain;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manager_id")
    @JsonIgnoreProperties({"team", "password", "riotAccessToken", "riotRefreshToken", "discordAccessToken", "discordRefreshToken"})
    private User manager; // Optional manager, defaults to captain if null
    
    @OneToMany(mappedBy = "team", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"team", "password", "riotAccessToken", "riotRefreshToken", "discordAccessToken", "discordRefreshToken"})
    private List<User> members = new ArrayList<>();
    
    @Column(nullable = false)
    private Integer maxMembers = 7; // 6 players + 1 optional manager
    
    @Embedded
    private TeamStats stats = new TeamStats();
    
    @Column(nullable = false)
    private Integer elo = 1000;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isFull() {
        return members.size() >= maxMembers;
    }
    
    public boolean isCaptain(User user) {
        return captain != null && captain.getId().equals(user.getId());
    }
    
    public boolean isManager(User user) {
        // If no manager is set, captain is the manager
        if (manager == null) {
            return isCaptain(user);
        }
        return manager.getId().equals(user.getId());
    }
    
    public boolean canManageTeam(User user) {
        return isCaptain(user) || isManager(user);
    }
    
    public User getEffectiveManager() {
        return manager != null ? manager : captain;
    }
    
    public void addMember(User user, com.valorantarena.enums.TeamRole role) {
        if (!isFull()) {
            members.add(user);
            user.setTeam(this);
            user.setTeamRole(role);
        }
    }
    
    public void removeMember(User user) {
        members.remove(user);
        user.setTeam(null);
        user.setTeamRole(null);
    }
    
    public void updateStats(boolean isWin) {
        this.stats.setMatchesPlayed(this.stats.getMatchesPlayed() + 1);
        
        if (isWin) {
            this.stats.setWins(this.stats.getWins() + 1);
            this.elo += 30; // Base ELO gain for team win
        } else {
            this.stats.setLosses(this.stats.getLosses() + 1);
            this.elo = Math.max(0, this.elo - 20); // Base ELO loss
        }
        
        // Calculate win rate
        if (this.stats.getMatchesPlayed() > 0) {
            double winRate = (this.stats.getWins() * 100.0) / this.stats.getMatchesPlayed();
            this.stats.setWinRate(winRate);
        }
    }
}

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
class TeamStats {
    @Column(nullable = false)
    private Integer matchesPlayed = 0;
    
    @Column(nullable = false)
    private Integer wins = 0;
    
    @Column(nullable = false)
    private Integer losses = 0;
    
    @Column(nullable = false)
    private Double winRate = 0.0;
    
    @Column(nullable = false)
    private Integer tournamentsWon = 0;
}
