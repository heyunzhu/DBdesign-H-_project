USE library;

-- 1.查询所有可以借阅的图书
SELECT
    book_id,
    isbn,
    book_name,
    publisher,
    publish_date
FROM book
WHERE book_status = 0;

-- 2.按书名关键字查询图书
SELECT
    book_id,
    isbn,
    book_name,
    publisher,
    publish_date,
    book_status
FROM book
WHERE book_name LIKE '%数据库%';

-- 3.查询正常状态用户
SELECT
    book_id,
    isbn,
    book_name,
    publisher,
    publish_date,
    book_status
FROM book
WHERE book_name LIKE '%数据库%';

-- 4.查询图书详细信息
SELECT
    b.book_id,
    b.book_name,
    b.isbn,
    a.author_name,
    t.type_name,
    b.publisher,
    b.publish_date,
    CASE b.book_status
        WHEN 0 THEN '在馆可借'
        WHEN 1 THEN '借出中'
        WHEN 2 THEN '停用/下架'
    END AS book_status_text
FROM book b
JOIN author a ON b.author_id = a.author_id
JOIN book_type t ON b.type_id = t.type_id;

-- 5.查询用户借阅历史
SELECT
    br.borrow_id,
    u.user_name,
    u.user_no,
    b.book_name,
    br.borrow_time,
    br.due_return_time,
    br.actual_return_time,
    CASE br.borrow_status
        WHEN 0 THEN '未归还'
        WHEN 1 THEN '已归还'
        WHEN 2 THEN '逾期'
    END AS borrow_status_text
FROM borrow_record br
JOIN sys_user u ON br.user_id = u.user_id
JOIN book b ON br.book_id = b.book_id
ORDER BY br.borrow_time DESC;

-- 6.统计每个分类下图书的数量
SELECT
    t.type_name,
    COUNT(b.book_id) AS book_count
FROM book_type t
LEFT JOIN book b ON t.type_id = b.type_id
GROUP BY t.type_id, t.type_name
ORDER BY book_count DESC;

-- 7.查询当前逾期未还记录
SELECT
    br.borrow_id,
    u.user_name,
    u.user_no,
    b.book_name,
    br.borrow_time,
    br.due_return_time,
    DATEDIFF(CURRENT_DATE, br.due_return_time) AS overdue_days
FROM borrow_record br
JOIN sys_user u ON br.user_id = u.user_id
JOIN book b ON br.book_id = b.book_id
WHERE br.borrow_status IN (0, 2)
  AND br.actual_return_time IS NULL
  AND br.due_return_time < CURRENT_TIMESTAMP
ORDER BY overdue_days DESC;

-- 8.统计借阅数量最多的图书分类
SELECT
    t.type_name,
    COUNT(br.borrow_id) AS borrow_count
FROM borrow_record br
JOIN book b ON br.book_id = b.book_id
JOIN book_type t ON b.type_id = t.type_id
GROUP BY t.type_id, t.type_name
ORDER BY borrow_count DESC;
