package com.example.library.vo;

public class AgentActionVO {

    private String type;
    private String label;
    private Integer bookId;
    private Integer borrowId;
    private String query;

    public AgentActionVO() {
    }

    public AgentActionVO(String type, String label, Integer bookId, Integer borrowId) {
        this.type = type;
        this.label = label;
        this.bookId = bookId;
        this.borrowId = borrowId;
    }

    public AgentActionVO(String type, String label, Integer bookId, Integer borrowId, String query) {
        this.type = type;
        this.label = label;
        this.bookId = bookId;
        this.borrowId = borrowId;
        this.query = query;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public Integer getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Integer borrowId) {
        this.borrowId = borrowId;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
