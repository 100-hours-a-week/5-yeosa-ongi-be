-- Flyway migration script: Update profile_image to use CDN URL

-- users의 profile 변경

UPDATE users
SET profile_image = CONCAT(
        'https://cdn.ongi.today/',
        CASE
            WHEN profile_image LIKE 'http%' THEN
                SUBSTRING_INDEX(profile_image, '/', -1)  -- URL이면 마지막 파일명만 추출
            ELSE
                profile_image  -- 순수 파일명이면 그대로 사용
            END
                    )
WHERE profile_image NOT LIKE 'http://%.kakaocdn.net/%'
  AND profile_image NOT LIKE 'http://img1.kakaocdn.net/%';


-- picture url 변경

UPDATE picture
SET pictureurl = CONCAT(
        'https://cdn.ongi.today',
        CASE
            WHEN pictureurl LIKE 'http%' THEN SUBSTRING_INDEX(pictureurl, '/', -1)
            ELSE pictureurl
            END
                 )
WHERE pictureurl IS NOT NULL;
