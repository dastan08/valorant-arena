# Valorant Arena

A competitive tournament platform for Valorant, inspired by FACEIT, built with **Spring Boot** (Java) and **Angular**.

## Features

- 🎮 **Player & Team Management** - Create teams, invite players, and manage rosters
- 🏆 **Tournament System** - Multiple tournament formats (Single/Double Elimination, Round Robin, Swiss)
- 🏅 **ELO-based Ranking** - Competitive ranking system for players and teams
- ⚡ **Real-time Updates** - Live match updates using WebSocket
- 📊 **Statistics & Leaderboards** - Detailed player and team statistics
- 🔐 **JWT Authentication** - Secure authentication with role-based access control
- 💰 **Prize Pools** - Tournament prize pools and entry fees

## Tech Stack

### Backend
- **Framework**: Spring Boot 3.1.5
- **Language**: Java 17
- **Database**: MySQL
- **Security**: Spring Security + JWT
- **Build Tool**: Maven
- **Real-time**: WebSocket (Spring WebSocket)
- **API Documentation**: Swagger/OpenAPI

### Frontend
- **Framework**: Angular 17
- **Language**: TypeScript
- **Styling**: Tailwind CSS
- **State Management**: RxJS
- **HTTP Client**: Angular HttpClient
- **Authentication**: @auth0/angular-jwt
- **Notifications**: ngx-toastr

## Prerequisites

- **Java**: JDK 17 or higher
- **Node.js**: v16 or higher
- **MySQL**: v8.0 or higher
- **Maven**: v3.6 or higher
- **Angular CLI**: v17 or higher

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/valorant-arena.git
cd valorant-arena
```

### 2. Database Setup

Create a MySQL database:

```sql
CREATE DATABASE valorant_arena;
```

### 3. Backend Setup

```bash
cd backend

# Create secrets configuration file
cp src/main/resources/application-secrets.yml.template src/main/resources/application-secrets.yml

# Edit application-secrets.yml and add your actual secrets:
# - Database password
# - JWT secret key
# - Riot API key and OAuth credentials
# - Discord OAuth credentials and bot token

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

**API Documentation**: `http://localhost:8080/swagger-ui.html`

**⚠️ Important**: Never commit `application-secrets.yml` to version control. It's already in `.gitignore`.

### 4. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

The frontend will start on `http://localhost:4200`

## Configuration

### Backend Configuration

The backend uses two configuration files:

1. **`application.yml`** - Main configuration (committed to git)
   - Contains non-sensitive defaults and structure
   - Uses environment variables with fallback values

2. **`application-secrets.yml`** - Secrets configuration (NOT committed to git)
   - Contains actual API keys, tokens, and passwords
   - Created from `application-secrets.yml.template`
   - Already in `.gitignore`

**Required Secrets:**
- Database password
- JWT secret key (minimum 256 bits)
- Riot Games API key
- Riot OAuth client ID and secret
- Discord OAuth client ID and secret
- Discord bot token

You can also use environment variables instead of `application-secrets.yml`:
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - JWT secret key
- `RIOT_API_KEY` - Riot Games API key
- `RIOT_CLIENT_ID`, `RIOT_CLIENT_SECRET` - Riot OAuth
- `DISCORD_CLIENT_ID`, `DISCORD_CLIENT_SECRET`, `DISCORD_BOT_TOKEN` - Discord

## Project Structure

```
valorant-arena/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/valorantarena/
│   │   │   │   ├── config/          # Configuration classes
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── entity/          # JPA entities
│   │   │   │   ├── enums/           # Enumerations
│   │   │   │   ├── repository/      # JPA repositories
│   │   │   │   ├── security/        # Security & JWT
│   │   │   │   ├── service/         # Business logic
│   │   │   │   └── ValorantArenaApplication.java
│   │   │   └── resources/
│   │   │       └── application.yml
│   │   └── test/
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/          # Reusable components
│   │   │   ├── core/
│   │   │   │   ├── guards/          # Route guards
│   │   │   │   ├── interceptors/    # HTTP interceptors
│   │   │   │   ├── models/          # TypeScript interfaces
│   │   │   │   └── services/        # API services
│   │   │   ├── pages/               # Page components
│   │   │   ├── app.component.ts
│   │   │   ├── app.module.ts
│   │   │   └── app-routing.module.ts
│   │   ├── assets/
│   │   ├── styles.css
│   │   └── index.html
│   ├── angular.json
│   ├── package.json
│   └── tsconfig.json
│
└── README.md
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `GET /api/auth/me` - Get current user profile
- `PUT /api/auth/me` - Update user profile

### Users
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/top` - Get top players by ELO

### Teams
- `GET /api/teams` - Get all teams
- `POST /api/teams` - Create new team
- `GET /api/teams/{id}` - Get team by ID
- `PUT /api/teams/{id}` - Update team
- `DELETE /api/teams/{id}` - Delete team

### Tournaments
- `GET /api/tournaments` - Get all tournaments
- `POST /api/tournaments` - Create tournament
- `GET /api/tournaments/{id}` - Get tournament by ID
- `POST /api/tournaments/{id}/register/{teamId}` - Register team

### Matches
- `GET /api/matches` - Get all matches
- `GET /api/matches/live` - Get live matches
- `POST /api/matches` - Create match
- `PUT /api/matches/{id}` - Update match result

## Features in Detail

### User Management
- User registration with email verification
- JWT-based authentication
- Role-based access control (User, Moderator, Admin)
- Player statistics tracking
- ELO rating system

### Team Management
- Create and manage teams
- Team captain system
- Member invitation and management
- Team statistics and rankings
- Maximum 5 members per team

### Tournament System
- Multiple tournament formats
- Automatic bracket generation
- Prize pool management
- Registration deadlines
- Tournament status tracking

### Match System
- Match scheduling
- Score reporting
- Live match tracking
- Match history
- Dispute resolution

## Development

### Running Tests

**Backend:**
```bash
cd backend
mvn test
```

**Frontend:**
```bash
cd frontend
npm test
```

### Building for Production

**Backend:**
```bash
cd backend
mvn clean package
java -jar target/valorant-arena-backend-1.0.0.jar
```

**Frontend:**
```bash
cd frontend
npm run build
# Output will be in dist/ folder
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Inspired by [FACEIT](https://www.faceit.com/)
- [Riot Games API](https://developer.riotgames.com/)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Angular](https://angular.io/)

## Support

For support, email support@valorantarena.com or join our Discord server.
