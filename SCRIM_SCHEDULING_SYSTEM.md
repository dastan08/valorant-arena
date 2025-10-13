# Scrim Scheduling System

## Overview
Teams can challenge each other to practice matches (scrims) with scheduled times, map selection, and match format.

## Features Implemented

### **Backend Complete** ✅

**1. Scrim Entity**
- Challenger Team & Opponent Team
- Scheduled Time
- Status (PENDING, ACCEPTED, DECLINED, CANCELLED, IN_PROGRESS, COMPLETED)
- Map selection
- Best of (1, 3, 5)
- Match result tracking
- Scores

**2. Scrim Status Flow**
```
PENDING → ACCEPTED → IN_PROGRESS → COMPLETED
   ↓           ↓
DECLINED   CANCELLED
```

**3. ScrimService Methods**
- `challengeTeam()` - Send scrim challenge
- `acceptScrim()` - Accept challenge
- `declineScrim()` - Decline challenge
- `cancelScrim()` - Cancel scrim
- `getTeamScrims()` - All scrims for a team
- `getPendingScrims()` - Pending challenges
- `getUpcomingScrims()` - Accepted future scrims

**4. Discord Notifications** ✅
- ⚔️ Challenge sent
- ✅ Challenge accepted
- ❌ Challenge declined
- 🚫 Scrim cancelled

---

## API Endpoints

### Challenge a Team
```http
POST /api/scrims/challenge
Authorization: Bearer <token>
Body: {
  "challengerTeamId": 1,
  "opponentTeamId": 2,
  "scheduledTime": "2025-10-15T18:00:00",
  "message": "Let's practice!",
  "map": "Haven",
  "bestOf": 3
}
```

### Accept Scrim
```http
POST /api/scrims/{scrimId}/accept
Authorization: Bearer <token>
```

### Decline Scrim
```http
POST /api/scrims/{scrimId}/decline
Authorization: Bearer <token>
Body: {
  "reason": "Team not available"
}
```

### Cancel Scrim
```http
POST /api/scrims/{scrimId}/cancel
Authorization: Bearer <token>
Body: {
  "reason": "Schedule conflict"
}
```

### Get Team Scrims
```http
GET /api/scrims/team/{teamId}
GET /api/scrims/team/{teamId}/pending
GET /api/scrims/team/{teamId}/upcoming
```

---

## Database Schema

```sql
CREATE TABLE scrims (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    challenger_team_id BIGINT NOT NULL,
    opponent_team_id BIGINT NOT NULL,
    scheduled_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    message VARCHAR(500),
    map VARCHAR(255) DEFAULT 'TBD',
    best_of INT DEFAULT 1,
    result VARCHAR(20),
    challenger_score INT,
    opponent_score INT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    responded_at DATETIME,
    completed_at DATETIME,
    FOREIGN KEY (challenger_team_id) REFERENCES teams(id),
    FOREIGN KEY (opponent_team_id) REFERENCES teams(id)
);
```

---

## Usage Flow

### **Challenging a Team**
1. Captain/Manager opens team detail
2. Clicks "Challenge to Scrim"
3. Selects:
   - Opponent team
   - Date & time
   - Map
   - Best of (1, 3, or 5)
   - Optional message
4. Sends challenge
5. Opponent team receives Discord notification

### **Responding to Challenge**
1. Opponent team sees pending challenge
2. Captain/Manager can:
   - ✅ Accept → Scrim scheduled
   - ❌ Decline → Challenge rejected
3. Both teams receive Discord notification

### **Managing Scrims**
1. View upcoming scrims
2. View scrim history
3. Cancel if needed
4. Track results

---

## Discord Notifications

### Challenge Sent
```
⚔️ Scrim Challenge

Team Phoenix has challenged your team to a scrim!

Scheduled: 2025-10-15 18:00
Map: Haven
Best of: 3
Message: "Let's practice!"
```

### Challenge Accepted
```
✅ Scrim Accepted

The scrim between Team Phoenix and Team Viper is confirmed!

Scheduled: 2025-10-15 18:00
Map: Haven
Best of: 3

See you on the battlefield!
```

### Challenge Declined
```
❌ Scrim Declined

Team Viper has declined your scrim challenge.

Reason: Team not available
```

### Scrim Cancelled
```
🚫 Scrim Cancelled

The scrim between Team Phoenix and Team Viper has been cancelled.

Reason: Schedule conflict
```

---

## Permissions

**Challenge Team:**
- Captain or Manager of challenger team

**Accept/Decline:**
- Captain or Manager of opponent team

**Cancel:**
- Captain or Manager of either team

---

## Frontend TODO

1. **Scrim List Component**
   - Show all scrims (upcoming, pending, past)
   - Filter by status
   - Sort by date

2. **Challenge Dialog**
   - Team selector
   - Date/time picker
   - Map dropdown
   - Best of selector
   - Message input

3. **Scrim Detail View**
   - Show scrim info
   - Accept/Decline buttons
   - Cancel button
   - Result display

4. **Team Detail Integration**
   - "Challenge to Scrim" button
   - Pending challenges badge
   - Upcoming scrims list

---

## Valorant Maps

- Ascent
- Bind
- Haven
- Split
- Icebox
- Breeze
- Fracture
- Pearl
- Lotus
- Sunset
- Abyss

---

## Match Formats

- **Best of 1** - Single map
- **Best of 3** - First to 2 wins
- **Best of 5** - First to 3 wins

---

## Future Enhancements

- 🎮 In-game result submission
- 📊 Scrim statistics tracking
- 🏆 Scrim leaderboards
- 📅 Calendar view
- 🔔 Reminder notifications
- 💬 Scrim chat/discussion
- 📹 VOD upload/sharing
- 🎯 Performance analytics

---

## Testing Checklist

- [ ] Challenge team successfully
- [ ] Receive Discord notification
- [ ] Accept challenge
- [ ] Both teams notified
- [ ] Decline challenge
- [ ] Challenger notified
- [ ] Cancel scrim
- [ ] Both teams notified
- [ ] View pending scrims
- [ ] View upcoming scrims
- [ ] View scrim history
- [ ] Permissions enforced

---

## Summary

✅ **Backend Complete**
- Entity, Repository, Service, Controller
- Discord notifications
- Permission checks
- Full CRUD operations

⏳ **Frontend Pending**
- UI components
- Challenge dialog
- Scrim list
- Integration with team pages

The scrim scheduling system is ready for frontend integration! 🎮
