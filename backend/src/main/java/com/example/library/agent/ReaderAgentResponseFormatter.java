package com.example.library.agent;

import com.example.library.vo.BookDetailVO;
import com.example.library.vo.BorrowRecordVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReaderAgentResponseFormatter {

    public String formatBookReply(String keyword, List<BookDetailVO> books) {
        if (books.isEmpty()) {
            return "我查了一下，当前没有找到可借的" + (keyword.isBlank() ? "相关" : "“" + keyword + "”相关") + "图书。你可以换个关键词，或者试试“计算机”“数据库”“软件”这类更宽一点的方向。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("我查到 ").append(books.size()).append(" 本可借的")
                .append(keyword.isBlank() ? "相关" : "“" + keyword + "”相关")
                .append("图书：");
        for (int i = 0; i < books.size(); i++) {
            BookDetailVO book = books.get(i);
            builder.append("\n")
                    .append(i + 1)
                    .append(". 《")
                    .append(book.getBookName())
                    .append("》")
                    .append("，作者：")
                    .append(book.getAuthorName())
                    .append("。");
        }
        builder.append("\n如果你想借其中一本，可以点下面的借阅按钮，或者直接说“帮我借《书名》”。");
        return builder.toString();
    }

    public String formatBorrowReply(List<BorrowRecordVO> records) {
        if (records.isEmpty()) {
            return "你当前没有符合条件的借阅记录。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("这是你的借阅记录：");
        for (int i = 0; i < records.size(); i++) {
            BorrowRecordVO record = records.get(i);
            builder.append("\n")
                    .append(i + 1)
                    .append(". 《")
                    .append(record.getBookName())
                    .append("》，状态：")
                    .append(borrowStatusText(record.getBorrowStatus()))
                    .append("，应还时间：")
                    .append(record.getDueReturnTime());
        }
        builder.append("\n需要归还时，可以点击归还按钮，或说“帮我归还《书名》”。");
        return builder.toString();
    }

    private String borrowStatusText(Integer status) {
        if (Integer.valueOf(0).equals(status)) {
            return "未归还";
        }
        if (Integer.valueOf(1).equals(status)) {
            return "已归还";
        }
        if (Integer.valueOf(2).equals(status)) {
            return "逾期";
        }
        return "未知";
    }
}
