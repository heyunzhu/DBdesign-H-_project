USE library;

-- MySQL/InnoDB automatically creates usable indexes for foreign key columns
-- when no suitable index exists. This file focuses on status filters,
-- pagination, and composite indexes used by frequent business queries.

-- User list filtering: role + status is common in the management page.
CREATE INDEX idx_sys_user_role_status
ON sys_user(role_id, account_status);

-- Book list filtering: status filtering plus stable descending ID pagination.
CREATE INDEX idx_book_status_id
ON book(book_status, book_id);

-- Book category statistics and category filtering.
CREATE INDEX idx_book_type_status
ON book(type_id, book_status);

-- Overdue/unreturned query:
-- WHERE borrow_status IN (0, 2) AND due_return_time < CURRENT_TIMESTAMP.
CREATE INDEX idx_borrow_status_due_time
ON borrow_record(borrow_status, due_return_time);

-- User borrow history:
-- WHERE user_id = ? ORDER BY borrow_time DESC.
CREATE INDEX idx_borrow_user_time
ON borrow_record(user_id, borrow_time);

-- User borrow history with status filtering.
CREATE INDEX idx_borrow_user_status_time
ON borrow_record(user_id, borrow_status, borrow_time);
