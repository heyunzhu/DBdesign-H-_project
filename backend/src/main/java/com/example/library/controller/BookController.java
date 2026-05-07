package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.dto.BookCreateRequest;
import com.example.library.dto.BookUpdateRequest;
import com.example.library.service.BookService;
import com.example.library.vo.BookDetailVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ApiResponse<List<BookDetailVO>> listBooks(@RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) Integer status) {
        return ApiResponse.success(bookService.listBooks(keyword, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<BookDetailVO> getBook(@PathVariable Integer id) {
        return ApiResponse.success(bookService.getBook(id));
    }

    @PostMapping
    public ApiResponse<Void> createBook(@Valid @RequestBody BookCreateRequest request) {
        bookService.createBook(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateBook(@PathVariable Integer id,
                                        @Valid @RequestBody BookUpdateRequest request) {
        bookService.updateBook(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> disableBook(@PathVariable Integer id) {
        bookService.disableBook(id);
        return ApiResponse.success();
    }
}
