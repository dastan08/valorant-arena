package com.valorantarena.repository;

import com.valorantarena.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    Optional<Team> findByName(String name);
    
    Optional<Team> findByTag(String tag);
    
    Boolean existsByName(String name);
    
    Boolean existsByTag(String tag);
    
    // Find teams by captain
    List<Team> findByCaptainId(Long captainId);
    
    // Find active teams
    List<Team> findByIsActiveTrue();
    
    // Find top teams by ELO
    @Query("SELECT t FROM Team t WHERE t.isActive = true ORDER BY t.elo DESC")
    Page<Team> findTopTeamsByElo(Pageable pageable);
    
    // Search teams by name or tag
    @Query("SELECT t FROM Team t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.tag) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Team> searchByNameOrTag(String searchTerm);
    
    // Admin methods
    List<Team> findByCreatedAtAfter(LocalDateTime dateTime);
    Page<Team> findByNameContainingIgnoreCaseOrTagContainingIgnoreCase(String name, String tag, Pageable pageable);
}
