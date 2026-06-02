package com.example.library.vo;

public class LoginVO {

    private String token;
    private Integer userId;
    private String userNo;
    private String userName;
    private Integer roleId;
    private String roleName;

    public LoginVO() {
    }

    public LoginVO(String token, Integer userId, String userNo, String userName, Integer roleId, String roleName) {
        this.token = token;
        this.userId = userId;
        this.userNo = userNo;
        this.userName = userName;
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
