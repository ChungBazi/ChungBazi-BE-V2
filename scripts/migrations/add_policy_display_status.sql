ALTER TABLE policy
    ADD COLUMN display_status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE';

UPDATE policy
SET display_status = 'VISIBLE'
WHERE display_status IS NULL;

ALTER TABLE policy
    MODIFY display_status VARCHAR(20) NOT NULL DEFAULT 'VISIBLE';
