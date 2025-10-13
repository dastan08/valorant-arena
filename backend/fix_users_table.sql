-- Fix users table - Add missing audit columns (MySQL syntax)
USE valorant_arena;

-- Check and add createdAt column
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'valorant_arena' 
                   AND TABLE_NAME = 'users' 
                   AND COLUMN_NAME = 'createdAt');

SET @sql = IF(@col_exists = 0, 
              'ALTER TABLE users ADD COLUMN createdAt DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)',
              'SELECT "createdAt column already exists" as info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Check and add updatedAt column
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                   WHERE TABLE_SCHEMA = 'valorant_arena' 
                   AND TABLE_NAME = 'users' 
                   AND COLUMN_NAME = 'updatedAt');

SET @sql = IF(@col_exists = 0, 
              'ALTER TABLE users ADD COLUMN updatedAt DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)',
              'SELECT "updatedAt column already exists" as info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update existing rows to have timestamps
UPDATE users 
SET createdAt = COALESCE(createdAt, CURRENT_TIMESTAMP(6)), 
    updatedAt = COALESCE(updatedAt, CURRENT_TIMESTAMP(6))
WHERE createdAt IS NULL OR updatedAt IS NULL;

-- Make columns NOT NULL after setting values
ALTER TABLE users 
MODIFY COLUMN createdAt DATETIME(6) NOT NULL,
MODIFY COLUMN updatedAt DATETIME(6) NOT NULL;

SELECT 'Users table fixed successfully!' as status;
