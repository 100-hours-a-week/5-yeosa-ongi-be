-- V4__extract_s3key_from_picture_url.sql

UPDATE picture
SET s3key = SUBSTRING_INDEX(pictureurl, '/', -1)
WHERE pictureurl IS NOT NULL AND (s3key IS NULL OR s3key = '');