package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.dto.BorrowCreateRequest;
import com.example.library.service.BorrowService;
import com.example.library.vo.BorrowRecordVO;
import jakarta.validation.Valid;
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
@RequestMapping("/api/borrows")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping
    public ApiResponse<List<BorrowRecordVO>> listBorrowRecords(@RequestParam(required = false) Integer userId,
                                                               @RequestParam(required = false) Integer status) {
        return ApiResponse.success(borrowService.listBorrowRecords(userId, status));
    }

    @PostMapping
    public ApiResponse<Void> borrowBook(@Valid @RequestBody BorrowCreateRequest request) {
        borrowService.borrowBook(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/return")
    public ApiResponse<Void> returnBook(@PathVariable Integer id) {
        borrowService.returnBook(id);
        return ApiResponse.success();
    }
}
