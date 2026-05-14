package com.rent.dao;

import com.rent.util.DBUtil;
import com.rent.util.SecurityUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserSecurityDAO {

    public static void migratePlainPasswordsIfNeeded() {
        String selectSql = """
                SELECT id, password, password_hash, password_salt
                FROM users
                """;

        String updateSql = """
                UPDATE users
                SET password_hash = ?,
                    password_salt = ?,
                    password = NULL
                WHERE id = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement selectPs = conn.prepareStatement(selectSql);
             ResultSet rs = selectPs.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");

                String plainPassword = rs.getString("password");
                String existingHash = rs.getString("password_hash");
                String existingSalt = rs.getString("password_salt");

                boolean needsMigration =
                        (existingHash == null || existingHash.isBlank())
                                && plainPassword != null
                                && !plainPassword.isBlank();

                if (needsMigration) {
                    String salt = SecurityUtil.generateSalt();
                    String hash = SecurityUtil.hashSecret(plainPassword, salt);

                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setString(1, hash);
                        updatePs.setString(2, salt);
                        updatePs.setInt(3, id);
                        updatePs.executeUpdate();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean verifyPassword(String username, String inputPassword) {
        String sql = """
                SELECT id, password, password_hash, password_salt
                FROM users
                WHERE username = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                int userId = rs.getInt("id");
                String plainPassword = rs.getString("password");
                String hash = rs.getString("password_hash");
                String salt = rs.getString("password_salt");

                // New hashed password verification
                if (hash != null && !hash.isBlank()
                        && salt != null && !salt.isBlank()) {

                    return SecurityUtil.verifySecret(inputPassword, hash, salt);
                }

                // Fallback for old plain password, then upgrade immediately
                if (plainPassword != null && plainPassword.equals(inputPassword)) {
                    upgradePasswordByUserId(userId, inputPassword);
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean updatePassword(String username, String newPassword) {
        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashSecret(newPassword, salt);

        String sql = """
                UPDATE users
                SET password_hash = ?,
                    password_salt = ?,
                    password = NULL
                WHERE username = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setString(3, username);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void upgradePasswordByUserId(int userId, String plainPassword) {
        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashSecret(plainPassword, salt);

        String sql = """
                UPDATE users
                SET password_hash = ?,
                    password_salt = ?,
                    password = NULL
                WHERE id = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setString(2, salt);
            ps.setInt(3, userId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
