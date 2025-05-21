-- V6 script, 인덱스 수정하고 새 인덱스 추가

-- 불필요 인덱스 삭제
DROP INDEX idx_album_id_deleted ON album;

-- 수정 필요한 인덱스 삭제 후 다시 인덱스 부여(순서 변경)
DROP INDEX idx_picture_user_deleted_created_date ON picture;
CREATE INDEX idx_picture_user_deleted_created_date
    ON picture(user_id, deleted_at, created_date);

-- user_album에 새 인덱스
CREATE INDEX idx_user_album_userid_deleted
    ON user_album(user_id, deleted_at)
