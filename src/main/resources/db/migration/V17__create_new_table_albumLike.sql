CREATE TABLE album_likes(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    CONSTRAINT fk_album_likes_album
        FOREIGN KEY (album_id)
            REFERENCES album(id),

    CONSTRAINT fk_album_likes_user
        FOREIGN KEY (user_id)
            REFERENCES users(id),

    CONSTRAINT uq_album_user UNIQUE (album_id, user_id)
)