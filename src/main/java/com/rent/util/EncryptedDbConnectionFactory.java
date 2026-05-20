package com.rent.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

            /*
             * Important for sqlite-jdbc-crypt:
             * Put cipher/key in the JDBC URI before opening the database.
             * Do not rely on PRAGMA key after DriverManager.getConnection().
             */
            String encodedKey = URLEncoder.encode(databaseKey, StandardCharsets.UTF_8);

            String normalizedPath = dbPath
                    .toAbsolutePath()
                    .toString()
                    .replace("\\", "/");

            String url =
                    "jdbc:sqlite:file:" + normalizedPath +
                            "?cipher=sqlcipher" +
                            "&key=" + encodedKey;

            Connection conn = DriverManager.getConnection(url);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");

                /*
                 * Validate immediately.
                 * If key/cipher is wrong, this should fail here.
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
}