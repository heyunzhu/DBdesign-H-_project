package com.example.library.service.impl;

import com.example.library.common.BusinessException;
import com.example.library.dto.UserCreateRequest;
import com.example.library.dto.UserStatusUpdateRequest;
import com.example.library.dto.UserUpdateRequest;
import com.example.library.entity.SysUser;
import com.example.library.mapper.UserMapper;
import com.example.library.service.UserService;
import com.example.library.vo.UserVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public List<UserVO> listUsers(String keyword, Integer roleId, Integer status) {
        return userMapper.selectUsers(keyword, roleId, status);
    }

    @Override
    public UserVO getUser(Integer userId) {
        UserVO user = userMapper.selectUserById(userId);
        if (user == null) {
            throw new BusinessException("user not found");
        }
        return user;
    }

    @Override
    public void createUser(UserCreateRequest request) {
        SysUser user = new SysUser();
        user.setUserNo(request.getUserNo());
        user.setUserName(request.getUserName());
        user.setPhone(request.getPhone());
        user.setDeptName(request.getDeptName());
        user.setAccountStatus(1);
        user.setRoleId(request.getRoleId());
        userMapper.insertUser(user);
    }

    @Override
    public void updateUser(Integer userId, UserUpdateRequest request) {
        getUser(userId);

        SysUser user = new SysUser();
        user.setUserId(userId);
        user.setUserNo(request.getUserNo());
        user.setUserName(request.getUserName());
        user.setPhone(request.getPhone());
        user.setDeptName(request.getDeptName());
        user.setAccountStatus(request.getAccountStatus());
        user.setRoleId(request.getRoleId());
        userMapper.updateUser(user);
    }

    @Override
    public void updateUserStatus(Integer userId, UserStatusUpdateRequest request) {
        getUser(userId);
        userMapper.updateUserStatus(userId, request.getAccountStatus());
    }
}
