package com.example.library.service;

import com.example.library.dto.UserCreateRequest;
import com.example.library.dto.UserStatusUpdateRequest;
import com.example.library.dto.UserUpdateRequest;
import com.example.library.vo.UserVO;

import java.util.List;

public interface UserService {

    List<UserVO> listUsers(String keyword, Integer roleId, Integer status);

    UserVO getUser(Integer userId);

    void createUser(UserCreateRequest request);

    void updateUser(Integer userId, UserUpdateRequest request);

    void updateUserStatus(Integer userId, UserStatusUpdateRequest request);
}
