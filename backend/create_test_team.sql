-- ============================================================================
-- VALORANT ARENA - Create Test Team and Assign to User
-- ============================================================================

USE valorant_arena;

-- Get testuser6's ID
SET @user_id = (SELECT id FROM users WHERE username = 'testuser6' LIMIT 1);

-- If testuser6 doesn't exist, use testuser1
IF @user_id IS NULL THEN
    SET @user_id = (SELECT id FROM users WHERE username = 'testuser1' LIMIT 1);
END IF;

-- Create a test team with testuser as captain
INSERT INTO teams (name, tag, captain_id, description, logo, region, elo, max_members, status, created_at, updated_at)
VALUES (
    'Phoenix Squad',
    'PHNX',
    @user_id,
    'A competitive Valorant team ready to dominate',
    'default-team-logo.png',
    'NA',
    1200,
    5,
    'ACTIVE',
    NOW(),
    NOW()
);

SET @team_id = LAST_INSERT_ID();

-- Assign the user to the team
UPDATE users 
SET team_id = @team_id, team_role = 'CAPTAIN'
WHERE id = @user_id;

-- Add the user as a team member
INSERT INTO team_members (team_id, user_id, role, joined_at)
VALUES (@team_id, @user_id, 'CAPTAIN', NOW());

-- Display results
SELECT 
    u.id as user_id,
    u.username,
    t.id as team_id,
    t.name as team_name,
    t.tag as team_tag,
    u.team_role
FROM users u
LEFT JOIN teams t ON u.team_id = t.id
WHERE u.id = @user_id;

SELECT 'Test team created and assigned successfully!' as status;
