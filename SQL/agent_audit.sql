USE library;

CREATE TABLE IF NOT EXISTS agent_operation_log (
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

-- Indexes for this table are managed in indexes.sql to avoid duplicate
-- index creation when running the full database initialization flow.
