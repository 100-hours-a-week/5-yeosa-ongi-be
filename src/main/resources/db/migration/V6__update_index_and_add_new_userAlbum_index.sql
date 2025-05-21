-- V6 script, 인덱스 수정하고 새 인덱스 추가

-- 수정 필요한 인덱스 삭제 후 다시 인덱스 부여(순서 변경)
-- 제약조건 삭제가 너무 번거로워 일단은 인덱스 생성만 실시
CREATE INDEX idx_picture_userid_deletedat_created_date
    ON picture(user_id, deleted_at, created_date);

-- user_album에 새 인덱스
CREATE INDEX idx_user_album_userid_deleted
    ON user_album(user_id, deleted_at)
