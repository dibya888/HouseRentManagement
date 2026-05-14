package com.rent.model;

public class AuditLog {

    private int id;
    private String username;
    private String action;
    private String details;
    private String createdAt;

    public AuditLog() {
    }

    public AuditLog(int id, String username, String action, String details, String createdAt) {
        this.id = id;
        this.username = username;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public AuditLog(String username, String action, String details, String createdAt) {
        this.username = username;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}