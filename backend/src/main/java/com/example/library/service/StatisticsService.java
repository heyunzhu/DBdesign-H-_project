package com.example.library.service;

import com.example.library.vo.BorrowRecordVO;
import com.example.library.vo.CategoryBookCountVO;
import com.example.library.vo.CategoryBorrowCountVO;

import java.util.List;

public interface StatisticsService {

    List<CategoryBookCountVO> countBooksByCategory();

    List<BorrowRecordVO> listOverdueRecords();

    List<CategoryBorrowCountVO> countBorrowsByCategory();
}
