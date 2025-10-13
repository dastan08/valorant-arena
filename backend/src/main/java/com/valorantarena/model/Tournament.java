package com.valorantarena.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Entity
@Table(name = "tournaments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tournament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    @JsonIgnoreProperties({"tournaments", "user", "hibernateLazyInitializer", "handler"})
    private Organizer organizer;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    // Tournament Configuration
    @Enumerated(EnumType.STRING)
    @Column(name = "game_mode", nullable = false)
    private GameMode gameMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", nullable = false)
    private TournamentFormat format;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "min_participants", nullable = false)
    private Integer minParticipants = 4;

    @Column(name = "team_size", nullable = false)
    private Integer teamSize = 5;

    // Entry Requirements
    @Column(name = "min_rank")
    private String minRank;

    @Column(name = "max_rank")
    private String maxRank;

    @Column(name = "region")
    private String region;

    @Column(name = "entry_fee", precision = 10, scale = 2)
    private BigDecimal entryFee = BigDecimal.ZERO;

    // Prize Pool
    @Column(name = "prize_pool", precision = 10, scale = 2)
    private BigDecimal prizePool = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "prize_distribution")
    private Map<String, Integer> prizeDistribution;

    // Schedule
    @Column(name = "registration_start", nullable = false)
    private LocalDateTime registrationStart;

    @Column(name = "registration_end", nullable = false)
    private LocalDateTime registrationEnd;

    @Column(name = "check_in_start", nullable = false)
    private LocalDateTime checkInStart;

    @Column(name = "check_in_end", nullable = false)
    private LocalDateTime checkInEnd;

    @Column(name = "tournament_start", nullable = false)
    private LocalDateTime tournamentStart;

    @Column(name = "tournament_end")
    private LocalDateTime tournamentEnd;

    // Rules & Settings
    @Column(name = "rules", columnDefinition = "TEXT")
    private String rules;

    @Column(name = "match_format")
    private String matchFormat = "BO1";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "map_pool")
    private List<String> mapPool;

    @Column(name = "allow_substitutes")
    private Boolean allowSubstitutes = false;

    @Column(name = "max_substitutes")
    private Integer maxSubstitutes = 0;

    // Status & Metadata
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TournamentStatus status = TournamentStatus.DRAFT;

    @Column(name = "current_round")
    private Integer currentRound = 0;

    @Column(name = "total_rounds")
    private Integer totalRounds;

    // Moderation
    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "moderation_notes", columnDefinition = "TEXT")
    private String moderationNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum GameMode {
        SOLO,
        DUO,
        TEAM_5V5
    }

    public enum TournamentFormat {
        SINGLE_ELIMINATION,
        DOUBLE_ELIMINATION,
        ROUND_ROBIN,
        SWISS
    }

    public enum TournamentStatus {
        DRAFT,
        OPEN,
        REGISTRATION_CLOSED,
        CHECK_IN,
        ONGOING,
        COMPLETED,
        CANCELLED
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
