package com.valorantarena.service;

import com.valorantarena.dto.MatchmakingResponse;
import com.valorantarena.entity.MatchmakingQueue;
import com.valorantarena.entity.User;
import com.valorantarena.repository.MatchmakingQueueRepository;
import com.valorantarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchmakingService {
    
    private final MatchmakingQueueRepository queueRepository;
    private final UserRepository userRepository;
    private final EloService eloService;
    
    // Matchmaking parameters
    private static final int INITIAL_ELO_RANGE = 100; // Start with ±100 ELO
    private static final int MAX_ELO_RANGE = 500; // Max ±500 ELO after waiting
    private static final int ELO_RANGE_INCREASE_PER_MINUTE = 50; // Increase range by 50 every minute
    private static final int PLAYERS_PER_MATCH = 10; // 5v5
    private static final int QUEUE_TIMEOUT_MINUTES = 10; // Remove from queue after 10 minutes
    
    /**
     * Add player to matchmaking queue
     */
    @Transactional
    public MatchmakingResponse joinQueue(Long userId, String region) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if user is already in queue
        Optional<MatchmakingQueue> existing = queueRepository.findByUserAndStatus(
            user, MatchmakingQueue.QueueStatus.SEARCHING
        );
        
        if (existing.isPresent()) {
            log.info("User {} already in queue", user.getUsername());
            return buildQueueResponse(existing.get());
        }
        
        // Create new queue entry
        MatchmakingQueue queueEntry = new MatchmakingQueue();
        queueEntry.setUser(user);
        queueEntry.setElo(user.getElo());
        queueEntry.setRegion(region);
        queueEntry.setStatus(MatchmakingQueue.QueueStatus.SEARCHING);
        queueEntry.setQueuedAt(LocalDateTime.now());
        
        queueEntry = queueRepository.save(queueEntry);
        
        log.info("User {} joined matchmaking queue (ELO: {}, Region: {})", 
            user.getUsername(), user.getElo(), region);
        
        // Try to find match immediately
        tryFindMatch(queueEntry);
        
        return buildQueueResponse(queueEntry);
    }
    
    /**
     * Leave matchmaking queue
     */
    @Transactional
    public void leaveQueue(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        queueRepository.deleteByUser(user);
        log.info("User {} left matchmaking queue", user.getUsername());
    }
    
    /**
     * Get current queue status
     */
    public MatchmakingResponse getQueueStatus(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Optional<MatchmakingQueue> queueEntry = queueRepository.findByUserAndStatus(
            user, MatchmakingQueue.QueueStatus.SEARCHING
        );
        
        if (queueEntry.isEmpty()) {
            throw new RuntimeException("User not in queue");
        }
        
        return buildQueueResponse(queueEntry.get());
    }
    
    /**
     * Try to find a match for a player
     */
    @Transactional
    public void tryFindMatch(MatchmakingQueue queueEntry) {
        long waitTime = ChronoUnit.SECONDS.between(queueEntry.getQueuedAt(), LocalDateTime.now());
        int eloRange = calculateEloRange(waitTime);
        
        int minElo = queueEntry.getElo() - eloRange;
        int maxElo = queueEntry.getElo() + eloRange;
        
        log.debug("Searching for match: ELO range {}-{} for user {}", 
            minElo, maxElo, queueEntry.getUser().getUsername());
        
        // Find players in similar ELO range
        List<MatchmakingQueue> potentialMatches = queueRepository.findMatchingPlayers(
            MatchmakingQueue.QueueStatus.SEARCHING,
            queueEntry.getRegion(),
            minElo,
            maxElo
        );
        
        // Need 10 players for a 5v5 match
        if (potentialMatches.size() >= PLAYERS_PER_MATCH) {
            createMatch(potentialMatches.subList(0, PLAYERS_PER_MATCH));
        }
    }
    
    /**
     * Create a match from matched players
     */
    @Transactional
    protected void createMatch(List<MatchmakingQueue> players) {
        log.info("Creating match with {} players", players.size());
        
        // Sort players by ELO for balanced teams
        players.sort((a, b) -> Integer.compare(b.getElo(), a.getElo()));
        
        // Create balanced teams (snake draft: 1,4,5,8,9 vs 2,3,6,7,10)
        List<MatchmakingQueue> team1 = new ArrayList<>();
        List<MatchmakingQueue> team2 = new ArrayList<>();
        
        for (int i = 0; i < players.size(); i++) {
            if (i % 4 < 2) {
                if (i % 2 == 0) team1.add(players.get(i));
                else team2.add(players.get(i));
            } else {
                if (i % 2 == 0) team2.add(players.get(i));
                else team1.add(players.get(i));
            }
        }
        
        // Calculate average ELO
        int avgElo = (int) players.stream()
            .mapToInt(MatchmakingQueue::getElo)
            .average()
            .orElse(1000);
        
        // Update queue entries to MATCH_FOUND
        for (MatchmakingQueue player : players) {
            player.setStatus(MatchmakingQueue.QueueStatus.MATCH_FOUND);
            player.setMatchFoundAt(LocalDateTime.now());
            // In a real implementation, you'd create a Match entity and set matchId
            queueRepository.save(player);
        }
        
        log.info("Match created! Team 1 avg ELO: {}, Team 2 avg ELO: {}", 
            team1.stream().mapToInt(MatchmakingQueue::getElo).average().orElse(0),
            team2.stream().mapToInt(MatchmakingQueue::getElo).average().orElse(0));
    }
    
    /**
     * Calculate ELO range based on wait time
     */
    private int calculateEloRange(long waitTimeSeconds) {
        long waitMinutes = waitTimeSeconds / 60;
        int additionalRange = (int) (waitMinutes * ELO_RANGE_INCREASE_PER_MINUTE);
        return Math.min(INITIAL_ELO_RANGE + additionalRange, MAX_ELO_RANGE);
    }
    
    /**
     * Build response DTO
     */
    private MatchmakingResponse buildQueueResponse(MatchmakingQueue queueEntry) {
        MatchmakingResponse response = new MatchmakingResponse();
        response.setQueueId(queueEntry.getId());
        response.setStatus(queueEntry.getStatus().name());
        response.setPlayerElo(queueEntry.getElo());
        response.setPlayerRank(eloService.getFullRank(queueEntry.getElo()));
        response.setQueuedAt(queueEntry.getQueuedAt());
        
        long waitTime = ChronoUnit.SECONDS.between(queueEntry.getQueuedAt(), LocalDateTime.now());
        response.setEstimatedWaitTime(estimateWaitTime(queueEntry.getRegion(), queueEntry.getElo()));
        
        return response;
    }
    
    /**
     * Estimate wait time based on queue size
     */
    private long estimateWaitTime(String region, int elo) {
        long playersInQueue = queueRepository.countByStatusAndRegion(
            MatchmakingQueue.QueueStatus.SEARCHING, region
        );
        
        // Simple estimation: more players = shorter wait
        if (playersInQueue >= PLAYERS_PER_MATCH) return 10; // ~10 seconds
        if (playersInQueue >= PLAYERS_PER_MATCH / 2) return 30; // ~30 seconds
        return 60; // ~1 minute
    }
    
    /**
     * Scheduled task to process matchmaking queue
     * Runs every 5 seconds
     */
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processQueue() {
        List<MatchmakingQueue> searchingPlayers = queueRepository.findByStatusAndRegion(
            MatchmakingQueue.QueueStatus.SEARCHING, "NA" // Process each region
        );
        
        if (searchingPlayers.size() >= PLAYERS_PER_MATCH) {
            log.debug("Processing queue: {} players searching", searchingPlayers.size());
            
            for (MatchmakingQueue player : searchingPlayers) {
                if (player.getStatus() == MatchmakingQueue.QueueStatus.SEARCHING) {
                    tryFindMatch(player);
                }
            }
        }
    }
    
    /**
     * Scheduled task to clean up expired queue entries
     * Runs every minute
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredQueues() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(QUEUE_TIMEOUT_MINUTES);
        List<MatchmakingQueue> expiredQueues = queueRepository.findByQueuedAtBeforeAndStatus(
            expiryTime, MatchmakingQueue.QueueStatus.SEARCHING
        );
        
        if (!expiredQueues.isEmpty()) {
            log.info("Removing {} expired queue entries", expiredQueues.size());
            for (MatchmakingQueue queue : expiredQueues) {
                queue.setStatus(MatchmakingQueue.QueueStatus.EXPIRED);
                queueRepository.save(queue);
            }
        }
    }
    
    /**
     * Get queue statistics
     */
    public MatchmakingStats getQueueStats(String region) {
        long searching = queueRepository.countByStatusAndRegion(
            MatchmakingQueue.QueueStatus.SEARCHING, region
        );
        
        return new MatchmakingStats(searching, estimateWaitTime(region, 1000));
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MatchmakingStats {
        private long playersInQueue;
        private long estimatedWaitTime;
    }
}
