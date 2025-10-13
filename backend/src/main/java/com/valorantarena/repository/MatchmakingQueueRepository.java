package com.valorantarena.repository;

import com.valorantarena.entity.MatchmakingQueue;
import com.valorantarena.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchmakingQueueRepository extends JpaRepository<MatchmakingQueue, Long> {
    
    Optional<MatchmakingQueue> findByUserAndStatus(User user, MatchmakingQueue.QueueStatus status);
    
    List<MatchmakingQueue> findByStatusAndRegion(MatchmakingQueue.QueueStatus status, String region);
    
    @Query("SELECT q FROM MatchmakingQueue q WHERE q.status = :status AND q.region = :region " +
           "AND q.elo BETWEEN :minElo AND :maxElo ORDER BY q.queuedAt ASC")
    List<MatchmakingQueue> findMatchingPlayers(
        @Param("status") MatchmakingQueue.QueueStatus status,
        @Param("region") String region,
        @Param("minElo") int minElo,
        @Param("maxElo") int maxElo
    );
    
    @Query("SELECT COUNT(q) FROM MatchmakingQueue q WHERE q.status = :status AND q.region = :region")
    long countByStatusAndRegion(
        @Param("status") MatchmakingQueue.QueueStatus status,
        @Param("region") String region
    );
    
    List<MatchmakingQueue> findByQueuedAtBeforeAndStatus(
        LocalDateTime time,
        MatchmakingQueue.QueueStatus status
    );
    
    void deleteByUser(User user);
}
