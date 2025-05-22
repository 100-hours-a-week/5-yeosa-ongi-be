-- V5__add_created_date_column_and_index.sql

-- 인덱스 추가 (user_id, created_date + soft delete 조건)
CREATE INDEX idx_picture_user_deleted_created_date
    ON picture(user_id, created_date, deleted_at);
