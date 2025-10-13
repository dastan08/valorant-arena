-- ============================================================================
-- VALORANT ARENA - Add Missing Columns (Preserves Existing Data)
-- ============================================================================
-- This script adds missing createdAt and updatedAt columns to all tables
-- Use this if you want to keep your existing data
-- ============================================================================

USE valorant_arena;

-- Disable foreign key checks to avoid constraint errors
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- Add audit columns to USERS table
-- ============================================================================
-- Check if columns exist, if not add them
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'users' 
     AND COLUMN_NAME = 'createdAt') = 0,
    'ALTER TABLE users ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6)',
    'SELECT "users.createdAt already exists" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'users' 
     AND COLUMN_NAME = 'updatedAt') = 0,
    'ALTER TABLE users ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)',
    'SELECT "users.updatedAt already exists" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update NULL values in users table
UPDATE users 
SET createdAt = COALESCE(createdAt, CURRENT_TIMESTAMP(6)), 
    updatedAt = COALESCE(updatedAt, CURRENT_TIMESTAMP(6))
WHERE createdAt IS NULL OR updatedAt IS NULL;

-- ============================================================================
-- Add audit columns to TEAMS table
-- ============================================================================
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'teams' 
     AND COLUMN_NAME = 'createdAt') = 0,
    'ALTER TABLE teams ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6)',
    'SELECT "teams.createdAt already exists" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'teams' 
     AND COLUMN_NAME = 'updatedAt') = 0,
    'ALTER TABLE teams ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)',
    'SELECT "teams.updatedAt already exists" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update NULL values in teams table
UPDATE teams 
SET createdAt = COALESCE(createdAt, CURRENT_TIMESTAMP(6)), 
    updatedAt = COALESCE(updatedAt, CURRENT_TIMESTAMP(6))
WHERE createdAt IS NULL OR updatedAt IS NULL;

-- ============================================================================
-- Add audit columns to MATCHES table (if exists)
-- ============================================================================
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'matches') > 0,
    (SELECT IF(
        (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
         WHERE TABLE_SCHEMA = 'valorant_arena' 
         AND TABLE_NAME = 'matches' 
         AND COLUMN_NAME = 'createdAt') = 0,
        'ALTER TABLE matches ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6)',
        'SELECT "matches.createdAt already exists" as info'
    )),
    'SELECT "matches table does not exist" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'matches') > 0,
    (SELECT IF(
        (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
         WHERE TABLE_SCHEMA = 'valorant_arena' 
         AND TABLE_NAME = 'matches' 
         AND COLUMN_NAME = 'updatedAt') = 0,
        'ALTER TABLE matches ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)',
        'SELECT "matches.updatedAt already exists" as info'
    )),
    'SELECT "matches table does not exist" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update NULL values in matches table if it exists
UPDATE matches 
SET createdAt = COALESCE(createdAt, CURRENT_TIMESTAMP(6)), 
    updatedAt = COALESCE(updatedAt, CURRENT_TIMESTAMP(6))
WHERE createdAt IS NULL OR updatedAt IS NULL;

-- ============================================================================
-- Add audit columns to SCRIMS table (if exists)
-- ============================================================================
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'scrims') > 0,
    (SELECT IF(
        (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
         WHERE TABLE_SCHEMA = 'valorant_arena' 
         AND TABLE_NAME = 'scrims' 
         AND COLUMN_NAME = 'createdAt') = 0,
        'ALTER TABLE scrims ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6)',
        'SELECT "scrims.createdAt already exists" as info'
    )),
    'SELECT "scrims table does not exist" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'scrims') > 0,
    (SELECT IF(
        (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
         WHERE TABLE_SCHEMA = 'valorant_arena' 
         AND TABLE_NAME = 'scrims' 
         AND COLUMN_NAME = 'updatedAt') = 0,
        'ALTER TABLE scrims ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)',
        'SELECT "scrims.updatedAt already exists" as info'
    )),
    'SELECT "scrims table does not exist" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update NULL values in scrims table if it exists
UPDATE scrims 
SET createdAt = COALESCE(createdAt, CURRENT_TIMESTAMP(6)), 
    updatedAt = COALESCE(updatedAt, CURRENT_TIMESTAMP(6))
WHERE createdAt IS NULL OR updatedAt IS NULL;

-- ============================================================================
-- Add audit columns to MATCHMAKING_QUEUE table (if exists)
-- ============================================================================
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'matchmaking_queue') > 0,
    (SELECT IF(
        (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
         WHERE TABLE_SCHEMA = 'valorant_arena' 
         AND TABLE_NAME = 'matchmaking_queue' 
         AND COLUMN_NAME = 'createdAt') = 0,
        'ALTER TABLE matchmaking_queue ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6)',
        'SELECT "matchmaking_queue.createdAt already exists" as info'
    )),
    'SELECT "matchmaking_queue table does not exist" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES 
     WHERE TABLE_SCHEMA = 'valorant_arena' 
     AND TABLE_NAME = 'matchmaking_queue') > 0,
    (SELECT IF(
        (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
         WHERE TABLE_SCHEMA = 'valorant_arena' 
         AND TABLE_NAME = 'matchmaking_queue' 
         AND COLUMN_NAME = 'updatedAt') = 0,
        'ALTER TABLE matchmaking_queue ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)',
        'SELECT "matchmaking_queue.updatedAt already exists" as info'
    )),
    'SELECT "matchmaking_queue table does not exist" as info'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update NULL values in matchmaking_queue table if it exists
UPDATE matchmaking_queue 
SET createdAt = COALESCE(createdAt, CURRENT_TIMESTAMP(6)), 
    updatedAt = COALESCE(updatedAt, CURRENT_TIMESTAMP(6))
WHERE createdAt IS NULL OR updatedAt IS NULL;

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- Summary
-- ============================================================================
SELECT 'All missing columns added successfully!' as status;
SELECT 'Now restart the backend server with: mvn spring-boot:run' as next_step;
