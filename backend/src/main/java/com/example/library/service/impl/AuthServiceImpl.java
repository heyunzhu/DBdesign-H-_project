package com.example.library.service.impl;

import com.example.library.auth.LoginUser;
import com.example.library.auth.PasswordUtil;
import com.example.library.auth.TokenStore;
import com.example.library.common.BusinessException;
import com.example.library.dto.LoginRequest;
import com.example.library.entity.SysUser;
import com.example.library.mapper.UserMapper;
import com.example.library.service.AuthService;
import com.example.library.vo.LoginVO;
import com.example.library.vo.UserVO;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final TokenStore tokenStore;

    public AuthServiceImpl(UserMapper userMapper, TokenStore tokenStore) {
        this.userMapper = userMapper;
        this.tokenStore = tokenStore;
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = userMapper.selectUserEntityByUserNo(request.getUserNo());
        if (user == null || !PasswordUtil.sha256(request.getPassword()).equals(user.getPasswordHash())) {
            throw new BusinessException("userNo or password is incorrect");
        }
        if (Integer.valueOf(0).equals(user.getAccountStatus())) {
            throw new BusinessException("account is disabled");
        }

        UserVO userVO = userMapper.selectUserById(user.getUserId());
        LoginUser loginUser = new LoginUser(
                userVO.getUserId(),
                userVO.getUserNo(),
                userVO.getUserName(),
                userVO.getRoleId(),
                userVO.getRoleName()
        );
        String token = tokenStore.createToken(loginUser);
        return new LoginVO(
                token,
                loginUser.getUserId(),
                loginUser.getUserNo(),
                loginUser.getUserName(),
                loginUser.getRoleId(),
                loginUser.getRoleName()
        );
    }

    @Override
    public void logout(String token) {
        tokenStore.removeToken(token);
    }
}
