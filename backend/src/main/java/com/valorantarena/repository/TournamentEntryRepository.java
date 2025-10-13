package com.valorantarena.repository;

import com.valorantarena.model.TournamentEntry;
import com.valorantarena.model.TournamentEntry.EntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentEntryRepository extends JpaRepository<TournamentEntry, Long> {
    
    @Query("SELECT e FROM TournamentEntry e LEFT JOIN FETCH e.team WHERE e.tournament.id = :tournamentId")
    List<TournamentEntry> findByTournamentIdWithTeam(@Param("tournamentId") Long tournamentId);
    
    List<TournamentEntry> findByTournamentId(Long tournamentId);
    
    List<TournamentEntry> findByUserId(Long userId);
    
    List<TournamentEntry> findByTeamId(Long teamId);
    
    Optional<TournamentEntry> findByTournamentIdAndUserId(Long tournamentId, Long userId);
    
    Optional<TournamentEntry> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    
    @Query("SELECT e FROM TournamentEntry e WHERE e.tournament.id = :tournamentId AND e.status = :status")
    List<TournamentEntry> findByTournamentIdAndStatus(@Param("tournamentId") Long tournamentId, @Param("status") EntryStatus status);
    
    @Query("SELECT e FROM TournamentEntry e WHERE e.tournament.id = :tournamentId AND e.checkedIn = true")
    List<TournamentEntry> findCheckedInEntries(@Param("tournamentId") Long tournamentId);
    
    @Query("SELECT e FROM TournamentEntry e WHERE e.tournament.id = :tournamentId AND e.checkedIn = :checkedIn")
    List<TournamentEntry> findByTournamentIdAndCheckedIn(@Param("tournamentId") Long tournamentId, @Param("checkedIn") Boolean checkedIn);
    
    @Query("SELECT COUNT(e) FROM TournamentEntry e WHERE e.tournament.id = :tournamentId")
    Long countByTournamentId(@Param("tournamentId") Long tournamentId);
    
    boolean existsByTournamentIdAndUserId(Long tournamentId, Long userId);
    
    boolean existsByTournamentIdAndTeamId(Long tournamentId, Long teamId);
}
