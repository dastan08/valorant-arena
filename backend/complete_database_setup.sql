-- ============================================================================
-- VALORANT ARENA - Complete Database Setup Script
-- ============================================================================
-- This script will:
-- 1. Drop and recreate the database
-- 2. Let Hibernate create all tables automatically
-- 3. No manual table creation needed
-- ============================================================================

-- Step 1: Drop existing database (WARNING: This will delete all data!)
DROP DATABASE IF EXISTS valorant_arena;

-- Step 2: Create fresh database with proper character set
CREATE DATABASE valorant_arena 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Step 3: Select the database
USE valorant_arena;

-- ============================================================================
-- IMPORTANT: After running this script:
-- 1. Restart your Spring Boot backend server
-- 2. Hibernate will automatically create all tables with correct schema
-- 3. All tables will have createdAt and updatedAt columns
-- ============================================================================

SELECT 'Database recreated successfully!' as status;
SELECT 'Now restart the backend server with: mvn spring-boot:run' as next_step;
