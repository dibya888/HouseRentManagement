package com.rent.dao;

import com.rent.util.AuthDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuthAuditLogDAO {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String username, String action, String details) {
        String sql = """
            INSERT INTO auth_audit_logs
            (username, action, details, created_at)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username == null || username.isBlank() ? "SYSTEM" : username);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.setString(4, LocalDateTime.now().format(TS));

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}