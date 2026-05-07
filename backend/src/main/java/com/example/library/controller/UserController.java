package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.dto.UserCreateRequest;
import com.example.library.dto.UserStatusUpdateRequest;
import com.example.library.dto.UserUpdateRequest;
import com.example.library.service.UserService;
import com.example.library.vo.UserVO;
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
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<UserVO>> listUsers(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) Integer roleId,
                                               @RequestParam(required = false) Integer status) {
        return ApiResponse.success(userService.listUsers(keyword, roleId, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserVO> getUser(@PathVariable Integer id) {
        return ApiResponse.success(userService.getUser(id));
    }

    @PostMapping
    public ApiResponse<Void> createUser(@Valid @RequestBody UserCreateRequest request) {
        userService.createUser(request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateUser(@PathVariable Integer id,
                                        @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable Integer id,
                                              @Valid @RequestBody UserStatusUpdateRequest request) {
        userService.updateUserStatus(id, request);
        return ApiResponse.success();
    }
}
