package com.example.library.service.impl;

import com.example.library.common.BusinessException;
import com.example.library.dto.BookCreateRequest;
import com.example.library.dto.BookUpdateRequest;
import com.example.library.entity.Book;
import com.example.library.mapper.BookMapper;
import com.example.library.service.BookService;
import com.example.library.vo.BookDetailVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private static final int BOOK_STATUS_DISABLED = 2;

    private final BookMapper bookMapper;

    public BookServiceImpl(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    @Override
    public List<BookDetailVO> listBooks(String keyword, Integer status) {
        return bookMapper.selectBooks(keyword, status);
    }

    @Override
    public BookDetailVO getBook(Integer bookId) {
        BookDetailVO book = bookMapper.selectBookById(bookId);
        if (book == null) {
            throw new BusinessException("book not found");
        }
        return book;
    }

    @Override
    public void createBook(BookCreateRequest request) {
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setBookName(request.getBookName());
        book.setAuthorId(request.getAuthorId());
        book.setTypeId(request.getTypeId());
        book.setPublisher(request.getPublisher());
        book.setPublishDate(request.getPublishDate());
        book.setBookStatus(0);
        bookMapper.insertBook(book);
    }

    @Override
    public void updateBook(Integer bookId, BookUpdateRequest request) {
        getBook(bookId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setIsbn(request.getIsbn());
        book.setBookName(request.getBookName());
        book.setAuthorId(request.getAuthorId());
        book.setTypeId(request.getTypeId());
        book.setPublisher(request.getPublisher());
        book.setPublishDate(request.getPublishDate());
        book.setBookStatus(request.getBookStatus());

        bookMapper.updateBook(book);
    }

    @Override
    public void disableBook(Integer bookId) {
        getBook(bookId);
        bookMapper.updateBookStatus(bookId, BOOK_STATUS_DISABLED);
    }
}
