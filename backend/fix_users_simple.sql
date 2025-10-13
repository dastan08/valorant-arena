-- Simple fix for users table - MySQL syntax
USE valorant_arena;

-- Add createdAt column (ignore error if exists)
ALTER TABLE users ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6);

-- Add updatedAt column (ignore error if exists)  
ALTER TABLE users ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- Update NULL values
UPDATE users SET createdAt = CURRENT_TIMESTAMP(6) WHERE createdAt IS NULL;
UPDATE users SET updatedAt = CURRENT_TIMESTAMP(6) WHERE updatedAt IS NULL;

-- Make NOT NULL
ALTER TABLE users MODIFY COLUMN createdAt DATETIME(6) NOT NULL;
ALTER TABLE users MODIFY COLUMN updatedAt DATETIME(6) NOT NULL;

SELECT 'Done!' as status;
