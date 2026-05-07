package com.example.library.mapper;

import com.example.library.vo.BorrowRecordVO;
import com.example.library.vo.CategoryBookCountVO;
import com.example.library.vo.CategoryBorrowCountVO;

import java.util.List;

public interface StatisticsMapper {

    List<CategoryBookCountVO> countBooksByCategory();

    List<BorrowRecordVO> selectOverdueRecords();

    List<CategoryBorrowCountVO> countBorrowsByCategory();
}
