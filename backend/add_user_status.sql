-- Add status column to users table for admin management
-- This script adds a status field to track user account status (ACTIVE, SUSPENDED, BANNED)

-- Add the status column with default value ACTIVE
ALTER TABLE users 
ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Add a check constraint to ensure only valid status values
ALTER TABLE users 
ADD CONSTRAINT chk_user_status 
CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED'));

-- Create an index on status for better query performance
CREATE INDEX idx_users_status ON users(status);

-- Update any existing users to have ACTIVE status (should already be default)
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- Optional: Add some comments for documentation
COMMENT ON COLUMN users.status IS 'User account status: ACTIVE, SUSPENDED, or BANNED';

-- Verify the changes
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'status';
