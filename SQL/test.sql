USE library;

-- 1. 测试 user_no 唯一约束：重复学号/工号，预期失败
INSERT INTO sys_user (user_no, user_name, role_id)
VALUES ('20240001', '重复用户', 1);


-- 2. 测试 account_status 检查约束：账号状态只能是 0 或 1，预期失败
INSERT INTO sys_user (user_no, user_name, account_status, role_id)
VALUES ('20249999', '非法状态用户', 3, 1);


-- 3. 测试外键约束：不存在的 role_id，预期失败
INSERT INTO sys_user (user_no, user_name, role_id)
VALUES ('20248888', '非法角色用户', 999);


-- 4. 测试 role-permission 唯一约束：重复授权，预期失败
INSERT INTO sys_role_permission (role_id, permission_id)
VALUES (1, 1);


-- 5. 测试 book_status 检查约束：图书状态只能是 0、1、2，预期失败
UPDATE book
SET book_status = 9
WHERE book_id = 1;


-- 6. 测试 borrow_status 检查约束：借阅状态只能是 0、1、2，预期失败
UPDATE borrow_record
SET borrow_status = 8
WHERE borrow_id = 1;


-- 7. 测试还书时间逻辑：已归还时 actual_return_time 不能为空，预期失败
UPDATE borrow_record
SET borrow_status = 1,
    actual_return_time = NULL
WHERE borrow_id = 1;


-- 8. 测试应归还时间逻辑：应归还时间必须晚于借阅时间，预期失败
INSERT INTO borrow_record (
    user_id,
    book_id,
    borrow_time,
    due_return_time,
    borrow_status
)
VALUES (
    1,
    1,
    '2026-05-01 10:00:00',
    '2026-04-01 10:00:00',
    0
);


-- 9. 测试删除被引用的角色：已有用户关联时不允许删除，预期失败
DELETE FROM sys_role
WHERE role_id = 1;


-- 10. 测试删除被引用的图书：已有借阅记录时不允许删除，预期失败
DELETE FROM book
WHERE book_id = 4;
