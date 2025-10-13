-- Fix ALL tables - Add missing audit columns (MySQL compatible)
USE valorant_arena;

-- Disable foreign key checks temporarily
SET FOREIGN_KEY_CHECKS = 0;

-- Fix users table (ignore errors if columns exist)
ALTER TABLE users ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE users ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- Fix teams table (ignore errors if columns exist)
ALTER TABLE teams ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE teams ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- Fix matches table (ignore errors if columns exist or table doesn't exist)
ALTER TABLE matches ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE matches ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- Fix scrims table (ignore errors if columns exist or table doesn't exist)
ALTER TABLE scrims ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE scrims ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- Fix matchmaking_queue table (ignore errors if columns exist or table doesn't exist)
ALTER TABLE matchmaking_queue ADD COLUMN createdAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6);
ALTER TABLE matchmaking_queue ADD COLUMN updatedAt DATETIME(6) NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6);

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'All tables fixed! (Ignore any duplicate column errors)' as status;
