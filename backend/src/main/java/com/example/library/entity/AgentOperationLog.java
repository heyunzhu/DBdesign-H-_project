package com.example.library.entity;

import java.time.LocalDateTime;

public class AgentOperationLog {

    private Integer logId;
    private Integer userId;
    private String actionType;
    private Integer targetBookId;
    private Integer targetBorrowId;
    private String userMessage;
    private String toolName;
    private String toolArguments;
    private Integer resultSuccess;
    private String resultMessage;
    private LocalDateTime createTime;

    public Integer getLogId() {
        return logId;
    }

    public void setLogId(Integer logId) {
        this.logId = logId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public Integer getTargetBookId() {
        return targetBookId;
    }

    public void setTargetBookId(Integer targetBookId) {
        this.targetBookId = targetBookId;
    }

    public Integer getTargetBorrowId() {
        return targetBorrowId;
    }

    public void setTargetBorrowId(Integer targetBorrowId) {
        this.targetBorrowId = targetBorrowId;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getToolArguments() {
        return toolArguments;
    }

    public void setToolArguments(String toolArguments) {
        this.toolArguments = toolArguments;
    }

    public Integer getResultSuccess() {
        return resultSuccess;
    }

    public void setResultSuccess(Integer resultSuccess) {
        this.resultSuccess = resultSuccess;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
