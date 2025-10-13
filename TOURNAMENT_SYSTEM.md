# 🏆 Tournament Management System (TMS)

## Overview
The Tournament Management System allows verified organizers to create, host, and manage competitive Valorant tournaments on the platform.

## 🚀 Current Implementation Status

### ✅ Completed
- **Database Schema**: All tournament tables created (V6 migration)
- **Entity Models**: Organizer, Tournament, TournamentEntry, TournamentMatch
- **Repositories**: Basic CRUD and query methods

### 🔨 In Progress
- Backend Services (TournamentService, OrganizerService)
- REST API Controllers
- Frontend Components

### 📋 Pending
- Bracket Generation Logic
- Notification System
- Match Automation
- Admin Tools
- Frontend UI

---

## 📊 Database Structure

### Core Tables
1. **organizers** - Tournament organizer profiles and verification
2. **tournaments** - Tournament configuration and metadata
3. **tournament_entries** - Player/team registrations
4. **tournament_matches** - Individual matches
5. **brackets** - Tournament bracket structure
6. **tournament_disputes** - Dispute management
7. **tournament_statistics** - Performance tracking

---

## 🎯 User Roles

### Player
- Browse and search tournaments
- Register for tournaments (solo or team)
- Check in before tournament start
- View brackets and match schedules
- Report match scores
- View tournament statistics

### Organizer
- Apply for organizer verification
- Create and configure tournaments
- Manage registrations
- Generate brackets
- Monitor matches
- Resolve disputes

### Admin
- Verify organizer applications
- Moderate tournaments
- Resolve disputes
- Ban rule violators
- View analytics

---

## 🔄 Tournament Lifecycle

```
1. DRAFT → Organizer creates tournament
2. OPEN → Published, registration open
3. REGISTRATION_CLOSED → Registration deadline passed
4. CHECK_IN → Check-in window active
5. ONGOING → Tournament in progress
6. COMPLETED → Tournament finished
7. CANCELLED → Tournament cancelled
```

---

## 🎮 Tournament Types

### Game Modes
- **SOLO**: Individual players
- **DUO**: 2-player teams
- **TEAM_5V5**: Full 5-player teams

### Formats
- **SINGLE_ELIMINATION**: Single bracket, one loss = elimination
- **DOUBLE_ELIMINATION**: Winners + Losers brackets
- **ROUND_ROBIN**: Everyone plays everyone
- **SWISS**: Pairing based on performance

---

## 🔌 API Endpoints (Planned)

### Organizer Management
```
POST   /api/organizers/apply
GET    /api/organizers/me
PUT    /api/organizers/me
```

### Tournament CRUD
```
POST   /api/tournaments
GET    /api/tournaments/{id}
PUT    /api/tournaments/{id}
DELETE /api/tournaments/{id}
POST   /api/tournaments/{id}/publish
POST   /api/tournaments/{id}/start
POST   /api/tournaments/{id}/cancel
```

### Tournament Discovery
```
GET    /api/tournaments (with filters)
GET    /api/tournaments/featured
GET    /api/tournaments/upcoming
GET    /api/tournaments/{id}/participants
GET    /api/tournaments/{id}/bracket
```

### Registration
```
POST   /api/tournaments/{id}/register
DELETE /api/tournaments/{id}/unregister
POST   /api/tournaments/{id}/check-in
GET    /api/tournaments/my-registrations
```

### Match Management
```
GET    /api/tournaments/{id}/matches
GET    /api/matches/{id}
POST   /api/matches/{id}/report-score
POST   /api/matches/{id}/confirm-score
POST   /api/matches/{id}/dispute
```

### Admin
```
GET    /api/admin/organizers/pending
POST   /api/admin/organizers/{id}/verify
GET    /api/admin/disputes
POST   /api/admin/disputes/{id}/resolve
```

---

## 🎨 Frontend Components (Planned)

### Player Views
- `tournament-list` - Browse tournaments
- `tournament-detail` - View tournament info
- `tournament-registration` - Register for tournament
- `bracket-view` - View tournament bracket
- `my-tournaments` - User's tournament history

### Organizer Views
- `organizer-dashboard` - Overview of tournaments
- `tournament-create` - Multi-step tournament creation
- `tournament-manage` - Manage active tournament
- `participant-management` - View/manage participants
- `bracket-generator` - Generate and manage brackets

### Admin Views
- `organizer-verification` - Verify organizer applications
- `tournament-moderation` - Monitor tournaments
- `dispute-resolution` - Handle disputes

---

## 🔔 Notification System (Planned)

### Notification Types
- Registration opened
- Registration closing soon (24h warning)
- Check-in opened
- Match ready (15 min warning)
- Match started
- Opponent reported score
- Tournament started
- Round advanced
- Tournament completed

### Delivery Channels
- Discord (via bot)
- Email
- In-app notifications

---

## 🏗️ Next Steps

### Phase 1: Core Backend (Current)
- [ ] Create TournamentService
- [ ] Create OrganizerService
- [ ] Create TournamentController
- [ ] Create OrganizerController
- [ ] Add validation and error handling

### Phase 2: Bracket System
- [ ] Implement bracket generation algorithms
- [ ] Create BracketService
- [ ] Add bracket progression logic
- [ ] Handle byes and seeding

### Phase 3: Match Management
- [ ] Create MatchService
- [ ] Implement score reporting
- [ ] Add dispute system
- [ ] Screenshot upload handling

### Phase 4: Frontend
- [ ] Tournament list and filters
- [ ] Tournament detail page
- [ ] Registration flow
- [ ] Bracket visualization
- [ ] Organizer dashboard

### Phase 5: Notifications
- [ ] Event system
- [ ] Discord integration
- [ ] Email templates
- [ ] In-app notifications

### Phase 6: Admin Tools
- [ ] Organizer verification UI
- [ ] Tournament moderation
- [ ] Dispute resolution interface
- [ ] Analytics dashboard

---

## 🔒 Security Considerations

- Role-based access control (RBAC)
- Organizer verification required
- Rate limiting on registrations
- Screenshot validation
- Payment security (if entry fees)
- Dispute cooldown to prevent abuse

---

## 📈 Scaling Strategy

### Immediate (MVP)
- Redis caching for tournament lists
- Database indexing on frequently queried fields
- CDN for static assets

### Future (Growth)
- Microservices architecture
- Message queue (RabbitMQ/Kafka)
- Read replicas for database
- WebSockets for real-time updates
- Elasticsearch for advanced search

---

## 📝 Notes

- Tournament system is designed to be modular
- Can be extracted as separate microservice later
- Supports both platform-hosted and external tournaments
- Extensible for other games in the future

---

## 🤝 Contributing

When implementing new features:
1. Follow existing code patterns
2. Add comprehensive tests
3. Update this documentation
4. Consider scalability
5. Maintain backward compatibility

---

**Last Updated**: October 12, 2025
**Version**: 1.0.0 (Initial Implementation)
