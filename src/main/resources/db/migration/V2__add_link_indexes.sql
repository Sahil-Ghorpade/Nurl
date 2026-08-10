CREATE INDEX idx_links_user_deleted
    ON links(user_id, deleted);

CREATE INDEX idx_links_deleted_expires
    ON links(deleted, expires_at);