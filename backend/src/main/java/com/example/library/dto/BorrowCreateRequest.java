package com.example.library.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class BorrowCreateRequest {

    @NotNull(message = "userId cannot be null")
    private Integer userId;

    @NotNull(message = "bookId cannot be null")
    private Integer bookId;

    private LocalDateTime dueReturnTime;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public LocalDateTime getDueReturnTime() {
        return dueReturnTime;
    }

    public void setDueReturnTime(LocalDateTime dueReturnTime) {
        this.dueReturnTime = dueReturnTime;
    }
}
