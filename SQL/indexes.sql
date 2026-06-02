USE library;

-- ============================================================
-- Business indexes for frequent queries, pagination,
-- and composite conditions used by the backend.
--
-- MySQL/InnoDB automatically creates usable indexes for
-- foreign key columns when no suitable index exists.
-- This file focuses on status filters, pagination, and
-- composite indexes used by frequent business queries.
-- ============================================================

-- User management: filter by role + account_status, paginate by user_id.
CREATE INDEX idx_sys_user_role_status_id
ON sys_user(role_id, account_status, user_id);

-- Login uses sys_user.user_no, which is already covered by the UNIQUE index
-- created in schema.sql. Do not create another duplicate index for it.

-- Book list: filter by status, paginate by book_id.
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
ON borrow_record(user_id, borrow_time, borrow_id);

-- User borrow history with status filtering:
-- WHERE user_id = ? AND borrow_status = ? ORDER BY borrow_time DESC.
CREATE INDEX idx_borrow_user_status_time
ON borrow_record(user_id, borrow_status, borrow_time, borrow_id);

-- Agent operation audit: query operation timeline by user.
CREATE INDEX idx_agent_log_user_time
ON agent_operation_log(user_id, create_time, log_id);
