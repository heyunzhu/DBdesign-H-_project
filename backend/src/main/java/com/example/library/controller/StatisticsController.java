package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.service.StatisticsService;
import com.example.library.vo.BorrowRecordVO;
import com.example.library.vo.CategoryBookCountVO;
import com.example.library.vo.CategoryBorrowCountVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/category-books")
    public ApiResponse<List<CategoryBookCountVO>> countBooksByCategory() {
        return ApiResponse.success(statisticsService.countBooksByCategory());
    }

    @GetMapping("/overdue-records")
    public ApiResponse<List<BorrowRecordVO>> listOverdueRecords() {
        return ApiResponse.success(statisticsService.listOverdueRecords());
    }

    @GetMapping("/borrow-ranking")
    public ApiResponse<List<CategoryBorrowCountVO>> countBorrowsByCategory() {
        return ApiResponse.success(statisticsService.countBorrowsByCategory());
    }
}
