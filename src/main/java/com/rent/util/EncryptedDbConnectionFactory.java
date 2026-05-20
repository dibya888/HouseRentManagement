package com.rent.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class EncryptedDbConnectionFactory {

    private EncryptedDbConnectionFactory() {
    }

    public static Connection open(Path dbPath, String databaseKey) {
        try {
            if (databaseKey == null || databaseKey.isBlank()) {
                throw new IllegalArgumentException("Database key is missing.");
            }

            Path parent = dbPath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();

            Connection conn = DriverManager.getConnection(url);

            try (Statement stmt = conn.createStatement()) {

                /*
                 * Important:
                 * Cipher/key pragmas must run before normal SQL statements.
                 */
                stmt.execute("PRAGMA cipher = 'sqlcipher'");
                stmt.execute("PRAGMA key = " + sqlQuote(databaseKey));
                stmt.execute("PRAGMA foreign_keys = ON");

                /*
                 * Validate key immediately.
                 * Wrong key or broken encryption setup should fail here.
                 */
                try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM sqlite_master")) {
                    if (rs.next()) {
                        rs.getInt(1);
                    }
                }
            }

            return conn;

        } catch (Exception e) {
            throw new RuntimeException("Failed to open encrypted database: " + dbPath, e);
        }
    }

    private static String sqlQuote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }
}