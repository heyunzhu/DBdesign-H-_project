package com.example.library.service;

import com.example.library.dto.BookCreateRequest;
import com.example.library.dto.BookUpdateRequest;
import com.example.library.vo.BookDetailVO;
import com.example.library.vo.PageResult;

import java.util.List;

public interface BookService {

    List<BookDetailVO> listBooks(String keyword, Integer status);

    PageResult<BookDetailVO> listBooksPage(String keyword, Integer status, Integer page, Integer pageSize);

    BookDetailVO getBook(Integer bookId);

    void createBook(BookCreateRequest request);

    void updateBook(Integer bookId, BookUpdateRequest request);

    void disableBook(Integer bookId);
}
