-- Clean migration - run each command separately
-- Ignore any "already exists" errors

-- 1. Check what currently exists
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;

-- 2. Update any NULL status values (safe to run)
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- 3. Check current status distribution
SELECT status, COUNT(*) as count 
FROM users 
GROUP BY status;

-- 4. Verify status column exists and is working
SELECT COUNT(*) as total_users, 
       COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_users
FROM users;
