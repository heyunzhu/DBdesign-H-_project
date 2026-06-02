package com.example.library.agent;

import com.example.library.auth.AuthContext;
import com.example.library.auth.LoginUser;
import com.example.library.common.BusinessException;
import com.example.library.dto.BorrowCreateRequest;
import com.example.library.entity.AgentOperationLog;
import com.example.library.mapper.AgentOperationLogMapper;
import com.example.library.service.BookService;
import com.example.library.service.BorrowService;
import com.example.library.vo.AgentActionVO;
import com.example.library.vo.BookDetailVO;
import com.example.library.vo.BorrowRecordVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ReaderAgentToolExecutor {

    private static final int BOOK_STATUS_AVAILABLE = 0;
    private static final int BORROW_STATUS_RETURNED = 1;

    private final BookService bookService;
    private final BorrowService borrowService;
    private final AgentOperationLogMapper agentOperationLogMapper;
    private final ObjectMapper objectMapper;

    public ReaderAgentToolExecutor(BookService bookService,
                                   BorrowService borrowService,
                                   AgentOperationLogMapper agentOperationLogMapper,
                                   ObjectMapper objectMapper) {
        this.bookService = bookService;
        this.borrowService = borrowService;
        this.agentOperationLogMapper = agentOperationLogMapper;
        this.objectMapper = objectMapper;
    }

    public String execute(String toolName, String argumentsJson, AgentToolContext context) {
        Map<String, Object> arguments = parseArguments(argumentsJson);
        Map<String, Object> result = executeForMap(toolName, arguments, context);
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            return "{\"success\":false,\"message\":\"tool result serialization failed\"}";
        }
    }

    public Map<String, Object> executeForMap(String toolName, Map<String, Object> arguments, AgentToolContext context) {
        try {
            return switch (toolName) {
                case "search_books" -> searchBooks(arguments, context);
                case "borrow_book" -> borrowBook(arguments, context);
                case "borrow_book_by_query" -> borrowBookByQuery(arguments, context);
                case "list_my_borrows" -> listMyBorrows(arguments, context);
                case "return_my_book" -> returnMyBook(arguments, context);
                case "return_book_by_query" -> returnBookByQuery(arguments, context);
                default -> Map.of("success", false, "message", "unknown tool: " + toolName);
            };
        } catch (BusinessException ex) {
            if (isAuditedTool(toolName)) {
                writeAudit(actionType(toolName), toolName, arguments, false, ex.getMessage(),
                        intValue(arguments.get("bookId")), intValue(arguments.get("borrowId")));
            }
            return Map.of("success", false, "message", ex.getMessage());
        }
    }

    private Map<String, Object> searchBooks(Map<String, Object> arguments, AgentToolContext context) {
        String keyword = stringValue(arguments.get("keyword"));
        boolean onlyAvailable = booleanValue(arguments.get("onlyAvailable"));
        Integer status = onlyAvailable ? BOOK_STATUS_AVAILABLE : null;
        List<BookDetailVO> books = keyword.isBlank()
                ? bookService.listBooks("", status).stream().limit(8).toList()
                : findCandidateBooks(keyword, status);
        context.getBooks().clear();
        context.getBooks().addAll(books);
        context.getActions().clear();
        return Map.of(
                "success", true,
                "books", books,
                "message", books.isEmpty() ? "没有找到匹配图书" : "找到 " + books.size() + " 本图书"
        );
    }

    private Map<String, Object> borrowBook(Map<String, Object> arguments, AgentToolContext context) {
        Integer bookId = intValue(arguments.get("bookId"));
        if (bookId == null) {
            writeAudit("borrow", "borrow_book", arguments, false,
                    "execute: bookId is required", null, null);
            return Map.of("success", false, "message", "bookId is required");
        }
        LoginUser user = AuthContext.get();
        BorrowCreateRequest request = new BorrowCreateRequest();
        request.setUserId(user.getUserId());
        request.setBookId(bookId);
        Integer borrowId = borrowService.borrowBook(request);
        writeAudit("borrow", "borrow_book", arguments, true, "execute: borrow success", bookId, borrowId);
        context.getActions().clear();
        return Map.of("success", true, "message", "借阅成功", "borrowId", borrowId);
    }

    private Map<String, Object> borrowBookByQuery(Map<String, Object> arguments, AgentToolContext context) {
        String query = stringValue(arguments.get("query"));
        if (query.isBlank()) {
            writeAudit("borrow", "borrow_book_by_query", arguments, false,
                    "prepare: query is required", null, null);
            return Map.of("success", false, "message", "query is required");
        }

        List<BookDetailVO> candidates = findCandidateBooks(query);
        context.getBooks().clear();
        context.getBooks().addAll(candidates);
        context.getActions().clear();

        if (candidates.isEmpty()) {
            writeAudit("borrow", "borrow_book_by_query", arguments, false,
                    "prepare: no available matching books", null, null);
            return Map.of("success", false, "message", "没有找到可借的匹配图书");
        }

        candidates.forEach(book -> context.getActions().add(new AgentActionVO(
                "borrow",
                "借阅《" + book.getBookName() + "》",
                book.getBookId(),
                null,
                book.getBookName()
        )));
        writeAudit("borrow", "borrow_book_by_query", arguments, true,
                "prepare: found " + candidates.size() + " candidate books", candidates.get(0).getBookId(), null);
        return Map.of(
                "success", false,
                "requiresConfirmation", true,
                "message", "confirm required before borrowing",
                "books", candidates
        );
    }

    private Map<String, Object> listMyBorrows(Map<String, Object> arguments, AgentToolContext context) {
        Integer status = intValue(arguments.get("status"));
        List<BorrowRecordVO> records = borrowService.listBorrowRecords(AuthContext.get().getUserId(), status)
                .stream()
                .limit(10)
                .toList();
        context.getBorrowRecords().clear();
        context.getBorrowRecords().addAll(records);
        context.getActions().clear();
        records.stream()
                .filter(record -> !Integer.valueOf(BORROW_STATUS_RETURNED).equals(record.getBorrowStatus()))
                .forEach(record -> context.getActions().add(new AgentActionVO(
                        "return",
                        "归还《" + record.getBookName() + "》",
                        null,
                        record.getBorrowId(),
                        record.getBookName()
                )));
        return Map.of(
                "success", true,
                "borrowRecords", records,
                "message", records.isEmpty() ? "当前没有借阅记录" : "找到 " + records.size() + " 条借阅记录"
        );
    }

    private Map<String, Object> returnMyBook(Map<String, Object> arguments, AgentToolContext context) {
        Integer borrowId = intValue(arguments.get("borrowId"));
        if (borrowId == null) {
            writeAudit("return", "return_my_book", arguments, false,
                    "execute: borrowId is required", null, null);
            return Map.of("success", false, "message", "borrowId is required");
        }
        BorrowRecordVO record = findMyBorrowRecord(borrowId);
        borrowService.returnBook(borrowId);
        writeAudit("return", "return_my_book", arguments, true, "execute: return success",
                record == null ? null : record.getBookId(), borrowId);
        context.getActions().clear();
        return Map.of("success", true, "message", "归还成功");
    }

    private Map<String, Object> returnBookByQuery(Map<String, Object> arguments, AgentToolContext context) {
        String query = stringValue(arguments.get("query"));
        if (query.isBlank()) {
            writeAudit("return", "return_book_by_query", arguments, false,
                    "prepare: query is required", null, null);
            return Map.of("success", false, "message", "query is required");
        }

        List<BorrowRecordVO> candidates = borrowService.listBorrowRecords(AuthContext.get().getUserId(), null)
                .stream()
                .filter(record -> !Integer.valueOf(BORROW_STATUS_RETURNED).equals(record.getBorrowStatus()))
                .filter(record -> scoreBorrowRecord(query, record) > 0)
                .sorted(Comparator.comparingInt((BorrowRecordVO record) -> scoreBorrowRecord(query, record)).reversed())
                .limit(8)
                .toList();
        context.getBorrowRecords().clear();
        context.getBorrowRecords().addAll(candidates);
        context.getActions().clear();

        if (candidates.isEmpty()) {
            writeAudit("return", "return_book_by_query", arguments, false,
                    "prepare: no matching active borrow records", null, null);
            return Map.of("success", false, "message", "没有找到你正在借阅的匹配图书");
        }

        candidates.forEach(record -> context.getActions().add(new AgentActionVO(
                "return",
                "归还《" + record.getBookName() + "》",
                null,
                record.getBorrowId(),
                record.getBookName()
        )));
        BorrowRecordVO first = candidates.get(0);
        writeAudit("return", "return_book_by_query", arguments, true,
                "prepare: found " + candidates.size() + " candidate borrow records", first.getBookId(), first.getBorrowId());
        return Map.of(
                "success", false,
                "requiresConfirmation", true,
                "message", "confirm required before returning",
                "borrowRecords", candidates
        );
    }

    private Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(argumentsJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            return new LinkedHashMap<>();
        }
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private boolean booleanValue(Object value) {
        return value instanceof Boolean bool && bool;
    }

    private Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private List<BookDetailVO> findCandidateBooks(String query) {
        return findCandidateBooks(query, BOOK_STATUS_AVAILABLE);
    }

    private List<BookDetailVO> findCandidateBooks(String query, Integer status) {
        List<BookDetailVO> allCandidates = bookService.listBooks("", status)
                .stream()
                .filter(book -> scoreBook(query, book) > 0)
                .sorted(Comparator.comparingInt((BookDetailVO book) -> scoreBook(query, book)).reversed())
                .limit(8)
                .toList();
        List<BookDetailVO> exactMatches = allCandidates.stream()
                .filter(book -> expandedQueryTerms(query).stream()
                        .map(this::normalize)
                        .anyMatch(term -> term.equals(normalize(book.getBookName()))))
                .toList();
        if (!exactMatches.isEmpty()) {
            return exactMatches;
        }
        if (allCandidates.size() <= 1) {
            return allCandidates;
        }
        int topScore = scoreBook(query, allCandidates.get(0));
        int secondScore = scoreBook(query, allCandidates.get(1));
        if (topScore >= secondScore + 30) {
            return List.of(allCandidates.get(0));
        }
        return allCandidates;
    }

    private int scoreBook(String query, BookDetailVO book) {
        return expandedQueryTerms(query).stream()
                .map(this::normalize)
                .filter(term -> !term.isBlank())
                .mapToInt(term -> scoreBookTerm(term, book))
                .max()
                .orElse(0);
    }

    private int scoreBookTerm(String normalizedQuery, BookDetailVO book) {
        int score = 0;
        score += scoreField(normalizedQuery, book.getBookName(), 70);
        score += scoreField(normalizedQuery, book.getAuthorName(), 50);
        score += scoreField(normalizedQuery, book.getTypeName(), 30);
        score += scoreField(normalizedQuery, book.getIsbn(), 80);
        return score;
    }

    private int scoreBorrowRecord(String query, BorrowRecordVO record) {
        String normalizedQuery = normalize(query);
        int score = 0;
        score += scoreField(normalizedQuery, record.getBookName(), 70);
        score += scoreField(normalizedQuery, record.getIsbn(), 80);
        return score;
    }

    private int scoreField(String normalizedQuery, String value, int weight) {
        String normalizedValue = normalize(value);
        if (normalizedQuery.isBlank() || normalizedValue.isBlank()) {
            return 0;
        }
        if (normalizedQuery.equals(normalizedValue)) {
            return weight + 30;
        }
        if (normalizedQuery.contains(normalizedValue) || normalizedValue.contains(normalizedQuery)) {
            return weight;
        }
        int score = 0;
        for (String term : importantTerms(normalizedQuery)) {
            if (normalizedValue.contains(term)) {
                score += Math.max(10, weight / 3);
            }
        }
        return score;
    }

    private List<String> importantTerms(String normalizedQuery) {
        List<String> knownTerms = List.of(
                "数据结构", "算法", "数据库", "人工智能", "机器学习", "深度学习", "面向对象", "程序设计",
                "操作系统", "计算机网络", "计算机", "软件", "软件工程", "java", "python", "oop",
                "数学", "英语", "文学", "经济"
        );
        return knownTerms.stream()
                .map(this::normalize)
                .filter(term -> !term.isBlank() && normalizedQuery.contains(term))
                .toList();
    }

    private List<String> expandedQueryTerms(String query) {
        String normalizedQuery = normalize(query);
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        if (!query.isBlank()) {
            terms.add(query);
        }
        if (!normalizedQuery.isBlank()) {
            terms.add(normalizedQuery);
        }
        if (containsAnyNormalized(normalizedQuery, "oop", "objectoriented", "面向对象", "对象程序", "对象编程")) {
            terms.add("面向对象");
            terms.add("面向对象程序设计");
            terms.add("程序设计");
            terms.add("java");
            terms.add("软件工程");
            terms.add("软件设计");
        }
        if (containsAnyNormalized(normalizedQuery, "ai", "人工智能", "智能")) {
            terms.add("人工智能");
            terms.add("机器学习");
            terms.add("深度学习");
        }
        if (containsAnyNormalized(normalizedQuery, "db", "database", "数据库")) {
            terms.add("数据库");
            terms.add("mysql");
        }
        if (containsAnyNormalized(normalizedQuery, "os", "operatingsystem", "操作系统")) {
            terms.add("操作系统");
        }
        if (containsAnyNormalized(normalizedQuery, "network", "计算机网络", "网络")) {
            terms.add("计算机网络");
        }
        return terms.stream()
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .toList();
    }

    private boolean containsAnyNormalized(String text, String... values) {
        for (String value : values) {
            if (text.contains(normalize(value))) {
                return true;
            }
        }
        return false;
    }

    private BorrowRecordVO findMyBorrowRecord(Integer borrowId) {
        if (borrowId == null) {
            return null;
        }
        return borrowService.listBorrowRecords(AuthContext.get().getUserId(), null)
                .stream()
                .filter(record -> borrowId.equals(record.getBorrowId()))
                .findFirst()
                .orElse(null);
    }

    private boolean isAuditedTool(String toolName) {
        return "borrow_book".equals(toolName)
                || "return_my_book".equals(toolName)
                || "borrow_book_by_query".equals(toolName)
                || "return_book_by_query".equals(toolName);
    }

    private String actionType(String toolName) {
        if ("return_my_book".equals(toolName) || "return_book_by_query".equals(toolName)) {
            return "return";
        }
        return "borrow";
    }

    private void writeAudit(String actionType,
                            String toolName,
                            Map<String, Object> arguments,
                            boolean success,
                            String resultMessage,
                            Integer bookId,
                            Integer borrowId) {
        LoginUser user = AuthContext.get();
        if (user == null) {
            return;
        }
        AgentOperationLog log = new AgentOperationLog();
        log.setUserId(user.getUserId());
        log.setActionType(actionType);
        log.setTargetBookId(bookId);
        log.setTargetBorrowId(borrowId);
        log.setUserMessage(truncate(stringValue(arguments.get("userMessage")), 1000));
        log.setToolName(toolName);
        log.setToolArguments(truncate(toJson(arguments), 1000));
        log.setResultSuccess(success ? 1 : 0);
        log.setResultMessage(truncate(resultMessage, 500));
        try {
            agentOperationLogMapper.insertAgentOperationLog(log);
        } catch (DataAccessException ignored) {
            // Audit failure should not block the reader's borrowing or returning flow.
        }
    }

    private String toJson(Map<String, Object> arguments) {
        try {
            return objectMapper.writeValueAsString(arguments);
        } catch (JsonProcessingException ex) {
            return "{}";
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String normalize(String value) {
        return Objects.toString(value, "")
                .toLowerCase()
                .replaceAll("[《》“”\"'\\s，。！？、,.!?：:()（）\\[\\]【】]", "")
                .replace("我想", "")
                .replace("帮我", "")
                .replace("借阅", "")
                .replace("借", "")
                .replace("归还", "")
                .replace("还", "")
                .replace("一本", "")
                .replace("这本", "")
                .replace("那本", "")
                .replace("相关", "")
                .replace("方面", "")
                .replace("图书", "")
                .replace("书", "")
                .trim();
    }
}
