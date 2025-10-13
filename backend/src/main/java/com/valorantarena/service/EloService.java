package com.valorantarena.service;

import com.valorantarena.entity.User;
import com.valorantarena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EloService {
    
    private final UserRepository userRepository;
    
    // K-factor determines how much ELO changes per match
    private static final int K_FACTOR = 32;
    
    // Default starting ELO
    public static final int DEFAULT_ELO = 1000;
    
    /**
     * Calculate expected score for a player
     * Formula: 1 / (1 + 10^((opponentElo - playerElo) / 400))
     */
    public double calculateExpectedScore(int playerElo, int opponentElo) {
        return 1.0 / (1.0 + Math.pow(10.0, (opponentElo - playerElo) / 400.0));
    }
    
    /**
     * Calculate new ELO after a match
     * Formula: newElo = oldElo + K * (actualScore - expectedScore)
     * 
     * @param playerElo Current player ELO
     * @param opponentElo Opponent's ELO
     * @param won Whether the player won (1.0) or lost (0.0)
     * @return New ELO rating
     */
    public int calculateNewElo(int playerElo, int opponentElo, boolean won) {
        double expectedScore = calculateExpectedScore(playerElo, opponentElo);
        double actualScore = won ? 1.0 : 0.0;
        
        int eloChange = (int) Math.round(K_FACTOR * (actualScore - expectedScore));
        int newElo = playerElo + eloChange;
        
        // Ensure ELO doesn't go below 0
        return Math.max(0, newElo);
    }
    
    /**
     * Calculate new ELO for team-based match (average opponent ELO)
     */
    public int calculateNewEloTeamMatch(int playerElo, int averageOpponentElo, boolean won) {
        return calculateNewElo(playerElo, averageOpponentElo, won);
    }
    
    /**
     * Update user's ELO after a match
     */
    @Transactional
    public User updateUserElo(Long userId, int opponentElo, boolean won) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        int oldElo = user.getElo();
        int newElo = calculateNewElo(oldElo, opponentElo, won);
        int eloChange = newElo - oldElo;
        
        user.setElo(newElo);
        userRepository.save(user);
        
        log.info("Updated ELO for user {}: {} -> {} ({}{})", 
            user.getUsername(), oldElo, newElo, eloChange >= 0 ? "+" : "", eloChange);
        
        return user;
    }
    
    /**
     * Get ELO rank name based on ELO rating
     */
    public String getEloRank(int elo) {
        if (elo < 400) return "Bronze";
        if (elo < 800) return "Silver";
        if (elo < 1200) return "Gold";
        if (elo < 1600) return "Platinum";
        if (elo < 2000) return "Diamond";
        if (elo < 2400) return "Immortal";
        return "Radiant";
    }
    
    /**
     * Get ELO rank tier (1-3) within a rank
     */
    public int getEloTier(int elo) {
        int rankElo = elo % 400;
        if (rankElo < 133) return 1;
        if (rankElo < 267) return 2;
        return 3;
    }
    
    /**
     * Get full rank display (e.g., "Gold 2")
     */
    public String getFullRank(int elo) {
        return getEloRank(elo) + " " + getEloTier(elo);
    }
    
    /**
     * Calculate ELO difference between two players
     */
    public int getEloDifference(int elo1, int elo2) {
        return Math.abs(elo1 - elo2);
    }
    
    /**
     * Check if two players are within acceptable ELO range for matchmaking
     */
    public boolean isWithinMatchmakingRange(int elo1, int elo2, int maxDifference) {
        return getEloDifference(elo1, elo2) <= maxDifference;
    }
}
