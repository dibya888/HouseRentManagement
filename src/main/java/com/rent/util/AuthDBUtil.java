package com.rent.util;

import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public final class AuthDBUtil {

    private AuthDBUtil() {}

    public static Connection connect() {
        try {
            Files.createDirectories(AppPaths.getAuthDir());
            String url = "jdbc:sqlite:" + AppPaths.getAuthDbPath().toAbsolutePath();
            Connection conn = DriverManager.getConnection(url);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to auth database.", e);
        }
    }

    public static void init() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id TEXT PRIMARY KEY,
                    username TEXT NOT NULL UNIQUE,
                    display_name TEXT,
                    password_hash TEXT NOT NULL,
                    password_salt TEXT NOT NULL,
                    db_key_salt TEXT NOT NULL,
                    encrypted_db_key TEXT NOT NULL,
                    role TEXT NOT NULL DEFAULT 'USER',
                    status TEXT NOT NULL DEFAULT 'ACTIVE',
                    db_folder TEXT NOT NULL,
                    created_at TEXT NOT NULL,
                    updated_at TEXT,
                    last_login_at TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS emergency_keys (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id TEXT NOT NULL,
                    key_hash TEXT NOT NULL,
                    key_salt TEXT NOT NULL,
                    used INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL,
                    used_at TEXT,
                    FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize auth database.", e);
        }
    }
}