package com.rent.util;

import java.nio.file.Path;

public final class CurrentSession {

    private static String userId;
    private static String username;
    private static String displayName;
    private static String role;
    private static String databaseKey;

    private CurrentSession() {}

    public static void start(String sessionUserId,
                             String sessionUsername,
                             String sessionDisplayName,
                             String sessionRole,
                             String sessionDatabaseKey) {
        userId = sessionUserId;
        username = sessionUsername;
        displayName = sessionDisplayName;
        role = sessionRole;
        databaseKey = sessionDatabaseKey;
    }

    public static boolean isLoggedIn() {
        return userId != null && !userId.isBlank()
                && username != null && !username.isBlank()
                && databaseKey != null && !databaseKey.isBlank();
    }

    public static void clear() {
        userId = null;
        username = null;
        displayName = null;
        role = null;
        databaseKey = null;
    }

    public static String getUserId() {
        requireLogin();
        return userId;
    }

    public static String getUsername() {
        requireLogin();
        return username;
    }

    public static String getDisplayName() {
        requireLogin();
        return displayName;
    }

    public static String getRole() {
        requireLogin();
        return role;
    }

    public static String getDatabaseKey() {
        requireLogin();
        return databaseKey;
    }

    public static boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equalsIgnoreCase(role);
    }

    public static Path getCurrentUserDatabasePath() {
        requireLogin();
        return AppPaths.getUserRentDbPath(userId);
    }

    public static void requireLogin() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("No active logged-in user session.");
        }
    }

    public static void requireAdmin() {
        requireLogin();
        if (!isAdmin()) {
            throw new SecurityException("Admin permission required.");
        }
    }
}