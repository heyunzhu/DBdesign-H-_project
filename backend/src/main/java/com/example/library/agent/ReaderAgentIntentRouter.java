package com.example.library.agent;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ReaderAgentIntentRouter {

    private static final Pattern BOOK_ID_PATTERN = Pattern.compile("bookId\\s*=\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BORROW_ID_PATTERN = Pattern.compile("borrowId\\s*=\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern QUOTED_TEXT_PATTERN = Pattern.compile("[《“\"]([^》”\"]+)[》”\"]");

    public Integer extractBookId(String message) {
        return extractId(BOOK_ID_PATTERN, message);
    }

    public Integer extractBorrowId(String message) {
        return extractId(BORROW_ID_PATTERN, message);
    }

    public boolean isCatalogRecommendationIntent(String message) {
        String lower = message.toLowerCase();
        return containsAny(message, "推荐", "建议", "有没有", "有哪些", "找", "查", "想读", "想看", "想学习", "应该借")
                && containsAny(message, "书", "图书", "小说", "文学", "教材", "资料", "oop", "OOP")
                || containsAny(lower, "oop", "object-oriented", "object oriented");
    }

    public boolean isBookSearchIntent(String message) {
        String lower = message.toLowerCase();
        if (containsAny(lower, "oop", "object-oriented", "object oriented")
                || containsAny(message, "面向对象", "对象程序", "对象编程")) {
            return containsAny(message, "学习", "推荐", "建议", "应该", "借", "书", "知道");
        }
        return containsAny(message, "书", "图书", "借")
                && containsAny(message, "找", "查", "推荐", "建议", "相关", "有没有", "想借", "可借");
    }

    public boolean isGreetingIntent(String message) {
        String text = message.trim();
        return "你好".equals(text) || "您好".equals(text) || "hello".equalsIgnoreCase(text) || "hi".equalsIgnoreCase(text);
    }

    public boolean isBorrowByQueryIntent(String message) {
        return containsAny(message, "帮我借", "我要借", "我想借", "借一下", "借阅")
                && !containsAny(message, "相关", "推荐", "建议", "有哪些", "什么书");
    }

    public boolean isReturnByQueryIntent(String message) {
        return containsAny(message, "帮我还", "帮我归还", "我要还", "我想还", "归还", "还书")
                && !containsAny(message, "记录", "哪些", "什么");
    }

    public boolean isMyBorrowIntent(String message) {
        return containsAny(message, "我", "我的")
                && containsAny(message, "借阅", "借了", "到期", "逾期", "未归还", "归还记录");
    }

    public boolean isCancelIntent(String message) {
        String text = message.trim();
        return containsAny(text, "算了", "取消", "不借了", "不想借", "先不借", "还是不借", "不还了", "先不还")
                || containsAny(text, "不想学习", "不学了");
    }

    public boolean isConfirmationIntent(String message) {
        String text = message.trim();
        if (isExactConfirmation(text)) {
            return true;
        }
        return containsAny(text,
                "第一本", "第二本", "第三本", "第四本", "第五本",
                "第1本", "第2本", "第3本", "第4本", "第5本",
                "第一个", "第二个", "第三个", "第四个", "第五个",
                "第一条", "第二条", "第三条", "第四条", "第五条",
                "就这本", "这本", "刚才那本", "上面那本", "借刚才那本", "还刚才那本", "归还刚才那本");
    }

    public Integer resolveCandidateIndex(String message, int size) {
        if (size < 1) {
            return null;
        }
        if (isExactConfirmation(message.trim())) {
            return size == 1 ? 0 : null;
        }
        Map<String, Integer> indexWords = Map.ofEntries(
                Map.entry("第一", 0),
                Map.entry("第1", 0),
                Map.entry("1", 0),
                Map.entry("第二", 1),
                Map.entry("第2", 1),
                Map.entry("2", 1),
                Map.entry("第三", 2),
                Map.entry("第3", 2),
                Map.entry("3", 2),
                Map.entry("第四", 3),
                Map.entry("第4", 3),
                Map.entry("4", 3),
                Map.entry("第五", 4),
                Map.entry("第5", 4),
                Map.entry("5", 4)
        );
        for (Map.Entry<String, Integer> entry : indexWords.entrySet()) {
            if (message.contains(entry.getKey()) && entry.getValue() < size) {
                return entry.getValue();
            }
        }
        if (containsAny(message, "就这本", "这本", "刚才那本", "上面那本", "借刚才那本", "还刚才那本", "归还刚才那本")) {
            return size == 1 ? 0 : null;
        }
        return null;
    }

    public Integer inferBorrowStatus(String message) {
        if (message.contains("逾期")) {
            return 2;
        }
        if (message.contains("已归还") || message.contains("归还记录")) {
            return 1;
        }
        if (message.contains("未归还") || message.contains("没还") || message.contains("还没还")) {
            return 0;
        }
        if (containsAny(message, "现在", "当前", "正在", "刚借", "借了什么", "都借")) {
            return 0;
        }
        return null;
    }

    public String extractActionQuery(String message, String... actionWords) {
        Matcher matcher = QUOTED_TEXT_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        String query = message;
        for (String actionWord : actionWords) {
            query = query.replace(actionWord, "");
        }
        return query
                .replace("帮我", "")
                .replace("我要", "")
                .replace("我想", "")
                .replace("一下", "")
                .replace("这本书", "")
                .replace("这本", "")
                .replace("那本书", "")
                .replace("那本", "")
                .replace("书", "")
                .trim();
    }

    public String extractBookKeyword(String message) {
        String lower = message.toLowerCase();
        if (isObjectOrientedTopic(lower, message)) {
            return "面向对象";
        }
        List<String> knownKeywords = List.of(
                "数据结构", "算法", "数据库", "人工智能", "机器学习", "深度学习", "面向对象", "程序设计",
                "操作系统", "计算机网络", "计算机", "软件工程", "软件", "Java", "Python",
                "数学", "英语", "文学", "经济"
        );
        for (String keyword : knownKeywords) {
            if (message.contains(keyword)) {
                return keyword;
            }
        }
        String keyword = message
                .replace("介绍一下", "")
                .replace("介绍", "")
                .replace("有哪些", "")
                .replace("我想借", "")
                .replace("有没有", "")
                .replace("有什么建议", "")
                .replace("有什么推荐", "")
                .replace("哪些", "")
                .replace("推荐", "")
                .replace("建议", "")
                .replace("相关的书", "")
                .replace("相关", "")
                .replace("方面", "")
                .replace("的", "")
                .replace("相关图书", "")
                .replace("图书", "")
                .replace("书", "")
                .replace("查一下", "")
                .replace("找一下", "")
                .trim();
        return limitKeyword(keyword);
    }

    public String extractCatalogKeyword(String message) {
        String lower = message.toLowerCase();
        if (isObjectOrientedTopic(lower, message)) {
            return "面向对象";
        }
        List<String> knownKeywords = List.of(
                "现代主义小说", "现代主义文学", "现代主义", "小说", "文学",
                "数据结构", "算法", "数据库", "人工智能", "机器学习", "深度学习", "面向对象", "程序设计",
                "操作系统", "计算机网络", "计算机", "软件工程", "软件", "Java", "Python",
                "数学", "英语", "经济"
        );
        for (String keyword : knownKeywords) {
            if (message.contains(keyword)) {
                return keyword;
            }
        }
        String keyword = message
                .replace("我想读", "")
                .replace("我想看", "")
                .replace("我想学习", "")
                .replace("应该借什么", "")
                .replace("有什么推荐", "")
                .replace("有推荐嘛", "")
                .replace("有推荐吗", "")
                .replace("推荐", "")
                .replace("建议", "")
                .replace("有哪些", "")
                .replace("有没有", "")
                .replace("相关的书", "")
                .replace("相关图书", "")
                .replace("图书", "")
                .replace("书", "")
                .replace("？", "")
                .replace("?", "")
                .trim();
        return limitKeyword(keyword);
    }

    public boolean containsToolCallMarkup(String reply) {
        return reply != null && (reply.contains("tool_calls")
                || reply.contains("DSML")
                || reply.contains("<｜｜")
                || reply.contains("</｜｜"));
    }

    public boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isExactConfirmation(String text) {
        return "确认".equals(text)
                || "确定".equals(text)
                || "可以".equals(text)
                || "是的".equals(text)
                || "对".equals(text)
                || "没错".equals(text)
                || "就它".equals(text)
                || "就这个".equals(text)
                || "1".equals(text)
                || "2".equals(text)
                || "3".equals(text)
                || "4".equals(text)
                || "5".equals(text);
    }

    private boolean isObjectOrientedTopic(String lowerMessage, String message) {
        return lowerMessage.contains("oop")
                || lowerMessage.contains("object-oriented")
                || lowerMessage.contains("object oriented")
                || message.contains("面向对象")
                || message.contains("对象程序")
                || message.contains("对象编程");
    }

    private String limitKeyword(String keyword) {
        return keyword.length() > 20 ? keyword.substring(0, 20) : keyword;
    }

    private Integer extractId(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }
}
