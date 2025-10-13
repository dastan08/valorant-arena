-- Final working migration script
-- Execute these commands one by one

-- 1. Check current table structure first
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;

-- 2. Add constraint (run this even if it gives an error about existing constraint)
ALTER TABLE users ADD CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'BANNED'));

-- 3. Create index (run this even if it gives an error about existing index)
CREATE INDEX idx_users_status ON users(status);

-- 4. Update any NULL values to ACTIVE
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- 5. Verify the final result
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'status';
