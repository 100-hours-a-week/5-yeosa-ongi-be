CREATE INDEX idx_picture_s3key_deleted ON picture(s3key, deleted_at);

CREATE INDEX idx_picture_s3key_album_deleted
    ON picture(s3key, album_id, deleted_at);