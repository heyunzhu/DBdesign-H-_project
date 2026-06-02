USE library;

DROP TABLE IF EXISTS agent_operation_log;
DROP TABLE IF EXISTS borrow_record;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS book_type;
DROP TABLE IF EXISTS author;
DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;

CREATE TABLE sys_role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_desc VARCHAR(200),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sys_permission (
    permission_id INT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(50) NOT NULL UNIQUE,
    permission_code VARCHAR(100) NOT NULL UNIQUE,
    permission_desc VARCHAR(200)
);

CREATE TABLE sys_user (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    user_no VARCHAR(20) NOT NULL UNIQUE,
    user_name VARCHAR(50) NOT NULL,
    password_hash VARCHAR(64) NOT NULL DEFAULT '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
    phone VARCHAR(20),
    dept_name VARCHAR(100),
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    account_status TINYINT NOT NULL DEFAULT 1,
    role_id INT NOT NULL,
    CONSTRAINT fk_user_role
        FOREIGN KEY (role_id) REFERENCES sys_role(role_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_user_status
        CHECK (account_status IN (0, 1))
);

CREATE TABLE sys_role_permission (
    rp_id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    CONSTRAINT fk_rp_role
        FOREIGN KEY (role_id) REFERENCES sys_role(role_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_rp_permission
        FOREIGN KEY (permission_id) REFERENCES sys_permission(permission_id)
        ON DELETE RESTRICT,
    CONSTRAINT uq_role_permission UNIQUE (role_id, permission_id)
);

CREATE TABLE author (
    author_id INT PRIMARY KEY AUTO_INCREMENT,
    author_name VARCHAR(50) NOT NULL,
    nationality VARCHAR(50),
    author_intro VARCHAR(500)
);

CREATE TABLE book_type (
    type_id INT PRIMARY KEY AUTO_INCREMENT,
    type_name VARCHAR(50) NOT NULL UNIQUE,
    type_desc VARCHAR(200)
);

CREATE TABLE book (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) NOT NULL UNIQUE,
    book_name VARCHAR(100) NOT NULL,
    author_id INT NOT NULL,
    type_id INT NOT NULL,
    publisher VARCHAR(100),
    publish_date DATE,
    book_status TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_book_author
        FOREIGN KEY (author_id) REFERENCES author(author_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_book_type
        FOREIGN KEY (type_id) REFERENCES book_type(type_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_book_status
        CHECK (book_status IN (0, 1, 2))
);

CREATE TABLE borrow_record (
    borrow_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_return_time DATETIME NOT NULL,
    actual_return_time DATETIME,
    borrow_status TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_borrow_user
        FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_borrow_book
        FOREIGN KEY (book_id) REFERENCES book(book_id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_borrow_status
        CHECK (borrow_status IN (0, 1, 2)),
    CONSTRAINT chk_due_after_borrow
        CHECK (due_return_time > borrow_time),
    CONSTRAINT chk_return_time_logic
        CHECK (
            (borrow_status IN (0, 2) AND actual_return_time IS NULL)
            OR
            (borrow_status = 1 AND actual_return_time IS NOT NULL AND actual_return_time >= borrow_time)
        )
);

CREATE TABLE agent_operation_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    action_type VARCHAR(30) NOT NULL,
    target_book_id INT,
    target_borrow_id INT,
    user_message VARCHAR(1000),
    tool_name VARCHAR(80) NOT NULL,
    tool_arguments VARCHAR(1000),
    result_success TINYINT NOT NULL,
    result_message VARCHAR(500),
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agent_log_user
        FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_agent_log_book
        FOREIGN KEY (target_book_id) REFERENCES book(book_id)
        ON DELETE SET NULL,
    CONSTRAINT fk_agent_log_borrow
        FOREIGN KEY (target_borrow_id) REFERENCES borrow_record(borrow_id)
        ON DELETE SET NULL,
    CONSTRAINT chk_agent_log_success
        CHECK (result_success IN (0, 1))
);
