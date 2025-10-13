package com.valorantarena.repository;

import com.valorantarena.model.TournamentMatch;
import com.valorantarena.model.TournamentMatch.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TournamentMatchRepository extends JpaRepository<TournamentMatch, Long> {
    
    List<TournamentMatch> findByTournamentId(Long tournamentId);
    
    List<TournamentMatch> findByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);
    
    @Query("SELECT m FROM TournamentMatch m WHERE m.tournament.id = :tournamentId AND m.status = :status")
    List<TournamentMatch> findByTournamentIdAndStatus(@Param("tournamentId") Long tournamentId, @Param("status") MatchStatus status);
    
    @Query("SELECT m FROM TournamentMatch m WHERE (m.participant1.user.id = :userId OR m.participant2.user.id = :userId) AND m.status IN ('SCHEDULED', 'READY', 'IN_PROGRESS')")
    List<TournamentMatch> findUpcomingMatchesForUser(@Param("userId") Long userId);
    
    @Query("SELECT m FROM TournamentMatch m WHERE (m.participant1.team.id = :teamId OR m.participant2.team.id = :teamId) AND m.status IN ('SCHEDULED', 'READY', 'IN_PROGRESS')")
    List<TournamentMatch> findUpcomingMatchesForTeam(@Param("teamId") Long teamId);
    
    @Query("SELECT m FROM TournamentMatch m WHERE m.scheduledTime BETWEEN :start AND :end ORDER BY m.scheduledTime ASC")
    List<TournamentMatch> findMatchesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT m FROM TournamentMatch m WHERE m.participant1.user.id = :userId OR m.participant2.user.id = :userId")
    List<TournamentMatch> findByParticipantUserId(@Param("userId") Long userId);
}
