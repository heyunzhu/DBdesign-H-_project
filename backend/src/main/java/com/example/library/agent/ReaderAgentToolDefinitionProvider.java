package com.example.library.agent;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ReaderAgentToolDefinitionProvider {

    public List<Map<String, Object>> definitions() {
        return List.of(
                tool("search_books", "查询图书，可按关键词搜索书名、ISBN 或作者。",
                        Map.of(
                                "keyword", stringSchema("搜索关键词，可为空"),
                                "onlyAvailable", booleanSchema("是否只查询可借图书")
                        ),
                        List.of()),
                tool("borrow_book_by_query", "根据读者自然语言描述查找并借阅图书。query 可以是书名、作者、ISBN 或主题，例如“王珊那本数据库书”。如果匹配多本书，工具会返回候选书让读者确认。",
                        Map.of("query", stringSchema("读者想借的图书描述、书名、作者、ISBN 或主题")),
                        List.of("query")),
                tool("list_my_borrows", "查询当前登录读者自己的借阅记录。",
                        Map.of("status", integerSchema("借阅状态：0 未归还，1 已归还，2 逾期；可为空")),
                        List.of()),
                tool("return_book_by_query", "根据读者自然语言描述查找并归还当前读者自己的未归还图书。query 可以是书名、ISBN 或主题。如果匹配多条借阅记录，工具会返回候选记录让读者确认。",
                        Map.of("query", stringSchema("读者想归还的图书描述、书名、ISBN 或主题")),
                        List.of("query"))
        );
    }

    private Map<String, Object> tool(String name, String description, Map<String, Object> properties, List<String> required) {
        return Map.of(
                "type", "function",
                "function", Map.of(
                        "name", name,
                        "description", description,
                        "parameters", Map.of(
                                "type", "object",
                                "properties", properties,
                                "required", required
                        )
                )
        );
    }

    private Map<String, Object> stringSchema(String description) {
        return Map.of("type", "string", "description", description);
    }

    private Map<String, Object> integerSchema(String description) {
        return Map.of("type", "integer", "description", description);
    }

    private Map<String, Object> booleanSchema(String description) {
        return Map.of("type", "boolean", "description", description);
    }
}
