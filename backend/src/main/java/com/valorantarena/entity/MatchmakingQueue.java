package com.valorantarena.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "matchmaking_queue")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingQueue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private Integer elo;
    
    @Column(nullable = false)
    private String region;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status = QueueStatus.SEARCHING;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime queuedAt;
    
    @Column
    private LocalDateTime matchFoundAt;
    
    @Column
    private Long matchId; // Reference to the match when found
    
    public enum QueueStatus {
        SEARCHING,
        MATCH_FOUND,
        ACCEPTED,
        DECLINED,
        EXPIRED
    }
}
