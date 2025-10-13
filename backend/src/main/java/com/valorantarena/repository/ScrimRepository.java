package com.valorantarena.repository;

import com.valorantarena.entity.Scrim;
import com.valorantarena.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScrimRepository extends JpaRepository<Scrim, Long> {
    
    // Find all scrims for a team (as challenger or opponent)
    @Query("SELECT s FROM Scrim s WHERE s.challengerTeam.id = :teamId OR s.opponentTeam.id = :teamId ORDER BY s.scheduledTime DESC")
    List<Scrim> findByTeamId(@Param("teamId") Long teamId);
    
    // Find pending scrims for a team (both incoming and outgoing challenges)
    @Query("SELECT s FROM Scrim s WHERE (s.challengerTeam.id = :teamId OR s.opponentTeam.id = :teamId) AND s.status = 'PENDING' ORDER BY s.createdAt DESC")
    List<Scrim> findPendingScrimsForTeam(@Param("teamId") Long teamId);
    
    // Find upcoming scrims for a team
    @Query("SELECT s FROM Scrim s WHERE (s.challengerTeam.id = :teamId OR s.opponentTeam.id = :teamId) " +
           "AND s.status = 'ACCEPTED' AND s.scheduledTime > :now ORDER BY s.scheduledTime ASC")
    List<Scrim> findUpcomingScrimsForTeam(@Param("teamId") Long teamId, @Param("now") LocalDateTime now);
    
    // Find scrims by status
    List<Scrim> findByStatus(Scrim.ScrimStatus status);
    
    // Find scrims between two teams
    @Query("SELECT s FROM Scrim s WHERE " +
           "(s.challengerTeam.id = :team1Id AND s.opponentTeam.id = :team2Id) OR " +
           "(s.challengerTeam.id = :team2Id AND s.opponentTeam.id = :team1Id) " +
           "ORDER BY s.scheduledTime DESC")
    List<Scrim> findScrimsBetweenTeams(@Param("team1Id") Long team1Id, @Param("team2Id") Long team2Id);
    
    // Admin methods
    long countByStatus(String status);
}
