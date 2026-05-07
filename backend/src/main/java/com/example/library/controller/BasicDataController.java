package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.service.BasicDataService;
import com.example.library.vo.AuthorVO;
import com.example.library.vo.BookTypeVO;
import com.example.library.vo.RoleVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BasicDataController {

    private final BasicDataService basicDataService;

    public BasicDataController(BasicDataService basicDataService) {
        this.basicDataService = basicDataService;
    }

    @GetMapping("/authors")
    public ApiResponse<List<AuthorVO>> listAuthors() {
        return ApiResponse.success(basicDataService.listAuthors());
    }

    @GetMapping("/book-types")
    public ApiResponse<List<BookTypeVO>> listBookTypes() {
        return ApiResponse.success(basicDataService.listBookTypes());
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleVO>> listRoles() {
        return ApiResponse.success(basicDataService.listRoles());
    }
}
