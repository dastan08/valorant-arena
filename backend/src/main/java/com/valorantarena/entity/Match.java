package com.valorantarena.entity;

import com.valorantarena.enums.MatchStatus;
import com.valorantarena.model.Tournament;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Match {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team1_id", nullable = false)
    private Team team1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team2_id", nullable = false)
    private Team team2;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.SCHEDULED;
    
    @Column(nullable = false)
    private Integer team1Score = 0;
    
    @Column(nullable = false)
    private Integer team2Score = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private Team winner;
    
    @NotNull
    @Column(nullable = false)
    private LocalDateTime scheduledTime;
    
    @Column
    private LocalDateTime startTime;
    
    @Column
    private LocalDateTime endTime;
    
    @Column(nullable = false)
    private Integer round = 1;
    
    @Column(nullable = false)
    private Integer bestOf = 1; // Best of 1, 3, 5, etc.
    
    @Column(length = 1000)
    private String notes;
    
    @Column
    private String serverInfo;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Helper methods
    public boolean isCompleted() {
        return status == MatchStatus.COMPLETED;
    }
    
    public void startMatch() {
        this.status = MatchStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
    }
    
    public void completeMatch(Team winningTeam, int team1FinalScore, int team2FinalScore) {
        this.status = MatchStatus.COMPLETED;
        this.winner = winningTeam;
        this.team1Score = team1FinalScore;
        this.team2Score = team2FinalScore;
        this.endTime = LocalDateTime.now();
    }
    
    public void cancelMatch() {
        this.status = MatchStatus.CANCELLED;
    }
    
    public Team getOpponent(Team team) {
        if (team.equals(team1)) {
            return team2;
        } else if (team.equals(team2)) {
            return team1;
        }
        return null;
    }
}
