package com.example.library.vo;

import java.util.ArrayList;
import java.util.List;

public class AgentChatResponse {

    private String reply;
    private List<BookDetailVO> books = new ArrayList<>();
    private List<BorrowRecordVO> borrowRecords = new ArrayList<>();
    private List<AgentActionVO> actions = new ArrayList<>();

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<BookDetailVO> getBooks() {
        return books;
    }

    public void setBooks(List<BookDetailVO> books) {
        this.books = books;
    }

    public List<BorrowRecordVO> getBorrowRecords() {
        return borrowRecords;
    }

    public void setBorrowRecords(List<BorrowRecordVO> borrowRecords) {
        this.borrowRecords = borrowRecords;
    }

    public List<AgentActionVO> getActions() {
        return actions;
    }

    public void setActions(List<AgentActionVO> actions) {
        this.actions = actions;
    }
}
