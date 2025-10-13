package com.valorantarena.repository;

import com.valorantarena.entity.Match;
import com.valorantarena.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    
    // Find matches by tournament
    List<Match> findByTournamentId(Long tournamentId);
    
    // Find matches by team
    @Query("SELECT m FROM Match m WHERE m.team1.id = :teamId OR m.team2.id = :teamId")
    List<Match> findByTeamId(Long teamId);
    
    // Find matches by status
    List<Match> findByStatus(MatchStatus status);
    
    // Find upcoming matches
    @Query("SELECT m FROM Match m WHERE m.scheduledTime > :now AND m.status = 'SCHEDULED' ORDER BY m.scheduledTime ASC")
    List<Match> findUpcomingMatches(LocalDateTime now);
    
    // Find live matches
    @Query("SELECT m FROM Match m WHERE m.status = 'IN_PROGRESS'")
    List<Match> findLiveMatches();
    
    // Find matches by tournament and round
    List<Match> findByTournamentIdAndRound(Long tournamentId, Integer round);
}
