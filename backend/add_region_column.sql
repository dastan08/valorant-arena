-- Add region column to users table
ALTER TABLE users ADD COLUMN region VARCHAR(20) AFTER valorant_id;
