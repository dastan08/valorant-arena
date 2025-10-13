-- Verification and completion script
-- Run these commands to verify everything is set up correctly

-- 1. Verify the status column exists and check its properties
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'status';

-- 2. Check if constraint exists (should return 1 row)
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'users' AND constraint_name = 'chk_user_status';

-- 3. Check if index exists (should return 1 row)
SELECT indexname, tablename 
FROM pg_indexes 
WHERE tablename = 'users' AND indexname = 'idx_users_status';

-- 4. Update any NULL status values to ACTIVE (safe to run)
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- 5. Check current status distribution
SELECT 
    status, 
    COUNT(*) as user_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) as percentage
FROM users 
GROUP BY status
ORDER BY status;

-- 6. Final verification - show sample users with their status
SELECT id, username, email, role, status, is_verified 
FROM users 
LIMIT 10;
