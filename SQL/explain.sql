USE library;

-- 1. Verify book list filtering and joins.
EXPLAIN
SELECT
    b.book_id,
    b.book_name,
    a.author_name,
    t.type_name,
    b.book_status
FROM book b
JOIN author a ON b.author_id = a.author_id
JOIN book_type t ON b.type_id = t.type_id
WHERE b.book_status = 0
ORDER BY b.book_id DESC
LIMIT 0, 10;

-- 2. Verify user filtering by role and account status.
EXPLAIN
SELECT
    u.user_id,
    u.user_no,
    u.user_name,
    r.role_name,
    u.account_status
FROM sys_user u
JOIN sys_role r ON u.role_id = r.role_id
WHERE u.role_id = 1
  AND u.account_status = 1
ORDER BY u.user_id DESC
LIMIT 0, 10;

-- 3. Verify user borrow history query.
EXPLAIN
SELECT
    br.borrow_id,
    u.user_name,
    b.book_name,
    br.borrow_time,
    br.borrow_status
FROM borrow_record br
JOIN sys_user u ON br.user_id = u.user_id
JOIN book b ON br.book_id = b.book_id
WHERE br.user_id = 1
ORDER BY br.borrow_time DESC
LIMIT 0, 10;

-- 4. Verify overdue/unreturned record query.
EXPLAIN
SELECT
    br.borrow_id,
    u.user_name,
    b.book_name,
    br.due_return_time
FROM borrow_record br
JOIN sys_user u ON br.user_id = u.user_id
JOIN book b ON br.book_id = b.book_id
WHERE br.borrow_status IN (0, 2)
  AND br.actual_return_time IS NULL
  AND br.due_return_time < CURRENT_TIMESTAMP
ORDER BY br.due_return_time;

-- 5. Verify category book count query.
EXPLAIN
SELECT
    t.type_name,
    COUNT(b.book_id) AS book_count
FROM book_type t
LEFT JOIN book b ON t.type_id = b.type_id
GROUP BY t.type_id, t.type_name
ORDER BY book_count DESC;
