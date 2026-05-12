package com.example.library.mapper;

import com.example.library.entity.Book;
import com.example.library.vo.BookDetailVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BookMapper {

    List<BookDetailVO> selectBooks(@Param("keyword") String keyword, @Param("status") Integer status);

    List<BookDetailVO> selectBooksPage(@Param("keyword") String keyword,
                                       @Param("status") Integer status,
                                       @Param("offset") Integer offset,
                                       @Param("pageSize") Integer pageSize);

    long countBooks(@Param("keyword") String keyword, @Param("status") Integer status);

    BookDetailVO selectBookById(@Param("bookId") Integer bookId);

    int insertBook(Book book);

    int updateBook(Book book);

    int updateBookStatus(@Param("bookId") Integer bookId, @Param("bookStatus") Integer bookStatus);
}
