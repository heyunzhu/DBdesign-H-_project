package com.example.library.controller;

import com.example.library.auth.AuthContext;
import com.example.library.auth.LoginUser;
import com.example.library.common.ApiResponse;
import com.example.library.dto.LoginRequest;
import com.example.library.service.AuthService;
import com.example.library.vo.LoginVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(resolveToken(request));
        return ApiResponse.success();
    }

    @GetMapping("/me")
    public ApiResponse<LoginVO> me(HttpServletRequest request) {
        LoginUser user = AuthContext.get();
        return ApiResponse.success(new LoginVO(
                resolveToken(request),
                user.getUserId(),
                user.getUserNo(),
                user.getUserName(),
                user.getRoleId(),
                user.getRoleName()
        ));
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("X-Auth-Token");
    }
}
