package com.example.library.agent;

import com.example.library.vo.AgentActionVO;
import com.example.library.vo.BookDetailVO;
import com.example.library.vo.BorrowRecordVO;

import java.util.ArrayList;
import java.util.List;

public class AgentToolContext {

    private final List<BookDetailVO> books = new ArrayList<>();
    private final List<BorrowRecordVO> borrowRecords = new ArrayList<>();
    private final List<AgentActionVO> actions = new ArrayList<>();

    public List<BookDetailVO> getBooks() {
        return books;
    }

    public List<BorrowRecordVO> getBorrowRecords() {
        return borrowRecords;
    }

    public List<AgentActionVO> getActions() {
        return actions;
    }
}
