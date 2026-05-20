package com.rent.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppPaths {

    private static final String APP_FOLDER_NAME = "HouseRentManagement";

    private AppPaths() {}

    public static Path getAppDataDir() {
        String appData = System.getenv("APPDATA");
        if (appData != null && !appData.isBlank()) {
            return Paths.get(appData, APP_FOLDER_NAME);
        }
        return Paths.get(System.getProperty("user.home"), "." + APP_FOLDER_NAME);
    }

    public static Path getAuthDir() {
        return getAppDataDir().resolve("auth");
    }

    public static Path getAuthDbPath() {
        return getAuthDir().resolve("auth.db");
    }

    public static Path getUsersDir() {
        return getAppDataDir().resolve("users");
    }

    public static Path getUserDir(String userId) {
        return getUsersDir().resolve(userId);
    }

    public static Path getUserRentDbPath(String userId) {
        return getUserDir(userId).resolve("rent.db");
    }

    // Legacy single-db path (for migration later)
    public static Path getLegacyDatabaseDir() {
        return getAppDataDir().resolve("database");
    }

    public static Path getLegacyDatabasePath() {
        return getLegacyDatabaseDir().resolve("rent.db");
    }
}