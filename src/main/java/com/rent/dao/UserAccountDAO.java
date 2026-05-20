package com.rent.dao;

import com.rent.model.UserAccount;
import com.rent.util.AuthDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserAccountDAO {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Optional<UserAccount> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static Optional<UserAccount> findById(String userId) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static List<UserAccount> getAllUsers() {
        List<UserAccount> users = new ArrayList<>();

        String sql = """
            SELECT *
            FROM users
            ORDER BY
                CASE WHEN role = 'ADMIN' THEN 0 ELSE 1 END,
                username COLLATE NOCASE
        """;

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(map(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return users;
    }

    public static boolean hasAnyUser() {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return rs.next() && rs.getInt(1) > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static boolean insert(UserAccount user) {
        String sql = """
            INSERT INTO users (
                id,
                username,
                display_name,
                password_hash,
                password_salt,
                db_key_salt,
                encrypted_db_key,
                role,
                status,
                db_folder,
                created_at,
                updated_at,
                last_login_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getId());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getDisplayName());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getPasswordSalt());
            ps.setString(6, user.getDbKeySalt());
            ps.setString(7, user.getEncryptedDbKey());
            ps.setString(8, user.getRole());
            ps.setString(9, user.getStatus());
            ps.setString(10, user.getDbFolder());
            ps.setString(11, user.getCreatedAt());
            ps.setString(12, user.getUpdatedAt());
            ps.setString(13, user.getLastLoginAt());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateLastLogin(String userId) {
        String sql = """
            UPDATE users
            SET last_login_at = ?
            WHERE id = ?
        """;

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, LocalDateTime.now().format(TS));
            ps.setString(2, userId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static UserAccount map(ResultSet rs) throws Exception {
        UserAccount u = new UserAccount();

        u.setId(rs.getString("id"));
        u.setUsername(rs.getString("username"));
        u.setDisplayName(rs.getString("display_name"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setPasswordSalt(rs.getString("password_salt"));
        u.setDbKeySalt(rs.getString("db_key_salt"));
        u.setEncryptedDbKey(rs.getString("encrypted_db_key"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        u.setDbFolder(rs.getString("db_folder"));
        u.setCreatedAt(rs.getString("created_at"));
        u.setUpdatedAt(rs.getString("updated_at"));
        u.setLastLoginAt(rs.getString("last_login_at"));

        return u;
    }

    public static boolean usernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();

            /*
             * Fail safe:
             * if we cannot check, behave as if username exists.
             */
            return true;
        }
    }
}