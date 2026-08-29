-- 알림 설정이 없는 기존 사용자에게 기본 ON 설정을 생성합니다.
-- NOT EXISTS 조건으로 애플리케이션 재시작 시에도 중복 삽입되지 않습니다.
INSERT INTO notification_setting (
    user_id,
    all_notification,
    policy_notification,
    chungbazi_notification,
    created_at,
    updated_at
)
SELECT
    user.user_id,
    TRUE,
    TRUE,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM `user` AS user
WHERE NOT EXISTS (
    SELECT 1
    FROM notification_setting AS setting
    WHERE setting.user_id = user.user_id
);
