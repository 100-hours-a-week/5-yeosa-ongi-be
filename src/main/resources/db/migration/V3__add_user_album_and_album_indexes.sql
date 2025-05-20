-- user_album 인덱스
CREATE INDEX idx_user_album_albumid_deleted
    ON user_album(album_id, deleted_at);

CREATE INDEX idx_user_album_userid_deleted
    ON user_album(user_id, deleted_at);

-- album 인덱스
CREATE INDEX idx_album_id_deleted
    ON album(id, deleted_at);

CREATE INDEX idx_album_created_deleted
    ON album(created_at, deleted_at);
