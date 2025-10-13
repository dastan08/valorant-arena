package com.valorantarena.model;

import com.valorantarena.entity.User;
import com.valorantarena.entity.Team;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tournament_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType;

    @Column(name = "seed_number")
    private Integer seedNumber;

    @Column(name = "checked_in")
    private Boolean checkedIn = false;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_id")
    private String paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EntryStatus status = EntryStatus.REGISTERED;

    @Column(name = "eliminated_at")
    private LocalDateTime eliminatedAt;

    @Column(name = "elimination_round")
    private Integer eliminationRound;

    @Column(name = "final_placement")
    private Integer finalPlacement;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum EntryType {
        SOLO,
        TEAM
    }

    public enum PaymentStatus {
        PENDING,
        PAID,
        REFUNDED
    }

    public enum EntryStatus {
        REGISTERED,
        CHECKED_IN,
        DISQUALIFIED,
        ELIMINATED,
        WINNER
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

    public String getParticipantName() {
        if (entryType == EntryType.TEAM && team != null) {
            return team.getName();
        } else if (entryType == EntryType.SOLO && user != null) {
            return user.getUsername();
        }
        return "Unknown";
    }
}
