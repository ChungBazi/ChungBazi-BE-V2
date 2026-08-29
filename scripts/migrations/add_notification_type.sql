ALTER TABLE notification
    ADD COLUMN notification_type VARCHAR(40) NOT NULL DEFAULT 'UNKNOWN'
    AFTER notification_category;

ALTER TABLE notification
    MODIFY COLUMN notification_type VARCHAR(40) NOT NULL;
