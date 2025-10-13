package com.valorantarena.repository;

import com.valorantarena.entity.TournamentModerator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentModeratorRepository extends JpaRepository<TournamentModerator, Long> {
    
    List<TournamentModerator> findByTournamentId(Long tournamentId);
    
    List<TournamentModerator> findByUserId(Long userId);
    
    Optional<TournamentModerator> findByTournamentIdAndUserId(Long tournamentId, Long userId);
    
    boolean existsByTournamentIdAndUserId(Long tournamentId, Long userId);
    
    @Query("SELECT tm.tournament.id FROM TournamentModerator tm WHERE tm.user.id = :userId")
    List<Long> findTournamentIdsByUserId(@Param("userId") Long userId);
    
    void deleteByTournamentIdAndUserId(Long tournamentId, Long userId);
}
