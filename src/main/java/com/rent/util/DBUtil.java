package com.rent.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DBUtil {

    private static final String URL = "jdbc:sqlite:src/main/resources/database/rent.db";

    public static Connection connect() {
        try {
            return DriverManager.getConnection(URL);
        } catch (Exception e) {
            System.out.println("DB Connection Failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // USERS TABLE
            String usersSql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE,
                password TEXT
            )
        """;

            stmt.execute(usersSql);

            stmt.execute("""
            INSERT OR IGNORE INTO users(username, password)
            VALUES ('admin', '1234');
        """);

            // TENANTS TABLE (🔥 ADD THIS)
            String tenantsSql = """
            CREATE TABLE IF NOT EXISTS tenants (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT NOT NULL,
                email TEXT,
                nid TEXT,
                address TEXT
            )
        """;

            stmt.execute(tenantsSql);

            System.out.println("Database Ready");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}