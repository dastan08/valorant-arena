-- Create tournament_moderators table for moderator assignment system
-- This allows organizers to assign specific moderators to their tournaments

CREATE TABLE IF NOT EXISTS tournament_moderators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tournament_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    assigned_by BIGINT NOT NULL,
    assigned_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(500),
    UNIQUE KEY unique_tournament_user (tournament_id, user_id),
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_by) REFERENCES users(id),
    INDEX idx_tournament_id (tournament_id),
    INDEX idx_user_id (user_id),
    INDEX idx_assigned_by (assigned_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments
ALTER TABLE tournament_moderators COMMENT = 'Stores tournament moderator assignments';
