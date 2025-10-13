-- ============================================
-- TOURNAMENT MANAGEMENT SYSTEM - DATABASE SCHEMA
-- Version: 1.0
-- ============================================

-- ============================================
-- ORGANIZERS TABLE
-- ============================================
CREATE TABLE organizers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    organization_name VARCHAR(255) NOT NULL,
    description TEXT,
    website_url VARCHAR(500),
    discord_server VARCHAR(255),
    verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    verified_by BIGINT REFERENCES users(id),
    verified_at TIMESTAMP,
    rejection_reason TEXT,
    total_tournaments_hosted INT DEFAULT 0,
    reputation_score DECIMAL(3,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_verification_status CHECK (verification_status IN ('PENDING', 'VERIFIED', 'REJECTED', 'SUSPENDED')),
    CONSTRAINT fk_organizers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_organizers_verified_by FOREIGN KEY (verified_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_organizers_user_id ON organizers(user_id);
CREATE INDEX idx_organizers_verification_status ON organizers(verification_status);

-- ============================================
-- TOURNAMENTS TABLE
-- ============================================
CREATE TABLE tournaments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organizer_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    banner_url VARCHAR(500),
    game_mode VARCHAR(50) NOT NULL,
    format VARCHAR(50) NOT NULL,
    max_participants INT NOT NULL,
    min_participants INT NOT NULL DEFAULT 4,
    team_size INT NOT NULL DEFAULT 5,
    min_rank VARCHAR(50),
    max_rank VARCHAR(50),
    region VARCHAR(50),
    entry_fee DECIMAL(10,2) DEFAULT 0.00,
    prize_pool DECIMAL(10,2) DEFAULT 0.00,
    prize_distribution JSON,
    registration_start TIMESTAMP NOT NULL,
    registration_end TIMESTAMP NOT NULL,
    check_in_start TIMESTAMP NOT NULL,
    check_in_end TIMESTAMP NOT NULL,
    tournament_start TIMESTAMP NOT NULL,
    tournament_end TIMESTAMP,
    rules TEXT,
    match_format VARCHAR(50) DEFAULT 'BO1',
    map_pool JSON,
    allow_substitutes BOOLEAN DEFAULT false,
    max_substitutes INT DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    current_round INT DEFAULT 0,
    total_rounds INT,
    is_featured BOOLEAN DEFAULT false,
    is_verified BOOLEAN DEFAULT false,
    moderation_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_tournament_status CHECK (status IN ('DRAFT', 'OPEN', 'REGISTRATION_CLOSED', 'CHECK_IN', 'ONGOING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_game_mode CHECK (game_mode IN ('SOLO', 'DUO', 'TEAM_5V5')),
    CONSTRAINT chk_format CHECK (format IN ('SINGLE_ELIMINATION', 'DOUBLE_ELIMINATION', 'ROUND_ROBIN', 'SWISS')),
    CONSTRAINT fk_tournaments_organizer FOREIGN KEY (organizer_id) REFERENCES organizers(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tournaments_organizer_id ON tournaments(organizer_id);
CREATE INDEX idx_tournaments_status ON tournaments(status);
CREATE INDEX idx_tournaments_start_date ON tournaments(tournament_start);
CREATE INDEX idx_tournaments_region ON tournaments(region);

-- ============================================
-- TOURNAMENT ENTRIES TABLE
-- ============================================
CREATE TABLE tournament_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    team_id BIGINT REFERENCES teams(id) ON DELETE CASCADE,
    entry_type VARCHAR(20) NOT NULL,
    seed_number INT,
    checked_in BOOLEAN DEFAULT false,
    checked_in_at TIMESTAMP,
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    payment_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'REGISTERED',
    eliminated_at TIMESTAMP,
    elimination_round INT,
    final_placement INT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_entry_type CHECK (entry_type IN ('SOLO', 'TEAM')),
    CONSTRAINT chk_entry_status CHECK (status IN ('REGISTERED', 'CHECKED_IN', 'DISQUALIFIED', 'ELIMINATED', 'WINNER')),
    CONSTRAINT chk_user_or_team CHECK ((user_id IS NOT NULL AND team_id IS NULL) OR (user_id IS NULL AND team_id IS NOT NULL))
);

CREATE INDEX idx_tournament_entries_tournament_id ON tournament_entries(tournament_id);
CREATE INDEX idx_tournament_entries_user_id ON tournament_entries(user_id);
CREATE INDEX idx_tournament_entries_team_id ON tournament_entries(team_id);
CREATE INDEX idx_tournament_entries_status ON tournament_entries(status);

-- ============================================
-- TOURNAMENT MATCHES TABLE
-- ============================================
CREATE TABLE tournament_matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    round_number INT NOT NULL,
    match_number INT NOT NULL,
    participant1_id BIGINT REFERENCES tournament_entries(id) ON DELETE SET NULL,
    participant2_id BIGINT REFERENCES tournament_entries(id) ON DELETE SET NULL,
    scheduled_time TIMESTAMP,
    actual_start_time TIMESTAMP,
    actual_end_time TIMESTAMP,
    participant1_score INT DEFAULT 0,
    participant2_score INT DEFAULT 0,
    winner_id BIGINT REFERENCES tournament_entries(id) ON DELETE SET NULL,
    best_of INT DEFAULT 1,
    maps_played JSON,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    reported_by BIGINT REFERENCES users(id),
    reported_at TIMESTAMP,
    confirmed_by BIGINT REFERENCES users(id),
    confirmed_at TIMESTAMP,
    screenshot_urls JSON,
    match_replay_url VARCHAR(500),
    is_disputed BOOLEAN DEFAULT false,
    dispute_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_match_status CHECK (status IN ('SCHEDULED', 'READY', 'IN_PROGRESS', 'COMPLETED', 'DISPUTED', 'CANCELLED'))
);

CREATE INDEX idx_tournament_matches_tournament_id ON tournament_matches(tournament_id);
CREATE INDEX idx_tournament_matches_status ON tournament_matches(status);
CREATE INDEX idx_tournament_matches_scheduled_time ON tournament_matches(scheduled_time);

-- ============================================
-- BRACKETS TABLE
-- ============================================
CREATE TABLE brackets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    bracket_type VARCHAR(20) NOT NULL,
    round_number INT NOT NULL,
    match_number INT NOT NULL,
    match_id BIGINT REFERENCES tournament_matches(id) ON DELETE SET NULL,
    next_match_bracket_id BIGINT REFERENCES brackets(id) ON DELETE SET NULL,
    loser_bracket_id BIGINT REFERENCES brackets(id) ON DELETE SET NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_bracket_type CHECK (bracket_type IN ('WINNERS', 'LOSERS')),
    CONSTRAINT chk_bracket_status CHECK (status IN ('PENDING', 'READY', 'IN_PROGRESS', 'COMPLETED'))
);

CREATE INDEX idx_brackets_tournament_id ON brackets(tournament_id);
CREATE INDEX idx_brackets_match_id ON brackets(match_id);
CREATE INDEX idx_brackets_round ON brackets(tournament_id, round_number);

-- ============================================
-- TOURNAMENT DISPUTES TABLE
-- ============================================
CREATE TABLE tournament_disputes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    match_id BIGINT REFERENCES tournament_matches(id) ON DELETE CASCADE,
    reported_by BIGINT NOT NULL REFERENCES users(id),
    reported_against BIGINT REFERENCES users(id),
    dispute_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    evidence_urls JSON,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    assigned_to BIGINT REFERENCES users(id),
    resolution TEXT,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_dispute_type CHECK (dispute_type IN ('SCORE_DISPUTE', 'CHEATING', 'RULE_VIOLATION', 'NO_SHOW', 'CONNECTION_ISSUE', 'OTHER')),
    CONSTRAINT chk_dispute_status CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'REJECTED'))
);

CREATE INDEX idx_tournament_disputes_tournament_id ON tournament_disputes(tournament_id);
CREATE INDEX idx_tournament_disputes_status ON tournament_disputes(status);
CREATE INDEX idx_tournament_disputes_assigned_to ON tournament_disputes(assigned_to);

-- ============================================
-- TOURNAMENT STATISTICS TABLE
-- ============================================
CREATE TABLE tournament_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL REFERENCES tournaments(id) ON DELETE CASCADE,
    entry_id BIGINT NOT NULL REFERENCES tournament_entries(id) ON DELETE CASCADE,
    matches_played INT DEFAULT 0,
    matches_won INT DEFAULT 0,
    matches_lost INT DEFAULT 0,
    rounds_won INT DEFAULT 0,
    rounds_lost INT DEFAULT 0,
    final_placement INT,
    prize_won DECIMAL(10,2) DEFAULT 0.00,
    additional_stats JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_tournament_entry_stats UNIQUE (tournament_id, entry_id)
);

CREATE INDEX idx_tournament_statistics_tournament_id ON tournament_statistics(tournament_id);
CREATE INDEX idx_tournament_statistics_entry_id ON tournament_statistics(entry_id);
