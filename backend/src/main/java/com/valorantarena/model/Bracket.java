package com.valorantarena.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "brackets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bracket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    @Column(name = "bracket_type", nullable = false)
    private BracketType bracketType;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "match_number", nullable = false)
    private Integer matchNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private TournamentMatch match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_match_bracket_id")
    private Bracket nextMatchBracket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loser_bracket_id")
    private Bracket loserBracket;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private BracketStatus status = BracketStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum BracketType {
        WINNERS,
        LOSERS
    }

    public enum BracketStatus {
        PENDING,
        READY,
        IN_PROGRESS,
        COMPLETED
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
