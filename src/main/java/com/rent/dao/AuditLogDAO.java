package com.rent.dao;

import com.rent.controller.LoginController;
import com.rent.model.AuditLog;
import com.rent.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class AuditLogDAO {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int RETENTION_DAYS = 365;

    public static void log(String action, String details) {
        String username = getLoggedInUsername();

        log(username, action, details);
    }

    public static void log(String username, String action, String details) {
        String sql = """
                INSERT INTO audit_logs
                (username, action, details, created_at)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username == null || username.isBlank() ? "SYSTEM" : username);
            ps.setString(2, action);
            ps.setString(3, details);
            ps.setString(4, LocalDateTime.now().format(TS));

            ps.executeUpdate();

            cleanupOldLogs();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<AuditLog> getAllLogs() {
        List<AuditLog> list = new ArrayList<>();

        String sql = """
                SELECT id, username, action, details, created_at
                FROM audit_logs
                ORDER BY id DESC
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AuditLog log = new AuditLog();

                log.setId(rs.getInt("id"));
                log.setUsername(rs.getString("username"));
                log.setAction(rs.getString("action"));
                log.setDetails(rs.getString("details"));
                log.setCreatedAt(rs.getString("created_at"));

                list.add(log);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<String> getActionTypes() {
        List<String> list = new ArrayList<>();

        String sql = """
                SELECT DISTINCT action
                FROM audit_logs
                ORDER BY action
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("action"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private static void cleanupOldLogs() {
        String sql = """
            DELETE FROM audit_logs
            WHERE datetime(created_at) < datetime('now', ?)
            """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "-" + RETENTION_DAYS + " days");
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getLoggedInUsername() {
        Preferences prefs =
                Preferences.userNodeForPackage(LoginController.class);

        return prefs.get("loggedInUser", "SYSTEM");
    }
}