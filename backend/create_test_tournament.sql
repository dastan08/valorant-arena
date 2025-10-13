-- ============================================================================
-- VALORANT ARENA - Create Test Tournament
-- ============================================================================
-- This script creates a test tournament for testing registration
-- ============================================================================

USE valorant_arena;

-- First, ensure we have an organizer
-- Get the first user with ORGANIZER role or create one
SET @organizer_user_id = (SELECT id FROM users WHERE `role` = 'ORGANIZER' LIMIT 1);

-- If no organizer exists, update testuser4 to be an organizer
UPDATE users SET `role` = 'ORGANIZER' WHERE username = 'testuser4';
SET @organizer_user_id = (SELECT id FROM users WHERE username = 'testuser4');

-- Create or get organizer record
INSERT INTO organizers (user_id, organization_name, verification_status, created_at, updated_at)
SELECT @organizer_user_id, 'Test Gaming Org', 'VERIFIED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM organizers WHERE user_id = @organizer_user_id);

SET @organizer_id = (SELECT id FROM organizers WHERE user_id = @organizer_user_id);

-- Delete existing test tournament if exists
DELETE FROM tournaments WHERE name = 'Valorant Champions League';

-- Create a test tournament
INSERT INTO tournaments (
    organizer_id,
    name,
    description,
    banner_url,
    game_mode,
    format,
    max_participants,
    min_participants,
    team_size,
    min_rank,
    max_rank,
    region,
    entry_fee,
    prize_pool,
    prize_distribution,
    registration_start,
    registration_end,
    check_in_start,
    check_in_end,
    tournament_start,
    rules,
    match_format,
    map_pool,
    allow_substitutes,
    max_substitutes,
    `status`,
    is_featured,
    created_at,
    updated_at
) VALUES (
    @organizer_id,
    'Valorant Champions League',
    'A Genisis Hackathon inclusive 5v5 Podium is called for all the spotlights!',
    'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=1200&h=400&fit=crop',
    'COMPETITIVE',
    'SINGLE_ELIMINATION',
    16,
    4,
    5,
    'IRON',
    'RADIANT',
    'NA',
    0.00,
    3000.00,
    '{"1st": 1500, "2nd": 1000, "3rd": 500}',
    NOW(),
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    DATE_ADD(NOW(), INTERVAL 8 DAY),
    'Standard Valorant competitive rules apply. No cheating, hacking, or toxic behavior. All matches must be played on official servers.',
    'Best of 3',
    '["Bind", "Haven", "Split", "Ascent", "Icebox", "Breeze", "Fracture"]',
    true,
    2,
    'OPEN',
    true,
    NOW(),
    NOW()
);

-- Get the tournament ID
SET @tournament_id = LAST_INSERT_ID();

-- Display the created tournament
SELECT 
    t.id,
    t.name,
    t.description,
    t.format,
    t.status,
    t.max_participants,
    t.prize_pool,
    t.entry_fee,
    t.registration_start,
    t.registration_end,
    t.tournament_start,
    o.organization_name as organizer
FROM tournaments t
JOIN organizers o ON t.organizer_id = o.id
WHERE t.id = @tournament_id;

-- ============================================================================
-- Summary
-- ============================================================================
SELECT 'Test tournament created successfully!' as status;
SELECT CONCAT('Tournament ID: ', @tournament_id) as tournament_info;
SELECT 'You can now test tournament registration!' as next_step;
