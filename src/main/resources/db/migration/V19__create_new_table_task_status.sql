CREATE TABLE ai_task_status (
                                task_id      VARCHAR(255) PRIMARY KEY,
                                step         ENUM('EMBEDDING', 'QUALITY', 'DUPLICATE', 'CATEGORY', 'AESTHETIC', 'PEOPLE') NOT NULL,
                                status       ENUM('PENDING', 'IN_PROGRESS', 'SUCCESS', 'FAILED', 'RETRY', 'TIMEOUT') NOT NULL,
                                user_id      BIGINT NOT NULL,
                                album_id     BIGINT NOT NULL,
                                retry_count  INT DEFAULT 0,
                                error_msg    TEXT,
                                s3keys_json  TEXT,
                                created_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
                                updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);