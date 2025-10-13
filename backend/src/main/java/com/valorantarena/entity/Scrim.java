package com.valorantarena.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "scrims")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Scrim {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "challenger_team_id", nullable = false)
    private Team challengerTeam;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "opponent_team_id", nullable = false)
    private Team opponentTeam;
    
    @Column(nullable = false)
    private LocalDateTime scheduledTime;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScrimStatus status = ScrimStatus.PENDING;
    
    @Column(length = 500)
    private String message;
    
    @Column(nullable = false)
    private String map = "TBD";
    
    @Column(nullable = false)
    private Integer bestOf = 1; // Best of 1, 3, 5
    
    @Enumerated(EnumType.STRING)
    private ScrimResult result;
    
    @Column
    private Integer challengerScore;
    
    @Column
    private Integer opponentScore;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime respondedAt;
    
    @Column
    private LocalDateTime completedAt;
    
    public enum ScrimStatus {
        PENDING,      // Waiting for opponent response
        ACCEPTED,     // Opponent accepted, scrim scheduled
        DECLINED,     // Opponent declined
        CANCELLED,    // Cancelled by either team
        IN_PROGRESS,  // Scrim is currently happening
        COMPLETED     // Scrim finished
    }
    
    public enum ScrimResult {
        CHALLENGER_WIN,
        OPPONENT_WIN,
        DRAW,
        CANCELLED
    }
}
