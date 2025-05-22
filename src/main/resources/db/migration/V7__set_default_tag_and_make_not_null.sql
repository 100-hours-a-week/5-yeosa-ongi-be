-- picture tag가 null이면 디폴트값으로 변경 ("AI 분석 전")

UPDATE picture SET tag='AI 분석 전' WHERE tag is NULL;

-- 태그 not null로 제약
ALTER TABLE picture
    MODIFY tag VARCHAR(20) NOT NULL;