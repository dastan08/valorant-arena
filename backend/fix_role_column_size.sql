-- Fix the role column size to accommodate TOURNAMENT_MODERATOR
-- The role column is currently too small (probably VARCHAR(10) or VARCHAR(15))
-- TOURNAMENT_MODERATOR is 21 characters, so we need at least VARCHAR(25)

ALTER TABLE users MODIFY COLUMN `role` VARCHAR(30) NOT NULL;

-- Verify the change
DESCRIBE users;
