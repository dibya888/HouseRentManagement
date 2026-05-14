package com.rent.dao;

import com.rent.util.DBUtil;
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

    public static void replaceKeys(List<String> plainKeys) {
        String deleteSql = "DELETE FROM emergency_keys";

        String insertSql = """
                INSERT INTO emergency_keys
                (key_hash, key_salt, used, created_at, used_at)
                VALUES (?, ?, 0, ?, NULL)
                """;

        try (Connection conn = DBUtil.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (String plainKey : plainKeys) {
                    String salt = SecurityUtil.generateSalt();
                    String hash = SecurityUtil.hashSecret(plainKey, salt);

                    ps.setString(1, hash);
                    ps.setString(2, salt);
                    ps.setString(3, LocalDateTime.now().format(TS));
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            conn.commit();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int countUnusedKeys() {
        String sql = """
                SELECT COUNT(*)
                FROM emergency_keys
                WHERE used = 0
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

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

        try (Connection conn = DBUtil.connect()) {
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