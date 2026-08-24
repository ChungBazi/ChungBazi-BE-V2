ALTER TABLE notification
    ADD COLUMN policy_source_modified_at DATETIME(6) NULL
    AFTER policy_id;

ALTER TABLE notification
    ADD CONSTRAINT uk_notification_policy_update_event
    UNIQUE (user_id, policy_id, notification_type, policy_source_modified_at);
