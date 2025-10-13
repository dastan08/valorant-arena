# Valorant API Implementation Guide

## ✅ What's Been Implemented

### Backend (Java/Spring Boot)

#### 1. DTOs Created
- **PlayerStatsDto** - Player statistics and MMR data
- **MatchDto** - Match history with player performance
- **LeaderboardPlayerDto** - Leaderboard player information

#### 2. Service Methods (RiotApiService)
- `getPlayerStats(gameName, tagLine)` - Fetch player stats and rank
- `getMatchHistory(gameName, tagLine, region)` - Get recent matches
- `getLeaderboard(region)` - Get regional leaderboard

#### 3. API Endpoints (ValorantController)
- `GET /api/valorant/stats/{gameName}/{tagLine}` - Player stats
- `GET /api/valorant/matches/{gameName}/{tagLine}?region=na` - Match history
- `GET /api/valorant/leaderboard/{region}` - Leaderboard

## 🔌 Henrik's Valorant API Integration

### Base URL
```
https://api.henrikdev.xyz/valorant
```

### Key Endpoints Used

#### 1. Player MMR/Stats
```
GET /v2/mmr/{region}/{name}/{tag}
```
**Example:**
```
GET /v2/mmr/na/PlayerName/1234
```

**Response:**
```json
{
  "status": 200,
  "data": {
    "name": "PlayerName",
    "tag": "1234",
    "current_data": {
      "currenttier": 15,
      "currenttierpatched": "Platinum 3",
      "ranking_in_tier": 45,
      "mmr_change_to_last_game": 20,
      "elo": 1500,
      "games_needed_for_rating": 0,
      "old": false
    }
  }
}
```

#### 2. Match History
```
GET /v3/matches/{region}/{name}/{tag}
```
**Example:**
```
GET /v3/matches/na/PlayerName/1234
```

**Response:**
```json
{
  "status": 200,
  "data": [
    {
      "metadata": {
        "map": "Ascent",
        "game_start": 1234567890,
        "game_length": 2400,
        "mode": "Competitive",
        "rounds_played": 24
      },
      "players": {
        "all_players": [
          {
            "name": "PlayerName",
            "tag": "1234",
            "team": "Blue",
            "character": "Jett",
            "stats": {
              "kills": 25,
              "deaths": 15,
              "assists": 8
            }
          }
        ]
      },
      "teams": {
        "red": {
          "has_won": false,
          "rounds_won": 11,
          "rounds_lost": 13
        },
        "blue": {
          "has_won": true,
          "rounds_won": 13,
          "rounds_lost": 11
        }
      }
    }
  ]
}
```

#### 3. Leaderboard
```
GET /v2/leaderboard/{region}
```
**Example:**
```
GET /v2/leaderboard/na
```

**Response:**
```json
{
  "status": 200,
  "data": [
    {
      "leaderboardRank": 1,
      "rankedRating": 1500,
      "numberOfWins": 250,
      "gameName": "TopPlayer",
      "tagLine": "1234",
      "puuid": "abc123..."
    }
  ]
}
```

## 🚀 Testing the Endpoints

### 1. Test Player Stats
```bash
curl http://localhost:8080/api/valorant/stats/PlayerName/1234
```

### 2. Test Match History
```bash
curl http://localhost:8080/api/valorant/matches/PlayerName/1234?region=na
```

### 3. Test Leaderboard
```bash
curl http://localhost:8080/api/valorant/leaderboard/na
```

## 📝 Frontend Integration

### Angular Service Example

```typescript
// valorant.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ValorantService {
  private apiUrl = 'http://localhost:8080/api/valorant';

  constructor(private http: HttpClient) {}

  getPlayerStats(gameName: string, tagLine: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats/${gameName}/${tagLine}`);
  }

  getMatchHistory(gameName: string, tagLine: string, region: string = 'na'): Observable<any> {
    return this.http.get(`${this.apiUrl}/matches/${gameName}/${tagLine}?region=${region}`);
  }

  getLeaderboard(region: string = 'na'): Observable<any> {
    return this.http.get(`${this.apiUrl}/leaderboard/${region}`);
  }
}
```

### Profile Component Example

```typescript
// profile.component.ts
export class ProfileComponent implements OnInit {
  playerStats: any;
  matchHistory: any[] = [];
  
  constructor(private valorantService: ValorantService) {}

  ngOnInit() {
    this.loadPlayerStats();
    this.loadMatchHistory();
  }

  loadPlayerStats() {
    const riotId = this.currentUser.valorantId.split('#');
    this.valorantService.getPlayerStats(riotId[0], riotId[1])
      .subscribe(response => {
        if (response.success) {
          this.playerStats = response.data;
        }
      });
  }

  loadMatchHistory() {
    const riotId = this.currentUser.valorantId.split('#');
    this.valorantService.getMatchHistory(riotId[0], riotId[1], 'na')
      .subscribe(response => {
        if (response.success) {
          this.matchHistory = response.data;
        }
      });
  }
}
```

### Profile Template Example

```html
<!-- Player Stats Card -->
<div *ngIf="playerStats" class="bg-gray-800 rounded-lg p-6 mb-6">
  <h3 class="text-xl font-bold text-white mb-4">Player Stats</h3>
  <div class="grid grid-cols-2 gap-4">
    <div>
      <p class="text-gray-400">Current Rank</p>
      <p class="text-white font-bold">{{ playerStats.currentRankPatched }}</p>
    </div>
    <div>
      <p class="text-gray-400">MMR</p>
      <p class="text-white font-bold">{{ playerStats.elo }}</p>
    </div>
    <div>
      <p class="text-gray-400">Region</p>
      <p class="text-white font-bold">{{ playerStats.region | uppercase }}</p>
    </div>
    <div>
      <p class="text-gray-400">Last Game</p>
      <p class="text-white font-bold" [class.text-green-500]="playerStats.mmrChangeToLastGame > 0">
        {{ playerStats.mmrChangeToLastGame > 0 ? '+' : '' }}{{ playerStats.mmrChangeToLastGame }}
      </p>
    </div>
  </div>
</div>

<!-- Match History -->
<div class="bg-gray-800 rounded-lg p-6">
  <h3 class="text-xl font-bold text-white mb-4">Recent Matches</h3>
  <div *ngFor="let match of matchHistory" class="bg-gray-700 rounded p-4 mb-3">
    <div class="flex justify-between items-center">
      <div>
        <p class="text-white font-bold">{{ match.map }}</p>
        <p class="text-gray-400 text-sm">{{ match.mode }}</p>
      </div>
      <div class="text-right">
        <p class="text-white">{{ match.player.kills }}/{{ match.player.deaths }}/{{ match.player.assists }}</p>
        <p class="text-sm" [class.text-green-500]="match.player.team === 'Blue' && match.blue.hasWon"
                          [class.text-red-500]="!(match.player.team === 'Blue' && match.blue.hasWon)">
          {{ (match.player.team === 'Blue' && match.blue.hasWon) || (match.player.team === 'Red' && match.red.hasWon) ? 'Victory' : 'Defeat' }}
        </p>
      </div>
    </div>
  </div>
</div>
```

## 🎯 Available Regions

- `na` - North America
- `eu` - Europe
- `ap` - Asia Pacific
- `kr` - Korea
- `latam` - Latin America
- `br` - Brazil

## ⚠️ Rate Limits

Henrik's API has rate limits:
- **Basic (No Key)**: 30 requests/minute
- **Advanced Key**: 90 requests/minute
- **Production Key**: Custom limits

For production use, consider getting an API key from Henrik's Discord.

## 🔧 Next Steps

### 1. Enhance Data Parsing
Currently, the methods return basic data. You can enhance them to parse full JSON responses:

```java
// Example: Parse full MMR response
ObjectMapper mapper = new ObjectMapper();
JsonNode root = mapper.readTree(response);
JsonNode data = root.get("data");
JsonNode currentData = data.get("current_data");

stats.setCurrentRank(currentData.get("currenttier").asInt());
stats.setCurrentRankPatched(currentData.get("currenttierpatched").asText());
stats.setElo(currentData.get("elo").asInt());
// ... etc
```

### 2. Add Caching
Implement caching to reduce API calls:

```java
@Cacheable(value = "playerStats", key = "#gameName + '#' + #tagLine")
public PlayerStatsDto getPlayerStats(String gameName, String tagLine) {
    // ... existing code
}
```

### 3. Create Frontend Components
- **Profile Stats Component** - Display player stats
- **Match History Component** - Show recent matches
- **Leaderboard Page** - Display top players

### 4. Add Error Handling
Implement proper error handling for:
- Player not found
- Rate limit exceeded
- API unavailable

## 📚 Resources

- **Henrik's API Docs**: https://docs.henrikdev.xyz
- **API Status**: https://status.henrikdev.xyz
- **Discord**: Join Henrik's Discord for API key
- **GitHub**: https://github.com/Henrik-3/unofficial-valorant-api

## 🎨 UI Enhancements

Consider adding:
- **Rank Icons** - Visual rank badges
- **Agent Icons** - Character images in match history
- **Map Images** - Map thumbnails
- **Performance Charts** - K/D ratio graphs
- **Win Rate** - Calculate and display win percentage

## ✅ Summary

You now have:
1. ✅ Backend service methods for Valorant data
2. ✅ REST API endpoints
3. ✅ DTOs for data transfer
4. ✅ Henrik API integration
5. ✅ Ready for frontend implementation

The infrastructure is in place - you can now build beautiful UI components to display this data!
