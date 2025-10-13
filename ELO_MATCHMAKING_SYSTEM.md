# ELO & Matchmaking System Documentation

## 📊 ELO Rating System

### **Overview**
The VALORANT Arena uses an ELO rating system to match players of similar skill levels. ELO is a mathematical system for calculating relative skill levels.

### **Default ELO**
- **Starting ELO:** 1000
- **Minimum ELO:** 0 (cannot go negative)
- **Maximum ELO:** Unlimited

### **ELO Ranks**

| ELO Range | Rank | Tier |
|-----------|------|------|
| 0 - 399 | Bronze | 1-3 |
| 400 - 799 | Silver | 1-3 |
| 800 - 1199 | Gold | 1-3 |
| 1200 - 1599 | Platinum | 1-3 |
| 1600 - 1999 | Diamond | 1-3 |
| 2000 - 2399 | Immortal | 1-3 |
| 2400+ | Radiant | 1-3 |

### **ELO Calculation**

The system uses the standard ELO formula:

```
Expected Score = 1 / (1 + 10^((OpponentELO - PlayerELO) / 400))
ELO Change = K-Factor × (Actual Score - Expected Score)
New ELO = Old ELO + ELO Change
```

**Parameters:**
- **K-Factor:** 32 (determines how much ELO changes per match)
- **Actual Score:** 1.0 for win, 0.0 for loss
- **Expected Score:** Probability of winning based on ELO difference

**Examples:**

1. **Equal Skill Match (1000 vs 1000):**
   - Expected Score: 0.5 (50% chance to win)
   - Win: +16 ELO
   - Loss: -16 ELO

2. **Underdog Win (900 vs 1100):**
   - Expected Score: 0.24 (24% chance to win)
   - Win: +24 ELO (bigger reward!)
   - Loss: -8 ELO (smaller penalty)

3. **Favorite Win (1100 vs 900):**
   - Expected Score: 0.76 (76% chance to win)
   - Win: +8 ELO (smaller reward)
   - Loss: -24 ELO (bigger penalty!)

## 🎮 Matchmaking System

### **How It Works**

1. **Player Joins Queue**
   - Player selects region (NA, EU, ASIA)
   - System records player's current ELO
   - Player enters "SEARCHING" status

2. **ELO Range Expansion**
   - **Initial Range:** ±100 ELO
   - **Expansion Rate:** +50 ELO per minute
   - **Maximum Range:** ±500 ELO
   - **Purpose:** Balance queue time vs match quality

3. **Match Formation**
   - System finds 10 players in similar ELO range
   - Players sorted by ELO for team balance
   - Teams created using snake draft algorithm
   - Match status updated to "MATCH_FOUND"

4. **Team Balancing**
   - Snake draft: 1,4,5,8,9 vs 2,3,6,7,10
   - Ensures both teams have similar average ELO
   - Prevents one-sided matches

### **Matchmaking Parameters**

```java
INITIAL_ELO_RANGE = 100         // Start with ±100 ELO
MAX_ELO_RANGE = 500             // Max ±500 ELO after waiting
ELO_RANGE_INCREASE = 50/min     // Expand by 50 every minute
PLAYERS_PER_MATCH = 10          // 5v5 matches
QUEUE_TIMEOUT = 10 minutes      // Auto-remove after 10 min
PROCESS_INTERVAL = 5 seconds    // Check queue every 5 sec
```

### **Queue Statuses**

- **SEARCHING** - Actively looking for match
- **MATCH_FOUND** - Match created, waiting for acceptance
- **ACCEPTED** - Player accepted the match
- **DECLINED** - Player declined the match
- **EXPIRED** - Queue entry timed out

### **Wait Time Estimation**

```
10+ players in queue → ~10 seconds
5-9 players in queue → ~30 seconds
<5 players in queue → ~60 seconds
```

## 🔧 Backend Implementation

### **Services**

1. **EloService**
   - Calculate expected scores
   - Calculate new ELO ratings
   - Get rank names and tiers
   - Update user ELO after matches

2. **MatchmakingService**
   - Join/leave queue
   - Find matches
   - Create balanced teams
   - Process queue (scheduled)
   - Clean up expired entries

### **Entities**

1. **User**
   - `elo: Integer` - Current ELO rating (default: 1000)

2. **MatchmakingQueue**
   - `user: User` - Player in queue
   - `elo: Integer` - Player's ELO at queue time
   - `region: String` - Player's region
   - `status: QueueStatus` - Current status
   - `queuedAt: LocalDateTime` - When joined queue
   - `matchFoundAt: LocalDateTime` - When match was found

### **API Endpoints**

```
POST   /api/matchmaking/join     - Join matchmaking queue
POST   /api/matchmaking/leave    - Leave matchmaking queue
GET    /api/matchmaking/status   - Get current queue status
GET    /api/matchmaking/stats    - Get queue statistics
```

### **Scheduled Tasks**

1. **Process Queue** (every 5 seconds)
   - Check for potential matches
   - Create matches when 10 players found
   - Update queue statuses

2. **Cleanup Expired** (every 1 minute)
   - Remove players who waited >10 minutes
   - Mark entries as EXPIRED

## 🎨 Frontend Implementation

### **Matchmaking Service**

Create: `frontend/src/app/services/matchmaking.service.ts`

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval } from 'rxjs';
import { switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class MatchmakingService {
  private apiUrl = 'http://localhost:8080/api/matchmaking';

  constructor(private http: HttpClient) {}

  joinQueue(region: string = 'NA'): Observable<any> {
    return this.http.post(`${this.apiUrl}/join`, { region });
  }

  leaveQueue(): Observable<any> {
    return this.http.post(`${this.apiUrl}/leave`, {});
  }

  getQueueStatus(): Observable<any> {
    return this.http.get(`${this.apiUrl}/status`);
  }

  getQueueStats(region: string = 'NA'): Observable<any> {
    return this.http.get(`${this.apiUrl}/stats`, { params: { region } });
  }

  // Poll queue status every 2 seconds
  pollQueueStatus(): Observable<any> {
    return interval(2000).pipe(
      switchMap(() => this.getQueueStatus())
    );
  }
}
```

### **Matchmaking Component**

Create: `frontend/src/app/pages/matchmaking/matchmaking.component.ts`

```typescript
import { Component, OnInit, OnDestroy } from '@angular/core';
import { MatchmakingService } from '../../services/matchmaking.service';
import { Subscription } from 'rxjs';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-matchmaking',
  templateUrl: './matchmaking.component.html',
  styleUrls: ['./matchmaking.component.css']
})
export class MatchmakingComponent implements OnInit, OnDestroy {
  inQueue = false;
  queueStatus: any = null;
  queueStats: any = null;
  private statusSubscription?: Subscription;

  constructor(
    private matchmakingService: MatchmakingService,
    private toastr: ToastrService
  ) {}

  ngOnInit(): void {
    this.loadQueueStats();
  }

  ngOnDestroy(): void {
    if (this.inQueue) {
      this.leaveQueue();
    }
    this.stopPolling();
  }

  joinQueue(): void {
    this.matchmakingService.joinQueue('NA').subscribe({
      next: (response) => {
        if (response.success) {
          this.inQueue = true;
          this.queueStatus = response.data;
          this.toastr.success('Joined matchmaking queue!');
          this.startPolling();
        }
      },
      error: (error) => {
        this.toastr.error('Failed to join queue', 'Error');
      }
    });
  }

  leaveQueue(): void {
    this.matchmakingService.leaveQueue().subscribe({
      next: (response) => {
        this.inQueue = false;
        this.queueStatus = null;
        this.stopPolling();
        this.toastr.info('Left matchmaking queue');
      },
      error: (error) => {
        this.toastr.error('Failed to leave queue', 'Error');
      }
    });
  }

  private startPolling(): void {
    this.statusSubscription = this.matchmakingService.pollQueueStatus().subscribe({
      next: (response) => {
        if (response.success) {
          this.queueStatus = response.data;
          
          if (this.queueStatus.status === 'MATCH_FOUND') {
            this.toastr.success('Match found!', 'Success');
            this.stopPolling();
          }
        }
      },
      error: () => {
        // Player likely not in queue anymore
        this.inQueue = false;
        this.stopPolling();
      }
    });
  }

  private stopPolling(): void {
    if (this.statusSubscription) {
      this.statusSubscription.unsubscribe();
    }
  }

  private loadQueueStats(): void {
    this.matchmakingService.getQueueStats('NA').subscribe({
      next: (response) => {
        if (response.success) {
          this.queueStats = response.data;
        }
      }
    });
  }
}
```

### **Matchmaking UI Template**

Create: `frontend/src/app/pages/matchmaking/matchmaking.component.html`

```html
<div class="min-h-screen bg-valorant-dark py-12">
  <div class="max-w-4xl mx-auto px-4">
    <!-- Header -->
    <div class="text-center mb-8">
      <h1 class="text-4xl font-bold text-white mb-2">Matchmaking</h1>
      <p class="text-gray-400">Find players of similar skill level</p>
    </div>

    <!-- Queue Stats -->
    <div *ngIf="queueStats" class="bg-gray-800 rounded-lg p-6 mb-6">
      <div class="grid grid-cols-2 gap-4">
        <div class="text-center">
          <p class="text-gray-400 text-sm">Players in Queue</p>
          <p class="text-3xl font-bold text-valorant-red">{{ queueStats.playersInQueue }}</p>
        </div>
        <div class="text-center">
          <p class="text-gray-400 text-sm">Estimated Wait</p>
          <p class="text-3xl font-bold text-white">{{ queueStats.estimatedWaitTime }}s</p>
        </div>
      </div>
    </div>

    <!-- Queue Button -->
    <div *ngIf="!inQueue" class="text-center">
      <button (click)="joinQueue()" 
              class="bg-gradient-to-r from-valorant-red to-pink-600 hover:from-red-600 hover:to-pink-700 text-white px-12 py-4 text-xl font-bold uppercase tracking-wider transition-all transform hover:scale-105 rounded-lg">
        Find Match
      </button>
    </div>

    <!-- In Queue -->
    <div *ngIf="inQueue && queueStatus" class="bg-gray-800 rounded-lg p-8">
      <div class="text-center mb-6">
        <div class="animate-spin rounded-full h-16 w-16 border-b-2 border-valorant-red mx-auto mb-4"></div>
        <h2 class="text-2xl font-bold text-white mb-2">Searching for Match...</h2>
        <p class="text-gray-400">{{ queueStatus.playerRank }} ({{ queueStatus.playerElo }} ELO)</p>
      </div>

      <div class="space-y-4">
        <div class="flex justify-between text-sm">
          <span class="text-gray-400">Time in Queue:</span>
          <span class="text-white font-medium">{{ getWaitTime() }}</span>
        </div>
        <div class="flex justify-between text-sm">
          <span class="text-gray-400">Estimated Wait:</span>
          <span class="text-white font-medium">{{ queueStatus.estimatedWaitTime }}s</span>
        </div>
      </div>

      <button (click)="leaveQueue()" 
              class="w-full mt-6 bg-gray-700 hover:bg-gray-600 text-white px-6 py-3 rounded-lg transition-colors">
        Cancel Search
      </button>
    </div>
  </div>
</div>
```

## 📈 ELO Progression Examples

### **New Player Journey**

```
Match 1: 1000 ELO → Win vs 1000 → 1016 ELO (+16)
Match 2: 1016 ELO → Win vs 1020 → 1032 ELO (+16)
Match 3: 1032 ELO → Loss vs 1050 → 1017 ELO (-15)
Match 4: 1017 ELO → Win vs 1100 → 1041 ELO (+24) [Underdog!]
Match 5: 1041 ELO → Win vs 1050 → 1057 ELO (+16)
```

### **Rank Progression**

```
Bronze 1:   0 ELO → ~20 wins → 400 ELO (Silver 1)
Silver 1: 400 ELO → ~25 wins → 800 ELO (Gold 1)
Gold 1:   800 ELO → ~25 wins → 1200 ELO (Platinum 1)
Plat 1:  1200 ELO → ~25 wins → 1600 ELO (Diamond 1)
Diamond: 1600 ELO → ~25 wins → 2000 ELO (Immortal 1)
Immortal: 2000 ELO → ~25 wins → 2400 ELO (Radiant 1)
```

## 🧪 Testing

### **Test Scenarios**

1. **Join Queue**
   - User clicks "Find Match"
   - System adds to queue with current ELO
   - UI shows searching animation

2. **Match Found**
   - 10 players in similar ELO range
   - System creates balanced teams
   - Players notified of match

3. **Queue Timeout**
   - Player waits >10 minutes
   - System removes from queue
   - Player notified

4. **Leave Queue**
   - Player clicks "Cancel"
   - Removed from queue immediately
   - Can rejoin anytime

## 🚀 Future Enhancements

1. **Role-Based Matchmaking**
   - Queue for specific roles (Duelist, Controller, etc.)
   - Ensure balanced team compositions

2. **Party Matchmaking**
   - Queue with friends
   - Match against other parties
   - Adjust ELO for party advantage

3. **Ranked Seasons**
   - Reset ELO periodically
   - Seasonal rewards
   - Leaderboards

4. **MMR (Matchmaking Rating)**
   - Hidden MMR separate from displayed rank
   - More accurate skill assessment
   - Prevent smurfing

5. **Dodge Penalties**
   - Penalize players who leave matches
   - Temporary queue bans
   - ELO penalties

---

**ELO & Matchmaking System is now complete and ready to use!** 🎮
