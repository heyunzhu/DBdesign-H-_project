package com.example.library.agent;

import com.example.library.vo.BookDetailVO;
import com.example.library.vo.BorrowRecordVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AgentConversationMemory {

    private final Map<Integer, PendingAction> pendingActions = new ConcurrentHashMap<>();

    public void rememberBorrowOptions(Integer userId, List<BookDetailVO> books) {
        if (userId != null && books != null && !books.isEmpty()) {
            pendingActions.put(userId, PendingAction.borrow(books));
        }
    }

    public void rememberReturnOptions(Integer userId, List<BorrowRecordVO> records) {
        if (userId != null && records != null && !records.isEmpty()) {
            pendingActions.put(userId, PendingAction.returnBook(records));
        }
    }

    public PendingAction get(Integer userId) {
        return userId == null ? null : pendingActions.get(userId);
    }

    public void clear(Integer userId) {
        if (userId != null) {
            pendingActions.remove(userId);
        }
    }

    public static class PendingAction {

        private final String type;
        private final List<BookDetailVO> books;
        private final List<BorrowRecordVO> borrowRecords;

        private PendingAction(String type, List<BookDetailVO> books, List<BorrowRecordVO> borrowRecords) {
            this.type = type;
            this.books = books;
            this.borrowRecords = borrowRecords;
        }

        public static PendingAction borrow(List<BookDetailVO> books) {
            return new PendingAction("borrow", List.copyOf(books), List.of());
        }

        public static PendingAction returnBook(List<BorrowRecordVO> records) {
            return new PendingAction("return", List.of(), List.copyOf(records));
        }

        public String getType() {
            return type;
        }

        public List<BookDetailVO> getBooks() {
            return books;
        }

        public List<BorrowRecordVO> getBorrowRecords() {
            return borrowRecords;
        }
    }
}
