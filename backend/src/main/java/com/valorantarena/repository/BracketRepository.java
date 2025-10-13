package com.valorantarena.repository;

import com.valorantarena.model.Bracket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BracketRepository extends JpaRepository<Bracket, Long> {

    List<Bracket> findByTournamentIdOrderByRoundNumberAscMatchNumberAsc(Long tournamentId);

    List<Bracket> findByTournamentIdAndBracketType(Long tournamentId, Bracket.BracketType bracketType);

    List<Bracket> findByTournamentIdAndRoundNumber(Long tournamentId, Integer roundNumber);

    @Query("SELECT b FROM Bracket b WHERE b.tournament.id = :tournamentId AND b.roundNumber = :roundNumber AND b.bracketType = :bracketType ORDER BY b.matchNumber")
    List<Bracket> findByTournamentIdAndRoundNumberAndBracketType(
        @Param("tournamentId") Long tournamentId,
        @Param("roundNumber") Integer roundNumber,
        @Param("bracketType") Bracket.BracketType bracketType
    );

    @Query("SELECT MAX(b.roundNumber) FROM Bracket b WHERE b.tournament.id = :tournamentId")
    Optional<Integer> findMaxRoundNumber(@Param("tournamentId") Long tournamentId);

    void deleteByTournamentId(Long tournamentId);
}
