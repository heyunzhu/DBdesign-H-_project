package com.example.library.service.impl;

import com.example.library.mapper.StatisticsMapper;
import com.example.library.service.StatisticsService;
import com.example.library.vo.BorrowRecordVO;
import com.example.library.vo.CategoryBookCountVO;
import com.example.library.vo.CategoryBorrowCountVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public StatisticsServiceImpl(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    @Override
    public List<CategoryBookCountVO> countBooksByCategory() {
        return statisticsMapper.countBooksByCategory();
    }

    @Override
    public List<BorrowRecordVO> listOverdueRecords() {
        return statisticsMapper.selectOverdueRecords();
    }

    @Override
    public List<CategoryBorrowCountVO> countBorrowsByCategory() {
        return statisticsMapper.countBorrowsByCategory();
    }
}
