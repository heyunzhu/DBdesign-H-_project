USE library;

-- Optional archive table for old returned borrow records.
-- Keep this file as an expansion design. It should be used after the system has
-- accumulated a large amount of borrow history.

CREATE TABLE IF NOT EXISTS borrow_record_archive (
    archive_id INT PRIMARY KEY AUTO_INCREMENT,
    borrow_id INT NOT NULL,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_time DATETIME NOT NULL,
    due_return_time DATETIME NOT NULL,
    actual_return_time DATETIME,
    borrow_status TINYINT NOT NULL,
    archive_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_archive_borrow_status CHECK (borrow_status IN (0, 1, 2))
);

CREATE INDEX idx_archive_user_time
ON borrow_record_archive(user_id, borrow_time);

CREATE INDEX idx_archive_book_time
ON borrow_record_archive(book_id, borrow_time);

CREATE INDEX idx_archive_borrow_time
ON borrow_record_archive(borrow_time);

-- Example archive operation:
-- 1. Copy returned records older than one year into the archive table.
-- 2. Verify the archive result.
-- 3. Delete the copied records from the active table.
--
-- INSERT INTO borrow_record_archive (
--     borrow_id,
--     user_id,
--     book_id,
--     borrow_time,
--     due_return_time,
--     actual_return_time,
--     borrow_status
-- )
-- SELECT
--     borrow_id,
--     user_id,
--     book_id,
--     borrow_time,
--     due_return_time,
--     actual_return_time,
--     borrow_status
-- FROM borrow_record
-- WHERE borrow_status = 1
--   AND actual_return_time < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 YEAR);
--
-- DELETE FROM borrow_record
-- WHERE borrow_status = 1
--   AND actual_return_time < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 YEAR);
