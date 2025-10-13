package com.valorantarena.service;

import com.valorantarena.dto.*;
import com.valorantarena.enums.ValorantRank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class RiotApiService {
    
    private final WebClient riotApiClient;
    private final WebClient accountApiClient;
    private final WebClient henrikApiClient;
    private final String apiKey;
    
    public RiotApiService(@Value("${riot.api.key:}") String apiKey,
                          @Value("${riot.api.base-url:https://api.riotgames.com}") String baseUrl,
                          @Value("${riot.api.account-url:https://americas.api.riotgames.com}") String accountUrl) {
        this.apiKey = apiKey;
        
        this.riotApiClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
        
        this.accountApiClient = WebClient.builder()
                .baseUrl(accountUrl)
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
        
        // Henrik's Unofficial Valorant API - No API key needed for basic endpoints
        this.henrikApiClient = WebClient.builder()
                .baseUrl("https://api.henrikdev.xyz/valorant")
                .defaultHeader("User-Agent", "Valorant-Arena-App")
                .build();
    }
    
    /**
     * Verify Riot account format (simple validation like Procity.gg)
     * No API call - just validates format and accepts the Riot ID
     * @param gameName The Riot ID game name (e.g., "PlayerName")
     * @param tagLine The Riot ID tag (e.g., "1234")
     * @return RiotAccountDto with the provided information
     */
    public RiotAccountDto verifyAccount(String gameName, String tagLine) {
        log.info("Accepting Riot account: {}#{} (format validation only)", gameName, tagLine);
        
        // Simple format validation
        if (gameName == null || gameName.trim().isEmpty()) {
            log.warn("Invalid game name: empty or null");
            return null;
        }
        
        if (tagLine == null || tagLine.trim().isEmpty()) {
            log.warn("Invalid tag line: empty or null");
            return null;
        }
        
        // Tag line should be alphanumeric and 3-5 characters
        if (!tagLine.matches("^[a-zA-Z0-9]{3,5}$")) {
            log.warn("Invalid tag line format: {}", tagLine);
            return null;
        }
        
        // Create RiotAccountDto - accept without API verification
        RiotAccountDto account = new RiotAccountDto();
        account.setGameName(gameName);
        account.setTagLine(tagLine);
        account.setRegion("unknown"); // Will be determined when fetching rank
        account.setPuuid(gameName + "#" + tagLine); // Use Riot ID as identifier
        
        log.info("Riot account accepted: {}#{}", gameName, tagLine);
        return account;
    }
    
    /**
     * Get account by PUUID
     * @param puuid The player's PUUID
     * @return RiotAccountDto if found
     */
    public RiotAccountDto getAccountByPuuid(String puuid) {
        try {
            log.info("Fetching Riot account by PUUID: {}", puuid);
            
            return accountApiClient
                    .get()
                    .uri("/riot/account/v1/accounts/by-puuid/{puuid}", puuid)
                    .retrieve()
                    .bodyToMono(RiotAccountDto.class)
                    .block();
                    
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Riot account not found for PUUID: {}", puuid);
            return null;
        } catch (Exception e) {
            log.error("Error fetching Riot account by PUUID", e);
            throw new RuntimeException("Failed to fetch Riot account: " + e.getMessage());
        }
    }
    
    /**
     * Get player's current Valorant rank (auto-detects region)
     * @param gameName The Riot ID game name
     * @param tagLine The Riot ID tag
     * @return ValorantRank enum
     */
    public ValorantRank getPlayerRank(String gameName, String tagLine) {
        // Try multiple regions to find the player's rank
        String[] valorantRegions = {"na", "eu", "ap", "kr", "latam", "br"};
        
        for (String region : valorantRegions) {
            try {
                log.info("Fetching rank for: {}#{} in region {}", gameName, tagLine, region);
                
                ValorantMMRDto response = henrikApiClient
                        .get()
                        .uri("/v2/mmr/{region}/{name}/{tag}", region, gameName, tagLine)
                        .retrieve()
                        .bodyToMono(ValorantMMRDto.class)
                        .block();
                
                if (response != null && response.getData() != null && response.getData().getCurrentTier() != null) {
                    ValorantRank rank = mapTierToRank(response.getData().getCurrentTier());
                    log.info("Fetched rank: {} (tier: {}) from region {}", rank, response.getData().getCurrentTier(), region);
                    return rank;
                }
                
            } catch (WebClientResponseException.NotFound e) {
                log.debug("No rank data in region {}", region);
                // Continue to next region
            } catch (Exception e) {
                log.debug("Error fetching rank from region {}: {}", region, e.getMessage());
                // Continue to next region
            }
        }
        
        log.warn("No rank data found for {}#{} in any region, defaulting to IRON", gameName, tagLine);
        return ValorantRank.IRON; // Default
    }
    
    /**
     * Map Valorant tier number to ValorantRank enum
     * Tier mapping:
     * 3-5: Iron, 6-8: Bronze, 9-11: Silver, 12-14: Gold,
     * 15-17: Platinum, 18-20: Diamond, 21-23: Immortal, 24+: Radiant
     */
    private ValorantRank mapTierToRank(Integer tier) {
        if (tier == null || tier < 3) return ValorantRank.IRON;
        
        if (tier >= 3 && tier <= 5) return ValorantRank.IRON;
        if (tier >= 6 && tier <= 8) return ValorantRank.BRONZE;
        if (tier >= 9 && tier <= 11) return ValorantRank.SILVER;
        if (tier >= 12 && tier <= 14) return ValorantRank.GOLD;
        if (tier >= 15 && tier <= 17) return ValorantRank.PLATINUM;
        if (tier >= 18 && tier <= 20) return ValorantRank.DIAMOND;
        if (tier >= 21 && tier <= 23) return ValorantRank.IMMORTAL;
        if (tier >= 24) return ValorantRank.RADIANT;
        
        return ValorantRank.IRON;
    }
    
    /**
     * Get comprehensive player stats using the new ValorantApiService
     * Includes MMR, rank, match stats, and agent stats
     */
    public PlayerStatsDto getPlayerStats(String gameName, String tagLine) {
        try {
            log.info("Fetching comprehensive stats for {}#{}", gameName, tagLine);
            
            // Use the new ValorantApiService
            ValorantApiService valorantApi = new ValorantApiService("");
            
            // Get account info first
            ValorantApiService.AccountInfo accountInfo = valorantApi.getAccount(gameName, tagLine).block();
            
            if (accountInfo == null) {
                log.warn("Account not found for {}#{}", gameName, tagLine);
                return null;
            }
            
            // Get MMR with auto-region detection
            ValorantApiService.MMRInfo mmrInfo = valorantApi.getMMRAutoRegion(gameName, tagLine);
            
            if (mmrInfo == null) {
                log.warn("MMR info not found for {}#{}", gameName, tagLine);
                return null;
            }
            
            // Build PlayerStatsDto
            PlayerStatsDto stats = new PlayerStatsDto();
            stats.setName(gameName);
            stats.setTag(tagLine);
            stats.setRegion(mmrInfo.getRegion());
            
            // MMR and Rank data
            stats.setCurrentTier(mmrInfo.getCurrentTier());
            stats.setCurrentTierPatched(mmrInfo.getCurrentTierPatched());
            stats.setRankedRating(mmrInfo.getRankedRating());
            stats.setElo(mmrInfo.getElo());
            stats.setMmrChangeToLastGame(mmrInfo.getMmrChangeToLastGame());
            stats.setGamesNeededForRating(mmrInfo.getGamesNeededForRating());
            
            // Account data
            stats.setAccountLevel(accountInfo.getAccountLevel());
            
            // Convert PlayerCard to CardDto
            if (accountInfo.getCard() != null) {
                PlayerStatsDto.CardDto card = new PlayerStatsDto.CardDto();
                card.setSmall(accountInfo.getCard().getSmall());
                card.setLarge(accountInfo.getCard().getLarge());
                card.setWide(accountInfo.getCard().getWide());
                card.setId(accountInfo.getCard().getId());
                stats.setCard(card);
            }
            
            log.info("Successfully fetched stats for {}#{} in region {}", gameName, tagLine, mmrInfo.getRegion());
            return stats;
            
        } catch (Exception e) {
            log.error("Error fetching player stats for {}#{}: {}", gameName, tagLine, e.getMessage());
            return null;
        }
    }
    
    /**
     * Get match history from Henrik API
     */
    public java.util.List<MatchDto> getMatchHistory(String gameName, String tagLine, String region) {
        try {
            log.info("Fetching match history for {}#{} in region {}", gameName, tagLine, region);
            
            // Henrik API endpoint: /v3/matches/{region}/{name}/{tag}
            String response = henrikApiClient
                    .get()
                    .uri("/v3/matches/{region}/{name}/{tag}", region, gameName, tagLine)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // Parse response and return match list
            // For now returning empty list, you can implement full parsing
            return new java.util.ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error fetching match history", e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get leaderboard from Henrik API
     */
    public java.util.List<LeaderboardPlayerDto> getLeaderboard(String region) {
        try {
            log.info("Fetching leaderboard for region {}", region);
            
            // Henrik API endpoint: /v2/leaderboard/{region}
            String response = henrikApiClient
                    .get()
                    .uri("/v2/leaderboard/{region}", region)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // Parse response and return leaderboard
            // For now returning empty list, you can implement full parsing
            return new java.util.ArrayList<>();
            
        } catch (Exception e) {
            log.error("Error fetching leaderboard", e);
            return new java.util.ArrayList<>();
        }
    }
}
