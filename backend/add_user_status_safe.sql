-- Safe migration script that checks for existing columns/constraints
-- This script adds a status field to track user account status (ACTIVE, SUSPENDED, BANNED)

-- Check if status column exists, if not add it
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'status'
    ) THEN
        ALTER TABLE users 
        ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
        
        RAISE NOTICE 'Added status column to users table';
    ELSE
        RAISE NOTICE 'Status column already exists in users table';
    END IF;
END $$;

-- Check if constraint exists, if not add it
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE table_name = 'users' AND constraint_name = 'chk_user_status'
    ) THEN
        ALTER TABLE users 
        ADD CONSTRAINT chk_user_status 
        CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED'));
        
        RAISE NOTICE 'Added check constraint for user status';
    ELSE
        RAISE NOTICE 'Check constraint chk_user_status already exists';
    END IF;
END $$;

-- Check if index exists, if not create it
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = 'users' AND indexname = 'idx_users_status'
    ) THEN
        CREATE INDEX idx_users_status ON users(status);
        
        RAISE NOTICE 'Created index on users.status';
    ELSE
        RAISE NOTICE 'Index idx_users_status already exists';
    END IF;
END $$;

-- Update any existing users to have ACTIVE status if they have NULL
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- Add comment (this is safe to run multiple times)
COMMENT ON COLUMN users.status IS 'User account status: ACTIVE, SUSPENDED, or BANNED';

-- Verify the final state
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'status';

-- Show current user statuses
SELECT 
    COUNT(*) as total_users,
    status,
    COUNT(*) * 100.0 / SUM(COUNT(*)) OVER() as percentage
FROM users 
GROUP BY status
ORDER BY status;
