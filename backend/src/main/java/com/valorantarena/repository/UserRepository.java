package com.valorantarena.repository;

import com.valorantarena.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByValorantId(String valorantId);
    Optional<User> findByDiscordId(String discordId);
    Optional<User> findByRiotPuuid(String riotPuuid);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByValorantId(String valorantId);
    @Query("SELECT u FROM User u ORDER BY u.elo DESC")
    Page<User> findTopPlayersByElo(Pageable pageable);
    
    // Find users by team
    List<User> findByTeamId(Long teamId);
    
    // Search users by username
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByUsername(String searchTerm);
    
    // Admin methods
    List<User> findByCreatedAtAfter(LocalDateTime dateTime);
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email, Pageable pageable);
}
