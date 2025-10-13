package com.valorantarena.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Service for interacting with Valorant API endpoints
 * Based on JVA (Java Valorant API) patterns
 */
@Service
@Slf4j
public class ValorantApiService {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    // API Endpoints based on JVA
    private static final String BASE_URL = "https://api.henrikdev.xyz/valorant";
    private static final String ACCOUNT_ENDPOINT = "/v1/account/{name}/{tag}";
    private static final String MMR_ENDPOINT = "/v2/mmr/{region}/{name}/{tag}";
    private static final String MATCHES_ENDPOINT = "/v3/matches/{region}/{name}/{tag}";
    private static final String LEADERBOARD_ENDPOINT = "/v2/leaderboard/{region}";
    private static final String STATUS_ENDPOINT = "/v1/status/{region}";
    private static final String CONTENT_ENDPOINT = "/v1/content";
    
    // Regions
    private static final List<String> REGIONS = Arrays.asList("eu", "na", "ap", "kr", "latam", "br");
    
    public ValorantApiService(@Value("${valorant.api.key:}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("User-Agent", "Valorant-Arena/1.0")
                .defaultHeader("Accept", "application/json")
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Get account information by Riot ID
     * @param name Player name
     * @param tag Player tag
     * @return Account information
     */
    public Mono<AccountInfo> getAccount(String name, String tag) {
        log.info("Fetching account info for {}#{}", name, tag);
        
        return webClient.get()
                .uri(ACCOUNT_ENDPOINT, name, tag)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseAccountInfo)
                .doOnError(error -> log.error("Error fetching account: {}", error.getMessage()));
    }
    
    /**
     * Get MMR/Rank information for a player
     * @param region Player region
     * @param name Player name
     * @param tag Player tag
     * @return MMR information
     */
    public Mono<MMRInfo> getMMR(String region, String name, String tag) {
        log.info("Fetching MMR for {}#{} in region {}", name, tag, region);
        
        return webClient.get()
                .uri(MMR_ENDPOINT, region, name, tag)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseMMRInfo)
                .doOnError(error -> log.error("Error fetching MMR: {}", error.getMessage()));
    }
    
    /**
     * Get MMR with auto-region detection
     * @param name Player name
     * @param tag Player tag
     * @return MMR information
     */
    public MMRInfo getMMRAutoRegion(String name, String tag) {
        log.info("Auto-detecting region for {}#{}", name, tag);
        
        for (String region : REGIONS) {
            try {
                MMRInfo mmr = getMMR(region, name, tag).block();
                if (mmr != null && mmr.getCurrentTier() != null) {
                    log.info("Found player in region: {}", region);
                    mmr.setRegion(region);
                    return mmr;
                }
            } catch (Exception e) {
                log.debug("Player not found in region {}", region);
            }
        }
        
        log.warn("Player {}#{} not found in any region", name, tag);
        return null;
    }
    
    /**
     * Get match history for a player
     * @param region Player region
     * @param name Player name
     * @param tag Player tag
     * @param mode Game mode filter (competitive, unrated, etc.)
     * @param size Number of matches to fetch
     * @return List of matches
     */
    public Mono<List<MatchInfo>> getMatches(String region, String name, String tag, String mode, int size) {
        log.info("Fetching {} {} matches for {}#{} in {}", size, mode, name, tag, region);
        
        String url = MATCHES_ENDPOINT + "?mode=" + mode + "&size=" + size;
        
        return webClient.get()
                .uri(url, region, name, tag)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseMatches)
                .doOnError(error -> log.error("Error fetching matches: {}", error.getMessage()));
    }
    
    /**
     * Get leaderboard for a region
     * @param region Region code
     * @param size Number of players to fetch
     * @return List of leaderboard entries
     */
    public Mono<List<LeaderboardEntry>> getLeaderboard(String region, int size) {
        log.info("Fetching top {} players from {} leaderboard", size, region);
        
        return webClient.get()
                .uri(LEADERBOARD_ENDPOINT + "?size=" + size, region)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseLeaderboard)
                .doOnError(error -> log.error("Error fetching leaderboard: {}", error.getMessage()));
    }
    
    /**
     * Get server status for a region
     * @param region Region code
     * @return Server status information
     */
    public Mono<ServerStatus> getServerStatus(String region) {
        log.info("Fetching server status for region {}", region);
        
        return webClient.get()
                .uri(STATUS_ENDPOINT, region)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseServerStatus)
                .doOnError(error -> log.error("Error fetching server status: {}", error.getMessage()));
    }
    
    /**
     * Get game content (agents, maps, weapons, etc.)
     * @return Game content information
     */
    public Mono<GameContent> getGameContent() {
        log.info("Fetching game content");
        
        return webClient.get()
                .uri(CONTENT_ENDPOINT)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parseGameContent)
                .doOnError(error -> log.error("Error fetching game content: {}", error.getMessage()));
    }
    
    // Parsing methods
    private AccountInfo parseAccountInfo(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            
            AccountInfo info = new AccountInfo();
            info.setPuuid(data.get("puuid").asText());
            info.setRegion(data.get("region").asText());
            info.setAccountLevel(data.get("account_level").asInt());
            info.setName(data.get("name").asText());
            info.setTag(data.get("tag").asText());
            
            // Parse card
            JsonNode card = data.get("card");
            if (card != null) {
                AccountInfo.PlayerCard playerCard = new AccountInfo.PlayerCard();
                playerCard.setSmall(card.get("small").asText());
                playerCard.setLarge(card.get("large").asText());
                playerCard.setWide(card.get("wide").asText());
                playerCard.setId(card.get("id").asText());
                info.setCard(playerCard);
            }
            
            return info;
        } catch (Exception e) {
            log.error("Error parsing account info: {}", e.getMessage());
            return null;
        }
    }
    
    private MMRInfo parseMMRInfo(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            
            MMRInfo info = new MMRInfo();
            info.setCurrentTier(data.get("currenttier").asInt());
            info.setCurrentTierPatched(data.get("currenttierpatched").asText());
            info.setRankedRating(data.get("ranking_in_tier").asInt());
            info.setMmrChangeToLastGame(data.get("mmr_change_to_last_game").asInt());
            info.setElo(data.get("elo").asInt());
            info.setGamesNeededForRating(data.get("games_needed_for_rating").asInt());
            
            return info;
        } catch (Exception e) {
            log.error("Error parsing MMR info: {}", e.getMessage());
            return null;
        }
    }
    
    private List<MatchInfo> parseMatches(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            
            List<MatchInfo> matches = new ArrayList<>();
            
            if (data != null && data.isArray()) {
                for (JsonNode matchNode : data) {
                    MatchInfo match = new MatchInfo();
                    
                    JsonNode metadata = matchNode.get("metadata");
                    match.setMatchId(metadata.get("matchid").asText());
                    match.setMap(metadata.get("map").asText());
                    match.setGameStart(metadata.get("game_start").asLong());
                    match.setGameLength(metadata.get("game_length").asInt());
                    match.setGameMode(metadata.get("mode").asText());
                    match.setRegion(metadata.get("region").asText());
                    
                    // Parse player stats
                    JsonNode players = matchNode.get("players").get("all_players");
                    for (JsonNode playerNode : players) {
                        if (playerNode.get("name").asText().equals(metadata.get("name").asText()) &&
                            playerNode.get("tag").asText().equals(metadata.get("tag").asText())) {
                            
                            MatchInfo.PlayerStats stats = new MatchInfo.PlayerStats();
                            
                            JsonNode statsNode = playerNode.get("stats");
                            stats.setKills(statsNode.get("kills").asInt());
                            stats.setDeaths(statsNode.get("deaths").asInt());
                            stats.setAssists(statsNode.get("assists").asInt());
                            stats.setScore(statsNode.get("score").asInt());
                            stats.setHeadshots(statsNode.get("headshots").asInt());
                            stats.setBodyshots(statsNode.get("bodyshots").asInt());
                            stats.setLegshots(statsNode.get("legshots").asInt());
                            
                            match.setPlayerStats(stats);
                            match.setAgent(playerNode.get("character").asText());
                            match.setTeam(playerNode.get("team").asText());
                            break;
                        }
                    }
                    
                    // Parse teams
                    JsonNode teams = matchNode.get("teams");
                    if (teams != null) {
                        JsonNode redTeam = teams.get("red");
                        JsonNode blueTeam = teams.get("blue");
                        
                        if (redTeam != null && blueTeam != null) {
                            boolean redWon = redTeam.get("has_won").asBoolean();
                            boolean blueWon = blueTeam.get("has_won").asBoolean();
                            
                            if (match.getTeam().equalsIgnoreCase("Red")) {
                                match.setWon(redWon);
                            } else {
                                match.setWon(blueWon);
                            }
                            
                            match.setRoundsWon(match.getTeam().equalsIgnoreCase("Red") ? 
                                redTeam.get("rounds_won").asInt() : blueTeam.get("rounds_won").asInt());
                            match.setRoundsLost(match.getTeam().equalsIgnoreCase("Red") ? 
                                redTeam.get("rounds_lost").asInt() : blueTeam.get("rounds_lost").asInt());
                        }
                    }
                    
                    matches.add(match);
                }
            }
            
            return matches;
        } catch (Exception e) {
            log.error("Error parsing matches: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private List<LeaderboardEntry> parseLeaderboard(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            
            List<LeaderboardEntry> entries = new ArrayList<>();
            
            if (data != null && data.isArray()) {
                for (JsonNode node : data) {
                    LeaderboardEntry entry = new LeaderboardEntry();
                    entry.setRank(node.get("leaderboardRank").asInt());
                    entry.setRankedRating(node.get("rankedRating").asInt());
                    entry.setNumberOfWins(node.get("numberOfWins").asInt());
                    entry.setGameName(node.get("gameName").asText());
                    entry.setTagLine(node.get("tagLine").asText());
                    entry.setPuuid(node.get("puuid").asText());
                    entries.add(entry);
                }
            }
            
            return entries;
        } catch (Exception e) {
            log.error("Error parsing leaderboard: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private ServerStatus parseServerStatus(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            
            ServerStatus status = new ServerStatus();
            status.setMaintenanceStatus(data.get("maintenances").size() > 0 ? "Under Maintenance" : "Operational");
            status.setIncidentStatus(data.get("incidents").size() > 0 ? "Incidents Reported" : "No Incidents");
            
            return status;
        } catch (Exception e) {
            log.error("Error parsing server status: {}", e.getMessage());
            return null;
        }
    }
    
    private GameContent parseGameContent(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode data = root.get("data");
            
            GameContent content = new GameContent();
            
            // Parse agents
            JsonNode agents = data.get("agents");
            List<String> agentNames = new ArrayList<>();
            if (agents != null && agents.isArray()) {
                for (JsonNode agent : agents) {
                    agentNames.add(agent.get("displayName").asText());
                }
            }
            content.setAgents(agentNames);
            
            // Parse maps
            JsonNode maps = data.get("maps");
            List<String> mapNames = new ArrayList<>();
            if (maps != null && maps.isArray()) {
                for (JsonNode map : maps) {
                    mapNames.add(map.get("displayName").asText());
                }
            }
            content.setMaps(mapNames);
            
            return content;
        } catch (Exception e) {
            log.error("Error parsing game content: {}", e.getMessage());
            return null;
        }
    }
    
    // DTOs for the service
    @lombok.Data
    public static class AccountInfo {
        private String puuid;
        private String region;
        private Integer accountLevel;
        private String name;
        private String tag;
        private PlayerCard card;
        
        @lombok.Data
        public static class PlayerCard {
            private String small;
            private String large;
            private String wide;
            private String id;
        }
    }
    
    @lombok.Data
    public static class MMRInfo {
        private Integer currentTier;
        private String currentTierPatched;
        private Integer rankedRating;
        private Integer mmrChangeToLastGame;
        private Integer elo;
        private Integer gamesNeededForRating;
        private String region;
    }
    
    @lombok.Data
    public static class MatchInfo {
        private String matchId;
        private String map;
        private Long gameStart;
        private Integer gameLength;
        private String gameMode;
        private String region;
        private String agent;
        private String team;
        private Boolean won;
        private Integer roundsWon;
        private Integer roundsLost;
        private PlayerStats playerStats;
        
        @lombok.Data
        public static class PlayerStats {
            private Integer kills;
            private Integer deaths;
            private Integer assists;
            private Integer score;
            private Integer headshots;
            private Integer bodyshots;
            private Integer legshots;
        }
    }
    
    @lombok.Data
    public static class LeaderboardEntry {
        private Integer rank;
        private Integer rankedRating;
        private Integer numberOfWins;
        private String gameName;
        private String tagLine;
        private String puuid;
    }
    
    @lombok.Data
    public static class ServerStatus {
        private String maintenanceStatus;
        private String incidentStatus;
    }
    
    @lombok.Data
    public static class GameContent {
        private List<String> agents;
        private List<String> maps;
        private List<String> weapons;
        private List<String> gameModes;
    }
}
