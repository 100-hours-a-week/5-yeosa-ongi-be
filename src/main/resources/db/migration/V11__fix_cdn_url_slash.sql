-- V11 picture.pictureurl 잘못된 CDN URL 슬래시 보정
UPDATE picture
SET pictureurl = REPLACE(pictureurl, 'https://cdn.ongi.today', 'https://cdn.ongi.today/')
WHERE pictureurl LIKE 'https://cdn.ongi.today%'
  AND pictureurl NOT LIKE 'https://cdn.ongi.today/%';