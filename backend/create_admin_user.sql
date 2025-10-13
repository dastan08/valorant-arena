-- Create Admin User Script
-- This script creates an admin user or updates an existing user to admin role

-- Option 1: Update an existing user to admin role
-- Replace 'your_username' with your actual username
-- UPDATE users SET role = 'ADMIN', status = 'ACTIVE' WHERE username = 'your_username';

-- Option 2: Check existing users to find one to promote
SELECT id, username, email, role, status, is_verified 
FROM users 
ORDER BY id 
LIMIT 10;

-- Option 3: Create a new admin user
-- Note: You'll need to replace 'hashed_password' with an actual bcrypt hash
-- You can generate one at: https://bcrypt-generator.com/ (use password like 'admin123')
-- Or use the backend to register normally, then run the UPDATE command

-- Example: Create new admin user (uncomment and modify as needed)
/*
INSERT INTO users (
    username, 
    email, 
    password, 
    role, 
    rank, 
    status, 
    is_verified,
    avatar,
    elo,
    wins,
    losses,
    created_at,
    updated_at
) VALUES (
    'admin',
    'admin@valorantarena.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- password: admin123
    'ADMIN',
    'IRON_1',
    'ACTIVE',
    true,
    'default-avatar.png',
    1000,
    0,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
*/

-- Option 4: Promote the first user to admin (safest option)
-- This will make the first registered user an admin
UPDATE users 
SET role = 'ADMIN', status = 'ACTIVE', is_verified = true 
WHERE id = (SELECT MIN(id) FROM users);

-- Verify the admin user was created/updated
SELECT id, username, email, role, status, is_verified 
FROM users 
WHERE role = 'ADMIN';
