package com.example.library.service.impl;

import com.example.library.agent.AgentConversationMemory;
import com.example.library.agent.AgentToolContext;
import com.example.library.agent.DeepSeekClient;
import com.example.library.agent.DeepSeekProperties;
import com.example.library.agent.ReaderAgentIntentRouter;
import com.example.library.agent.ReaderAgentResponseFormatter;
import com.example.library.agent.ReaderAgentSkill;
import com.example.library.agent.ReaderAgentToolDefinitionProvider;
import com.example.library.agent.ReaderAgentToolExecutor;
import com.example.library.auth.AuthContext;
import com.example.library.dto.AgentChatRequest;
import com.example.library.service.ReaderAgentService;
import com.example.library.vo.AgentChatResponse;
import com.example.library.vo.BookDetailVO;
import com.example.library.vo.BorrowRecordVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReaderAgentServiceImpl implements ReaderAgentService {

    private final DeepSeekClient deepSeekClient;
    private final DeepSeekProperties deepSeekProperties;
    private final ReaderAgentToolExecutor toolExecutor;
    private final AgentConversationMemory conversationMemory;
    private final ReaderAgentIntentRouter intentRouter;
    private final ReaderAgentResponseFormatter responseFormatter;
    private final ReaderAgentToolDefinitionProvider toolDefinitionProvider;

    public ReaderAgentServiceImpl(DeepSeekClient deepSeekClient,
                                  DeepSeekProperties deepSeekProperties,
                                  ReaderAgentToolExecutor toolExecutor,
                                  AgentConversationMemory conversationMemory,
                                  ReaderAgentIntentRouter intentRouter,
                                  ReaderAgentResponseFormatter responseFormatter,
                                  ReaderAgentToolDefinitionProvider toolDefinitionProvider) {
        this.deepSeekClient = deepSeekClient;
        this.deepSeekProperties = deepSeekProperties;
        this.toolExecutor = toolExecutor;
        this.conversationMemory = conversationMemory;
        this.intentRouter = intentRouter;
        this.responseFormatter = responseFormatter;
        this.toolDefinitionProvider = toolDefinitionProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AgentChatResponse chat(AgentChatRequest request) {
        AgentToolContext toolContext = new AgentToolContext();
        String message = request.getMessage();

        AgentChatResponse cancelResponse = tryHandleCancelIntent(message, toolContext);
        if (cancelResponse != null) {
            return cancelResponse;
        }

        AgentChatResponse confirmationResponse = tryHandlePendingConfirmation(message, toolContext);
        if (confirmationResponse != null) {
            return confirmationResponse;
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", ReaderAgentSkill.SYSTEM_PROMPT));
        messages.add(Map.of("role", "user", "content", message));

        Map<String, Object> firstResponse;
        try {
            firstResponse = deepSeekClient.chat(chatRequest(messages, toolDefinitionProvider.definitions(), "auto"));
        } catch (Exception ex) {
            AgentChatResponse fallbackResponse = tryHandleLocalFallback(message, toolContext);
            if (fallbackResponse != null) {
                return fallbackResponse;
            }
            return buildResponse("我这边暂时连不上外部模型，不过基础借书功能还在。你可以告诉我关键词，比如“数据库相关的书”或“我现在借了哪些书”。", toolContext);
        }

        Map<String, Object> assistantMessage = extractMessage(firstResponse);
        List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) assistantMessage.get("tool_calls");
        String reply;

        if (toolCalls == null || toolCalls.isEmpty()) {
            reply = stringValue(assistantMessage.get("content"));
            if (intentRouter.containsToolCallMarkup(reply)) {
                AgentChatResponse fallbackResponse = tryHandleLocalFallback(message, toolContext);
                if (fallbackResponse != null) {
                    return fallbackResponse;
                }
                return buildResponse("我需要先查询馆藏，不能直接把内部工具调用内容展示给你。你可以换个具体主题或书名再试一次。", toolContext);
            }
            if (shouldUseToolFor(message)) {
                AgentChatResponse fallbackResponse = tryHandleLocalFallback(message, toolContext);
                if (fallbackResponse != null) {
                    return fallbackResponse;
                }
            }
        } else {
            messages.add(assistantMessage);
            for (Map<String, Object> toolCall : toolCalls) {
                Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                String toolName = stringValue(function.get("name"));
                String arguments = stringValue(function.get("arguments"));
                String toolResult = toolExecutor.execute(toolName, arguments, toolContext);
                messages.add(Map.of(
                        "role", "tool",
                        "tool_call_id", toolCall.get("id"),
                        "content", toolResult
                ));
            }
            try {
                Map<String, Object> finalResponse = deepSeekClient.chat(chatRequest(messages, toolDefinitionProvider.definitions(), "none"));
                reply = stringValue(extractMessage(finalResponse).get("content"));
                if (intentRouter.containsToolCallMarkup(reply)) {
                    AgentChatResponse fallbackResponse = buildSafeToolResponse(message, toolContext);
                    if (fallbackResponse != null) {
                        return fallbackResponse;
                    }
                    return buildResponse("我已经查询了工具结果，但不能把内部工具调用内容展示给你。你可以换个具体主题或书名再试一次。", toolContext);
                }
            } catch (Exception ex) {
                AgentChatResponse fallbackResponse = buildSafeToolResponse(message, toolContext);
                if (fallbackResponse != null) {
                    return fallbackResponse;
                }
                reply = "我已经完成了操作。你可以查看下方结果，或继续告诉我想查哪类书。";
            }
        }

        return buildResponse(reply == null || reply.isBlank() ? "我已经处理好了。" : reply, toolContext);
    }

    private AgentChatResponse tryHandleBusinessIntent(String message, AgentToolContext context) {
        Integer bookId = intentRouter.extractBookId(message);
        if (bookId != null && intentRouter.containsAny(message, "借", "借阅")) {
            Map<String, Object> result = toolExecutor.executeForMap("borrow_book", Map.of(
                    "bookId", bookId,
                    "userMessage", message
            ), context);
            if (Boolean.TRUE.equals(result.get("success"))) {
                conversationMemory.clear(currentUserId());
            }
            return buildResponse(Boolean.TRUE.equals(result.get("success"))
                    ? "借阅成功。你可以在“借阅管理”里查看这本书的应还时间。"
                    : "借阅失败：" + stringValue(result.get("message")), context);
        }

        Integer borrowId = intentRouter.extractBorrowId(message);
        if (borrowId != null && intentRouter.containsAny(message, "还", "归还")) {
            Map<String, Object> result = toolExecutor.executeForMap("return_my_book", Map.of(
                    "borrowId", borrowId,
                    "userMessage", message
            ), context);
            if (Boolean.TRUE.equals(result.get("success"))) {
                conversationMemory.clear(currentUserId());
            }
            return buildResponse(Boolean.TRUE.equals(result.get("success"))
                    ? "归还成功。"
                    : "归还失败：" + stringValue(result.get("message")), context);
        }

        if (intentRouter.isReturnByQueryIntent(message)) {
            String query = intentRouter.extractActionQuery(message, "归还", "还");
            Map<String, Object> result = toolExecutor.executeForMap("return_book_by_query", Map.of(
                    "query", query,
                    "userMessage", message
            ), context);
            return buildToolResponse("归还成功。", "归还", query, result, context);
        }

        if (intentRouter.isBorrowByQueryIntent(message)) {
            String query = intentRouter.extractActionQuery(message, "借阅", "借");
            Map<String, Object> result = toolExecutor.executeForMap("borrow_book_by_query", Map.of(
                    "query", query,
                    "userMessage", message
            ), context);
            return buildToolResponse("借阅成功。你可以在“借阅管理”里查看这本书的应还时间。", "借阅", query, result, context);
        }

        if (intentRouter.isMyBorrowIntent(message)) {
            Map<String, Object> arguments = new LinkedHashMap<>();
            Integer status = intentRouter.inferBorrowStatus(message);
            if (status != null) {
                arguments.put("status", status);
            }
            toolExecutor.executeForMap("list_my_borrows", arguments, context);
            return buildResponse(responseFormatter.formatBorrowReply(context.getBorrowRecords()), context);
        }

        return null;
    }

    private AgentChatResponse tryHandlePendingConfirmation(String message, AgentToolContext context) {
        AgentConversationMemory.PendingAction pendingAction = conversationMemory.get(currentUserId());
        if (pendingAction == null || !intentRouter.isConfirmationIntent(message)) {
            return null;
        }

        if ("borrow".equals(pendingAction.getType())) {
            List<BookDetailVO> books = pendingAction.getBooks();
            Integer index = intentRouter.resolveCandidateIndex(message, books.size());
            if (index == null) {
                return buildResponse("我还不确定你要借哪一本。你可以说“第一本”“第二本”，或者点击下面的借阅按钮。", context);
            }
            BookDetailVO book = books.get(index);
            Map<String, Object> result = toolExecutor.executeForMap("borrow_book", Map.of(
                    "bookId", book.getBookId(),
                    "userMessage", message
            ), context);
            conversationMemory.clear(currentUserId());
            return buildResponse(Boolean.TRUE.equals(result.get("success"))
                    ? "已为你借阅《" + book.getBookName() + "》。你可以在“借阅管理”里查看应还时间。"
                    : "借阅失败：" + stringValue(result.get("message")), context);
        }

        if ("return".equals(pendingAction.getType())) {
            List<BorrowRecordVO> records = pendingAction.getBorrowRecords();
            Integer index = intentRouter.resolveCandidateIndex(message, records.size());
            if (index == null) {
                return buildResponse("我还不确定你要归还哪一本。你可以说“第一本”“第二本”，或者点击下面的归还按钮。", context);
            }
            BorrowRecordVO record = records.get(index);
            Map<String, Object> result = toolExecutor.executeForMap("return_my_book", Map.of(
                    "borrowId", record.getBorrowId(),
                    "userMessage", message
            ), context);
            conversationMemory.clear(currentUserId());
            return buildResponse(Boolean.TRUE.equals(result.get("success"))
                    ? "已为你归还《" + record.getBookName() + "》。"
                    : "归还失败：" + stringValue(result.get("message")), context);
        }

        return null;
    }

    private AgentChatResponse tryHandleCancelIntent(String message, AgentToolContext context) {
        if (!intentRouter.isCancelIntent(message)) {
            return null;
        }
        conversationMemory.clear(currentUserId());
        return buildResponse("好的，那我先不帮你借书。你要是只是想聊复习计划、找学习方向，或者之后再查馆藏，都可以直接跟我说。", context);
    }

    private AgentChatResponse buildToolResponse(String successReply,
                                                String actionName,
                                                String query,
                                                Map<String, Object> result,
                                                AgentToolContext context) {
        if (Boolean.TRUE.equals(result.get("success"))) {
            conversationMemory.clear(currentUserId());
            return buildResponse(successReply, context);
        }
        if (Boolean.TRUE.equals(result.get("requiresConfirmation"))) {
            if (!context.getBooks().isEmpty()) {
                return buildResponse(responseFormatter.formatBookReply(query, context.getBooks())
                        + "\n\n为避免误操作，我还没有真正借出图书。请你回复“确认”“第一本”或点击下方按钮后，我再执行借阅。", context);
            }
            if (!context.getBorrowRecords().isEmpty()) {
                return buildResponse(responseFormatter.formatBorrowReply(context.getBorrowRecords())
                        + "\n\n为避免误操作，我还没有真正归还图书。请你回复“确认”“第一本”或点击下方按钮后，我再执行归还。", context);
            }
        }
        if (!context.getBooks().isEmpty()) {
            return buildResponse(responseFormatter.formatBookReply(query, context.getBooks()), context);
        }
        if (!context.getBorrowRecords().isEmpty()) {
            return buildResponse(responseFormatter.formatBorrowReply(context.getBorrowRecords()), context);
        }
        return buildResponse(actionName + "失败：" + stringValue(result.get("message")), context);
    }

    private AgentChatResponse tryHandleFallbackIntent(String message, AgentToolContext context) {
        if (intentRouter.isGreetingIntent(message)) {
            return buildResponse("你好，我在。你可以像和图书馆老师说话一样告诉我想找什么书，我会先查馆藏，再帮你借。", context);
        }

        if (intentRouter.isBookSearchIntent(message)) {
            String keyword = intentRouter.extractBookKeyword(message);
            toolExecutor.executeForMap("search_books", Map.of(
                    "keyword", keyword,
                    "onlyAvailable", true
            ), context);
            return buildResponse(responseFormatter.formatBookReply(keyword, context.getBooks()), context);
        }

        return null;
    }

    private AgentChatResponse tryHandleLocalFallback(String message, AgentToolContext context) {
        AgentChatResponse businessResponse = tryHandleBusinessIntent(message, context);
        if (businessResponse != null) {
            return businessResponse;
        }

        AgentChatResponse catalogResponse = tryHandleCatalogRecommendation(message, context);
        if (catalogResponse != null) {
            return catalogResponse;
        }

        return tryHandleFallbackIntent(message, context);
    }

    private boolean shouldUseToolFor(String message) {
        boolean directBorrowById = intentRouter.extractBookId(message) != null
                && intentRouter.containsAny(message, "借", "借阅");
        boolean directReturnById = intentRouter.extractBorrowId(message) != null
                && intentRouter.containsAny(message, "还", "归还");
        return directBorrowById
                || directReturnById
                || intentRouter.isBorrowByQueryIntent(message)
                || intentRouter.isReturnByQueryIntent(message)
                || intentRouter.isMyBorrowIntent(message)
                || intentRouter.isCatalogRecommendationIntent(message)
                || intentRouter.isBookSearchIntent(message);
    }

    private AgentChatResponse buildSafeToolResponse(String message, AgentToolContext context) {
        if (!context.getBooks().isEmpty()) {
            String keyword = intentRouter.isCatalogRecommendationIntent(message)
                    ? intentRouter.extractCatalogKeyword(message)
                    : intentRouter.extractBookKeyword(message);
            String reply = responseFormatter.formatBookReply(keyword, context.getBooks());
            if (context.getActions().stream().anyMatch(action -> "borrow".equals(action.getType()))) {
                reply += "\n\n为避免误操作，我还没有真正借出图书。请你回复“确认”“第一本”或点击下方按钮后，我再执行借阅。";
            }
            return buildResponse(reply, context);
        }

        if (!context.getBorrowRecords().isEmpty()) {
            String reply = responseFormatter.formatBorrowReply(context.getBorrowRecords());
            if (context.getActions().stream().anyMatch(action -> "return".equals(action.getType()))) {
                reply += "\n\n为避免误操作，我还没有真正归还图书。请你回复“确认”“第一本”或点击下方按钮后，我再执行归还。";
            }
            return buildResponse(reply, context);
        }

        return null;
    }

    private AgentChatResponse tryHandleCatalogRecommendation(String message, AgentToolContext context) {
        if (!intentRouter.isCatalogRecommendationIntent(message)) {
            return null;
        }
        String keyword = intentRouter.extractCatalogKeyword(message);
        toolExecutor.executeForMap("search_books", Map.of(
                "keyword", keyword,
                "onlyAvailable", true
        ), context);
        return buildResponse(responseFormatter.formatBookReply(keyword, context.getBooks()), context);
    }

    private AgentChatResponse buildResponse(String reply, AgentToolContext context) {
        rememberPendingOptions(context);
        AgentChatResponse response = new AgentChatResponse();
        response.setReply(reply);
        response.setBooks(context.getBooks());
        response.setBorrowRecords(context.getBorrowRecords());
        response.setActions(context.getActions());
        return response;
    }

    private void rememberPendingOptions(AgentToolContext context) {
        if (context.getActions().isEmpty()) {
            return;
        }
        boolean hasBorrowAction = context.getActions().stream().anyMatch(action -> "borrow".equals(action.getType()));
        boolean hasReturnAction = context.getActions().stream().anyMatch(action -> "return".equals(action.getType()));
        if (hasBorrowAction && !context.getBooks().isEmpty()) {
            conversationMemory.rememberBorrowOptions(currentUserId(), context.getBooks());
        } else if (hasReturnAction && !context.getBorrowRecords().isEmpty()) {
            conversationMemory.rememberReturnOptions(currentUserId(), context.getBorrowRecords());
        }
    }

    private Integer currentUserId() {
        return AuthContext.get() == null ? null : AuthContext.get().getUserId();
    }

    private Map<String, Object> chatRequest(List<Map<String, Object>> messages, List<Map<String, Object>> tools, String toolChoice) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("model", deepSeekProperties.getModel());
        request.put("messages", messages);
        request.put("tools", tools);
        request.put("tool_choice", toolChoice);
        request.put("temperature", 0.2);
        return request;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractMessage(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return Map.of("role", "assistant", "content", "服务暂时没有返回结果。");
        }
        return (Map<String, Object>) choices.get(0).get("message");
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
