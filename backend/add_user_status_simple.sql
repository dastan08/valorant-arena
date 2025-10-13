-- Simple migration script for adding user status column
-- Run each section separately if needed

-- First, check what columns exist
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;

-- If status column doesn't exist, run this:
-- ALTER TABLE users ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';

-- Add constraint (ignore error if it already exists)
ALTER TABLE users ADD CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED'));

-- Create index (ignore error if it already exists)
CREATE INDEX idx_users_status ON users(status);

-- Update any NULL values to ACTIVE
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- Add column comment
COMMENT ON COLUMN users.status IS 'User account status: ACTIVE, SUSPENDED, or BANNED';

-- Verify the result
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'status';
