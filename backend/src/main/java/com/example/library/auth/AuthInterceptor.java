package com.example.library.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final int ROLE_READER = 1;
    private static final int ROLE_LIBRARIAN = 2;
    private static final int ROLE_ADMIN = 3;

    private final TokenStore tokenStore;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(TokenStore tokenStore, ObjectMapper objectMapper) {
        this.tokenStore = tokenStore;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        LoginUser user = tokenStore.getUser(resolveToken(request));
        if (user == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "please login first");
            return false;
        }

        AuthContext.set(user);
        if (!isAllowed(request, user)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "permission denied");
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    private boolean isAllowed(HttpServletRequest request, LoginUser user) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        Integer roleId = user.getRoleId();

        if (path.startsWith("/api/users")) {
            return "GET".equalsIgnoreCase(method) ? isLibrarianOrAdmin(roleId) : isAdmin(roleId);
        }
        if (path.startsWith("/api/reader-agent")) {
            return Integer.valueOf(ROLE_READER).equals(roleId);
        }
        if (path.startsWith("/api/borrows")) {
            return roleId != null && roleId >= ROLE_READER;
        }
        if (path.startsWith("/api/statistics")) {
            return isLibrarianOrAdmin(roleId);
        }
        if (path.startsWith("/api/books") && !"GET".equalsIgnoreCase(method)) {
            return isLibrarianOrAdmin(roleId);
        }
        return roleId != null && roleId >= ROLE_READER;
    }

    private boolean isAdmin(Integer roleId) {
        return Integer.valueOf(ROLE_ADMIN).equals(roleId);
    }

    private boolean isLibrarianOrAdmin(Integer roleId) {
        return Integer.valueOf(ROLE_LIBRARIAN).equals(roleId) || Integer.valueOf(ROLE_ADMIN).equals(roleId);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return request.getHeader("X-Auth-Token");
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "code", status,
                "message", message,
                "data", null
        )));
    }
}
