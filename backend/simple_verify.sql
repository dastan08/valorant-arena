-- Simple verification script that works on all PostgreSQL versions

-- 1. Check if status column exists
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'status';

-- 2. Update any NULL status values to ACTIVE
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- 3. Check current status distribution
SELECT status, COUNT(*) as user_count
FROM users 
GROUP BY status
ORDER BY status;

-- 4. Show sample users with their status
SELECT id, username, email, role, status, is_verified 
FROM users 
LIMIT 10;
