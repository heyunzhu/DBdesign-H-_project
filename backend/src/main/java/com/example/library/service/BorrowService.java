package com.example.library.service;

import com.example.library.dto.BorrowCreateRequest;
import com.example.library.vo.BorrowRecordVO;
import com.example.library.vo.PageResult;

import java.util.List;

public interface BorrowService {

    List<BorrowRecordVO> listBorrowRecords(Integer userId, Integer status);

    PageResult<BorrowRecordVO> listBorrowRecordsPage(Integer userId, Integer status, Integer page, Integer pageSize);

    Integer borrowBook(BorrowCreateRequest request);

    void returnBook(Integer borrowId);
}
