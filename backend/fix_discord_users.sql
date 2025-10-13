-- Fix existing Discord users with duplicate valorantId
-- This updates all users with "NOT_LINKED" to have unique valorantIds

UPDATE users 
SET valorant_id = CONCAT('NOT_LINKED_', discord_id)
WHERE valorant_id = 'NOT_LINKED' AND discord_id IS NOT NULL;

-- Or if you want to delete all test users and start fresh:
-- DELETE FROM users WHERE valorant_id = 'NOT_LINKED';
-- DELETE FROM users WHERE discord_id IS NOT NULL;
