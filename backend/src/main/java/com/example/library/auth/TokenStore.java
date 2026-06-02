package com.example.library.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    private final Map<String, LoginUser> tokens = new ConcurrentHashMap<>();

    public String createToken(LoginUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, user);
        return token;
    }

    public LoginUser getUser(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return tokens.get(token);
    }

    public void removeToken(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
