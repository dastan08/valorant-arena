# 🏆 Tournament Management System - Setup Guide

## ✅ What's Been Built

### Backend Components
- ✅ **Database Schema** (V6 migration)
  - 7 core tables with relationships
  - Indexes for performance
  - JSONB columns for flexible data

- ✅ **Entity Models**
  - `Organizer` - Tournament organizer profiles
  - `Tournament` - Tournament configuration
  - `TournamentEntry` - Player/team registrations
  - `TournamentMatch` - Match data

- ✅ **Repositories**
  - `OrganizerRepository`
  - `TournamentRepository`
  - `TournamentEntryRepository`
  - `TournamentMatchRepository`

- ✅ **DTOs**
  - `OrganizerApplicationDTO`
  - `CreateTournamentDTO`
  - `TournamentRegistrationDTO`

- ✅ **Services**
  - `TournamentService` - Core tournament logic
  - `OrganizerService` - Organizer management

- ✅ **Controllers**
  - `TournamentController` - Tournament APIs
  - `OrganizerController` - Organizer APIs

---

## 🚀 Setup Instructions

### 1. Run Database Migration

```bash
# The migration will run automatically on next application start
# Or manually run:
psql -U postgres -d valorant_arena -f backend/src/main/resources/db/migration/V6__create_tournament_tables.sql
```

### 2. Add Required Dependencies

Add to `pom.xml` if not already present:

```xml
<!-- JSON Support -->
<dependency>
    <groupId>io.hypersistence</groupId>
    <artifactId>hypersistence-utils-hibernate-60</artifactId>
    <version>3.5.0</version>
</dependency>
```

### 3. Update Security Configuration

Add ORGANIZER role to your security config:

```java
// In SecurityConfig.java or equivalent
public enum Role {
    USER,
    ADMIN,
    MODERATOR,
    ORGANIZER  // Add this
}
```

### 4. Test the APIs

#### Apply as Organizer
```bash
POST /api/organizers/apply
Authorization: Bearer <token>
Content-Type: application/json

{
  "organizationName": "ESports Pro League",
  "description": "Professional esports organization",
  "websiteUrl": "https://esportspro.com",
  "discordServer": "https://discord.gg/esportspro"
}
```

#### Create Tournament (After Verification)
```bash
POST /api/tournaments
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Valorant Summer Championship 2025",
  "description": "Compete for $10,000 prize pool",
  "gameMode": "TEAM_5V5",
  "format": "SINGLE_ELIMINATION",
  "maxParticipants": 32,
  "minParticipants": 8,
  "teamSize": 5,
  "region": "NA",
  "prizePool": 10000.00,
  "registrationStart": "2025-06-01T00:00:00",
  "registrationEnd": "2025-06-15T23:59:59",
  "checkInStart": "2025-06-16T10:00:00",
  "checkInEnd": "2025-06-16T11:00:00",
  "tournamentStart": "2025-06-16T12:00:00",
  "rules": "Standard Valorant competitive rules apply...",
  "matchFormat": "BO3",
  "mapPool": ["Bind", "Haven", "Split", "Ascent"]
}
```

#### Register for Tournament
```bash
POST /api/tournaments/{id}/register
Authorization: Bearer <token>
Content-Type: application/json

{
  "entryType": "TEAM",
  "teamId": 123
}
```

---

## 📋 Next Steps

### Phase 2: Bracket Generation
- [ ] Create `BracketService`
- [ ] Implement single elimination algorithm
- [ ] Implement double elimination algorithm
- [ ] Add seeding logic
- [ ] Handle byes for non-power-of-2 participants

### Phase 3: Match Management
- [ ] Create `MatchService`
- [ ] Implement score reporting
- [ ] Add screenshot upload
- [ ] Dispute system
- [ ] Match notifications

### Phase 4: Frontend
- [ ] Tournament list page
- [ ] Tournament detail page
- [ ] Registration flow
- [ ] Organizer dashboard
- [ ] Bracket visualization

### Phase 5: Admin Tools
- [ ] Organizer verification UI
- [ ] Tournament moderation
- [ ] Dispute resolution
- [ ] Analytics dashboard

---

## 🔌 Available API Endpoints

### Organizer APIs
```
POST   /api/organizers/apply          - Apply as organizer
GET    /api/organizers/me             - Get my organizer profile
PUT    /api/organizers/me             - Update organizer profile
GET    /api/organizers/{id}           - Get organizer by ID
GET    /api/organizers/pending        - Get pending applications (Admin)
POST   /api/organizers/{id}/verify    - Verify organizer (Admin)
POST   /api/organizers/{id}/suspend   - Suspend organizer (Admin)
```

### Tournament APIs
```
POST   /api/tournaments                - Create tournament
GET    /api/tournaments/{id}           - Get tournament details
PUT    /api/tournaments/{id}           - Update tournament
POST   /api/tournaments/{id}/publish   - Publish tournament
GET    /api/tournaments                - List tournaments (with filters)
GET    /api/tournaments/upcoming       - Get upcoming tournaments
GET    /api/tournaments/featured       - Get featured tournaments
POST   /api/tournaments/{id}/register  - Register for tournament
POST   /api/tournaments/{id}/check-in  - Check in for tournament
GET    /api/tournaments/{id}/participants - Get participants
GET    /api/tournaments/my-registrations - Get my tournaments
```

---

## 🎯 Tournament Workflow

### For Organizers
1. **Apply** → Submit organizer application
2. **Wait** → Admin reviews and verifies
3. **Create** → Create tournament in DRAFT status
4. **Configure** → Set rules, schedule, prizes
5. **Publish** → Open registration
6. **Monitor** → Track registrations
7. **Generate Bracket** → After registration closes
8. **Start** → Begin tournament
9. **Manage** → Monitor matches, resolve disputes
10. **Complete** → Finalize results

### For Players
1. **Browse** → Find tournaments
2. **Register** → Join tournament (solo or team)
3. **Check In** → Confirm participation
4. **Compete** → Play matches
5. **Report** → Submit match results
6. **Advance** → Progress through bracket
7. **Win** → Claim prizes

---

## 🔒 Permissions

### USER Role
- Browse tournaments
- Register for tournaments
- Check in
- Report scores

### ORGANIZER Role
- All USER permissions
- Create tournaments
- Manage own tournaments
- Generate brackets

### ADMIN Role
- All permissions
- Verify organizers
- Moderate tournaments
- Resolve disputes
- Feature tournaments

---

## 🐛 Troubleshooting

### Migration Issues
```bash
# Check migration status
SELECT * FROM flyway_schema_history;

# If migration fails, rollback and retry
# Manually drop tables if needed
DROP TABLE IF EXISTS tournament_statistics CASCADE;
DROP TABLE IF EXISTS tournament_disputes CASCADE;
DROP TABLE IF EXISTS brackets CASCADE;
DROP TABLE IF EXISTS tournament_matches CASCADE;
DROP TABLE IF EXISTS tournament_entries CASCADE;
DROP TABLE IF EXISTS tournaments CASCADE;
DROP TABLE IF EXISTS organizers CASCADE;
```

### Common Errors

**"User is not a verified organizer"**
- Solution: Admin must verify organizer application first

**"Tournament registration is not open"**
- Solution: Organizer must publish tournament (DRAFT → OPEN)

**"Registration deadline has passed"**
- Solution: Check tournament.registrationEnd timestamp

**"Tournament is full"**
- Solution: Increase maxParticipants or wait for spot

---

## 📊 Database Queries for Testing

```sql
-- Check organizer applications
SELECT * FROM organizers WHERE verification_status = 'PENDING';

-- View all tournaments
SELECT id, name, status, organizer_id, tournament_start 
FROM tournaments 
ORDER BY created_at DESC;

-- Count tournament entries
SELECT t.name, COUNT(te.id) as entries
FROM tournaments t
LEFT JOIN tournament_entries te ON t.id = te.tournament_id
GROUP BY t.id, t.name;

-- View checked-in participants
SELECT t.name, u.username, te.checked_in_at
FROM tournament_entries te
JOIN tournaments t ON te.tournament_id = t.id
LEFT JOIN users u ON te.user_id = u.id
WHERE te.checked_in = true;
```

---

## 🎨 Frontend Integration (Coming Soon)

The frontend will include:
- Tournament discovery and filtering
- Real-time bracket updates
- Match scheduling calendar
- Notification center
- Organizer dashboard
- Admin moderation tools

---

**Status**: Backend Core Complete ✅
**Next**: Bracket Generation & Match Management
**Version**: 1.0.0
