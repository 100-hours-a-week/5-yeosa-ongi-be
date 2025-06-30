CREATE TABLE comments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          album_id BIGINT NOT NULL,
                          user_id BIGINT NOT NULL,
                          content VARCHAR(300),
                          created_at DATETIME,
                          deleted_at DATETIME,

                          CONSTRAINT fk_comments_album
                              FOREIGN KEY (album_id)
                                  REFERENCES album(id),

                          CONSTRAINT fk_comments_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
);
