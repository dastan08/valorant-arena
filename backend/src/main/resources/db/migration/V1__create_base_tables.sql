-- ============================================
-- BASE TABLES - USER MANAGEMENT
-- Version: 1.0
-- ============================================

-- ============================================
-- USERS TABLE
-- ============================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    riot_id VARCHAR(100),
    riot_tag VARCHAR(10),
    rank VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    profile_picture_url VARCHAR(500),
    discord_id VARCHAR(100),
    is_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN', 'MODERATOR', 'ORGANIZER')),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_riot_id ON users(riot_id);
CREATE INDEX idx_users_status ON users(status);

-- ============================================
-- TEAMS TABLE
-- ============================================
CREATE TABLE teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    tag VARCHAR(10) NOT NULL UNIQUE,
    captain_id BIGINT NOT NULL,
    logo VARCHAR(500),
    description TEXT,
    region VARCHAR(50),
    elo INT DEFAULT 1000,
    max_members INT DEFAULT 5,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_team_status CHECK (status IN ('ACTIVE', 'DISBANDED')),
    CONSTRAINT fk_teams_captain FOREIGN KEY (captain_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_teams_name ON teams(name);
CREATE INDEX idx_teams_tag ON teams(tag);
CREATE INDEX idx_teams_captain_id ON teams(captain_id);
CREATE INDEX idx_teams_status ON teams(status);

-- ============================================
-- TEAM MEMBERS TABLE
-- ============================================
CREATE TABLE team_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_team_member UNIQUE (team_id, user_id),
    CONSTRAINT chk_member_role CHECK (role IN ('CAPTAIN', 'MEMBER', 'SUBSTITUTE')),
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_team_members_team_id ON team_members(team_id);
CREATE INDEX idx_team_members_user_id ON team_members(user_id);
