package com.valorantarena.repository;

import com.valorantarena.model.Tournament;
import com.valorantarena.model.Tournament.TournamentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    
    Page<Tournament> findByStatus(TournamentStatus status, Pageable pageable);
    
    Page<Tournament> findByOrganizerId(Long organizerId, Pageable pageable);
    
    List<Tournament> findByOrganizerId(Long organizerId);
    
    @Query("SELECT t FROM Tournament t WHERE t.tournamentStart > :now AND t.status = 'OPEN' ORDER BY t.tournamentStart ASC")
    List<Tournament> findUpcomingTournaments(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Tournament t WHERE t.status = 'ONGOING' ORDER BY t.tournamentStart DESC")
    List<Tournament> findActiveTournaments();
    
    @Query("SELECT t FROM Tournament t WHERE t.registrationEnd > :now AND t.status = 'OPEN'")
    List<Tournament> findTournamentsWithOpenRegistration(@Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Tournament t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Tournament> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT t FROM Tournament t WHERE t.isFeatured = true AND t.status IN ('OPEN', 'ONGOING') ORDER BY t.tournamentStart ASC")
    List<Tournament> findFeaturedTournaments();
    
    @Query("SELECT t FROM Tournament t WHERE t.status = :status AND (:region IS NULL OR t.region = :region)")
    Page<Tournament> findByStatusAndRegion(@Param("status") TournamentStatus status, @Param("region") String region, Pageable pageable);
}
