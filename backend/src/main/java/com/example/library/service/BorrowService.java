package com.example.library.service;

import com.example.library.dto.BorrowCreateRequest;
import com.example.library.vo.BorrowRecordVO;

import java.util.List;

public interface BorrowService {

    List<BorrowRecordVO> listBorrowRecords(Integer userId, Integer status);

    void borrowBook(BorrowCreateRequest request);

    void returnBook(Integer borrowId);
}
