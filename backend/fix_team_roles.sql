-- Fix team roles and manager assignments
-- This script fixes the manager logic where captain should be manager when no explicit manager exists

-- Step 1: Set captain as manager for teams that don't have a manager
UPDATE teams 
SET manager_id = captain_id 
WHERE manager_id IS NULL;

-- Step 2: Update team captains role
UPDATE users u 
SET team_role = 'CAPTAIN' 
WHERE u.id IN (
    SELECT t.captain_id 
    FROM teams t 
    WHERE t.captain_id = u.id
) 
AND u.team_role IS NULL;

-- Step 3: Update team managers role (now all teams have managers)
UPDATE users u 
SET team_role = 'MANAGER' 
WHERE u.id IN (
    SELECT t.manager_id 
    FROM teams t 
    WHERE t.manager_id = u.id AND t.manager_id != t.captain_id
) 
AND u.team_role IS NULL;

-- Step 4: Update remaining team members to PLAYER role
UPDATE users u 
SET team_role = 'PLAYER' 
WHERE u.team_id IS NOT NULL 
AND u.team_role IS NULL;

-- Verify the updates
SELECT 
    u.username,
    u.team_role,
    t.name as team_name,
    CASE 
        WHEN u.id = t.captain_id THEN 'CAPTAIN'
        WHEN u.id = t.manager_id THEN 'MANAGER'
        ELSE 'PLAYER'
    END as expected_role
FROM users u
JOIN teams t ON u.team_id = t.id
ORDER BY t.name, u.username;
