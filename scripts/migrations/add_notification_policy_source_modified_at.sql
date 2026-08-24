ALTER TABLE notification
    ADD COLUMN policy_source_modified_at DATETIME(6) NULL
    AFTER policy_id;
