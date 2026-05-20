package com.rent.model;

public class UserAccount {

    private String id;
    private String username;
    private String displayName;
    private String passwordHash;
    private String passwordSalt;
    private String dbKeySalt;
    private String encryptedDbKey;
    private String role;
    private String status;
    private String dbFolder;
    private String createdAt;
    private String updatedAt;
    private String lastLoginAt;

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPasswordSalt() { return passwordSalt; }
    public void setPasswordSalt(String passwordSalt) { this.passwordSalt = passwordSalt; }

    public String getDbKeySalt() { return dbKeySalt; }
    public void setDbKeySalt(String dbKeySalt) { this.dbKeySalt = dbKeySalt; }

    public String getEncryptedDbKey() { return encryptedDbKey; }
    public void setEncryptedDbKey(String encryptedDbKey) { this.encryptedDbKey = encryptedDbKey; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDbFolder() { return dbFolder; }
    public void setDbFolder(String dbFolder) { this.dbFolder = dbFolder; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(String lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}