-- Recreate database from scratch
DROP DATABASE IF EXISTS valorant_arena;
CREATE DATABASE valorant_arena CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

SELECT 'Database recreated! Now restart the backend server.' as status;
