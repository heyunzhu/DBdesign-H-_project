package com.example.library.service;

import com.example.library.dto.LoginRequest;
import com.example.library.vo.LoginVO;

public interface AuthService {

    LoginVO login(LoginRequest request);

    void logout(String token);
}
