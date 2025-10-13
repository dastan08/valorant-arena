-- Fix team_invitations status column to support CANCELLED status
USE valorant_arena;

ALTER TABLE team_invitations MODIFY COLUMN status VARCHAR(20) NOT NULL;

-- Verify the change
DESCRIBE team_invitations;
