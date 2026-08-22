CREATE TEMPORARY TABLE duplicate_fcm_tokens AS
SELECT fcm_token
FROM `user`
WHERE fcm_token IS NOT NULL
GROUP BY fcm_token
HAVING COUNT(*) > 1;

UPDATE `user` AS target_user
JOIN duplicate_fcm_tokens AS duplicate
    ON target_user.fcm_token = duplicate.fcm_token
SET target_user.fcm_token = NULL;

DROP TEMPORARY TABLE duplicate_fcm_tokens;

ALTER TABLE `user`
    ADD CONSTRAINT uk_user_fcm_token UNIQUE (fcm_token);
