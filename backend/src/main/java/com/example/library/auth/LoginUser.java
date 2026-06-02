package com.example.library.auth;

public class LoginUser {

    private final Integer userId;
    private final String userNo;
    private final String userName;
    private final Integer roleId;
    private final String roleName;

    public LoginUser(Integer userId, String userNo, String userName, Integer roleId, String roleName) {
        this.userId = userId;
        this.userNo = userNo;
        this.userName = userName;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUserNo() {
        return userNo;
    }

    public String getUserName() {
        return userName;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public String getRoleName() {
        return roleName;
    }
}
