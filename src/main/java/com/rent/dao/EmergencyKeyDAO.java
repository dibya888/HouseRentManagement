package com.rent.dao;

import com.rent.util.AuthDBUtil;
import com.rent.util.CurrentSession;
import com.rent.util.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmergencyKeyDAO {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /*
     * Generate/replace keys for the currently logged-in user.
     * This belongs to auth.db, not rent.db.
     */
    public static void replaceKeys(List<String> plainKeys) {
        CurrentSession.requireLogin();

        String userId = CurrentSession.getUserId();

        String deleteSql = """
            DELETE FROM emergency_keys
            WHERE user_id = ?
        """;

        String insertSql = """
            INSERT INTO emergency_keys
            (user_id, key_hash, key_salt, used, created_at, used_at)
            VALUES (?, ?, ?, 0, ?, NULL)
        """;

        try (Connection conn = AuthDBUtil.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (String plainKey : plainKeys) {
                    String salt = SecurityUtil.generateSalt();
                    String hash = SecurityUtil.hashSecret(plainKey, salt);

                    ps.setString(1, userId);
                    ps.setString(2, hash);
                    ps.setString(3, salt);
                    ps.setString(4, LocalDateTime.now().format(TS));
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Count unused keys for current logged-in user.
     * Dashboard uses this after login.
     */
    public static int countUnusedKeys() {
        if (!CurrentSession.isLoggedIn()) {
            return 0;
        }

        String sql = """
            SELECT COUNT(*)
            FROM emergency_keys
            WHERE user_id = ?
              AND used = 0
        """;

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, CurrentSession.getUserId());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /*
     * Existing recovery flow only gives inputKey, not username/userId.
     * So this scans all unused keys in auth.db.
     *
     * Later we can make this stricter by requiring username during recovery.
     */
    public static boolean useEmergencyKey(String inputKey) {
        String selectSql = """
            SELECT id, key_hash, key_salt
            FROM emergency_keys
            WHERE used = 0
        """;

        String updateSql = """
            UPDATE emergency_keys
            SET used = 1,
                used_at = ?
            WHERE id = ?
        """;

        try (Connection conn = AuthDBUtil.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement selectPs = conn.prepareStatement(selectSql);
                 ResultSet rs = selectPs.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String hash = rs.getString("key_hash");
                    String salt = rs.getString("key_salt");

                    boolean matched = SecurityUtil.verifySecret(
                            inputKey,
                            hash,
                            salt
                    );

                    if (matched) {
                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                            updatePs.setString(1, LocalDateTime.now().format(TS));
                            updatePs.setInt(2, id);
                            updatePs.executeUpdate();
                        }

                        conn.commit();
                        return true;
                    }
                }
            }

            conn.rollback();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}