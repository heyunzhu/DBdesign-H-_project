package com.example.library.vo;

import java.time.LocalDateTime;

public class BorrowRecordVO {

    private Integer borrowId;
    private Integer userId;
    private String userNo;
    private String userName;
    private Integer bookId;
    private String bookName;
    private String isbn;
    private LocalDateTime borrowTime;
    private LocalDateTime dueReturnTime;
    private LocalDateTime actualReturnTime;
    private Integer borrowStatus;
    private String borrowStatusText;
    private Integer overdueDays;

    public Integer getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Integer borrowId) {
        this.borrowId = borrowId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public LocalDateTime getBorrowTime() {
        return borrowTime;
    }

    public void setBorrowTime(LocalDateTime borrowTime) {
        this.borrowTime = borrowTime;
    }

    public LocalDateTime getDueReturnTime() {
        return dueReturnTime;
    }

    public void setDueReturnTime(LocalDateTime dueReturnTime) {
        this.dueReturnTime = dueReturnTime;
    }

    public LocalDateTime getActualReturnTime() {
        return actualReturnTime;
    }

    public void setActualReturnTime(LocalDateTime actualReturnTime) {
        this.actualReturnTime = actualReturnTime;
    }

    public Integer getBorrowStatus() {
        return borrowStatus;
    }

    public void setBorrowStatus(Integer borrowStatus) {
        this.borrowStatus = borrowStatus;
    }

    public String getBorrowStatusText() {
        return borrowStatusText;
    }

    public void setBorrowStatusText(String borrowStatusText) {
        this.borrowStatusText = borrowStatusText;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }
}
