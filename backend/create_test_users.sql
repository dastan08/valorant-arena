-- ============================================================================
-- VALORANT ARENA - Create 5 Test Users
-- ============================================================================
-- This script creates 5 test users with realistic Valorant data
-- Password for all test users: test123 (bcrypt hash)
-- ============================================================================

USE valorant_arena;

-- Delete existing test users if they exist (optional - comment out if you want to keep existing data)
DELETE FROM users WHERE username IN ('testuser1', 'testuser2', 'testuser3', 'testuser4', 'testuser5');

-- ============================================================================
-- Insert 5 Test Users
-- ============================================================================

-- Test User 1 - Iron Rank
INSERT INTO users (
    username, 
    email, 
    password, 
    valorantId, 
    riotGameName, 
    riotTagLine, 
    `rank`, 
    `role`, 
    `status`, 
    avatar,
    discordId,
    discordUsername,
    isVerified,
    elo,
    matchesPlayed,
    wins,
    losses,
    kills,
    deaths,
    assists,
    headshotPercentage,
    averageCombatScore,
    createdAt,
    updatedAt
) VALUES (
    'testuser1',
    'testuser1@valorantarena.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: test123
    'IronWarrior#1234',
    'IronWarrior',
    '1234',
    'IRON',
    'USER',
    'ACTIVE',
    'default-avatar.png',
    'ironwarrior#1234',
    'IronWarrior',
    true,
    950,
    0,
    0,
    0,
    0,
    0,
    0,
    0.0,
    0,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);

-- Test User 2 - Bronze Rank
INSERT INTO users (
    username, 
    email, 
    password, 
    valorantId, 
    riotGameName, 
    riotTagLine, 
    `rank`, 
    `role`, 
    `status`, 
    avatar,
    discordId,
    discordUsername,
    isVerified,
    elo,
    matchesPlayed,
    wins,
    losses,
    kills,
    deaths,
    assists,
    headshotPercentage,
    averageCombatScore,
    createdAt,
    updatedAt
) VALUES (
    'testuser2',
    'testuser2@valorantarena.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: test123
    'BronzeKnight#5678',
    'BronzeKnight',
    '5678',
    'BRONZE',
    'USER',
    'ACTIVE',
    'default-avatar.png',
    'bronzeknight#5678',
    'BronzeKnight',
    true,
    1050,
    0,
    0,
    0,
    0,
    0,
    0,
    0.0,
    0,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);

-- Test User 3 - Silver Rank
INSERT INTO users (
    username, 
    email, 
    password, 
    valorantId, 
    riotGameName, 
    riotTagLine, 
    `rank`, 
    `role`, 
    `status`, 
    avatar,
    discordId,
    discordUsername,
    isVerified,
    elo,
    matchesPlayed,
    wins,
    losses,
    kills,
    deaths,
    assists,
    headshotPercentage,
    averageCombatScore,
    createdAt,
    updatedAt
) VALUES (
    'testuser3',
    'testuser3@valorantarena.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: test123
    'SilverSniper#9012',
    'SilverSniper',
    '9012',
    'SILVER',
    'USER',
    'ACTIVE',
    'default-avatar.png',
    'silversniper#9012',
    'SilverSniper',
    true,
    1150,
    0,
    0,
    0,
    0,
    0,
    0,
    0.0,
    0,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);

-- Test User 4 - Gold Rank (Organizer)
INSERT INTO users (
    username, 
    email, 
    password, 
    valorantId, 
    riotGameName, 
    riotTagLine, 
    `rank`, 
    `role`, 
    `status`, 
    avatar,
    discordId,
    discordUsername,
    isVerified,
    elo,
    matchesPlayed,
    wins,
    losses,
    kills,
    deaths,
    assists,
    headshotPercentage,
    averageCombatScore,
    createdAt,
    updatedAt
) VALUES (
    'testuser4',
    'testuser4@valorantarena.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: test123
    'GoldOrganizer#3456',
    'GoldOrganizer',
    '3456',
    'GOLD',
    'ORGANIZER',
    'ACTIVE',
    'default-avatar.png',
    'goldorganizer#3456',
    'GoldOrganizer',
    true,
    1250,
    0,
    0,
    0,
    0,
    0,
    0,
    0.0,
    0,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);

-- Test User 5 - Platinum Rank (Moderator)
INSERT INTO users (
    username, 
    email, 
    password, 
    valorantId, 
    riotGameName, 
    riotTagLine, 
    `rank`, 
    `role`, 
    `status`, 
    avatar,
    discordId,
    discordUsername,
    isVerified,
    elo,
    matchesPlayed,
    wins,
    losses,
    kills,
    deaths,
    assists,
    headshotPercentage,
    averageCombatScore,
    createdAt,
    updatedAt
) VALUES (
    'testuser5',
    'testuser5@valorantarena.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: test123
    'PlatinumMod#7890',
    'PlatinumMod',
    '7890',
    'PLATINUM',
    'MODERATOR',
    'ACTIVE',
    'default-avatar.png',
    'platinummod#7890',
    'PlatinumMod',
    true,
    1350,
    0,
    0,
    0,
    0,
    0,
    0,
    0.0,
    0,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);

-- ============================================================================
-- Verify Test Users Created
-- ============================================================================
SELECT 
    id,
    username,
    email,
    valorantId,
    riotGameName,
    riotTagLine,
    `rank`,
    `role`,
    `status`,
    isVerified,
    elo,
    createdAt
FROM users 
WHERE username IN ('testuser1', 'testuser2', 'testuser3', 'testuser4', 'testuser5')
ORDER BY id;

-- ============================================================================
-- Summary
-- ============================================================================
SELECT '5 test users created successfully!' as status;
SELECT 'Login credentials: username = testuser1-5, password = test123' as credentials;
