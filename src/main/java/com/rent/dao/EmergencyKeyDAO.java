package com.rent.dao;

import com.rent.util.AuthDBUtil;
import com.rent.util.CurrentSession;
import com.rent.util.DbKeyCryptoUtil;
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
        CurrentSession.requireLogin();

        String userId = CurrentSession.getUserId();
        String dbKey = CurrentSession.getDatabaseKey();

        String deleteSql = """
            DELETE FROM emergency_keys
            WHERE user_id = ?
        """;

        String insertSql = """
            INSERT INTO emergency_keys
            (user_id, key_hash, key_salt,
             encrypted_db_key_by_key, db_key_salt_by_key,
             used, created_at, used_at)
            VALUES (?, ?, ?, ?, ?, 0, ?, NULL)
        """;

        try (Connection conn = AuthDBUtil.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setString(1, userId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (String plainKey : plainKeys) {
                    String keySalt = SecurityUtil.generateSalt();
                    String keyHash = SecurityUtil.hashSecret(plainKey, keySalt);

                    String dbKeySaltByEmergencyKey = DbKeyCryptoUtil.generateSalt();
                    String encryptedDbKeyByEmergencyKey =
                            DbKeyCryptoUtil.encryptDatabaseKey(
                                    dbKey,
                                    plainKey,
                                    dbKeySaltByEmergencyKey
                            );

                    ps.setString(1, userId);
                    ps.setString(2, keyHash);
                    ps.setString(3, keySalt);
                    ps.setString(4, encryptedDbKeyByEmergencyKey);
                    ps.setString(5, dbKeySaltByEmergencyKey);
                    ps.setString(6, LocalDateTime.now().format(TS));

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

    public static EmergencyKeyMatch findMatchingUnusedKeyForUser(String userId, String inputKey) {
        String sql = """
            SELECT id, key_hash, key_salt,
                   encrypted_db_key_by_key,
                   db_key_salt_by_key
            FROM emergency_keys
            WHERE user_id = ?
              AND used = 0
        """;

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
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
                        String encryptedDbKeyByKey =
                                rs.getString("encrypted_db_key_by_key");

                        String dbKeySaltByKey =
                                rs.getString("db_key_salt_by_key");

                        if (encryptedDbKeyByKey == null || encryptedDbKeyByKey.isBlank()
                                || dbKeySaltByKey == null || dbKeySaltByKey.isBlank()) {
                            return EmergencyKeyMatch.matchedButMissingDbKeyWrapper(id);
                        }

                        return EmergencyKeyMatch.matched(
                                id,
                                encryptedDbKeyByKey,
                                dbKeySaltByKey
                        );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean markKeyUsed(int keyId) {
        String sql = """
            UPDATE emergency_keys
            SET used = 1,
                used_at = ?
            WHERE id = ?
        """;

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, LocalDateTime.now().format(TS));
            ps.setInt(2, keyId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class EmergencyKeyMatch {
        public final int id;
        public final boolean missingDbKeyWrapper;
        public final String encryptedDbKeyByKey;
        public final String dbKeySaltByKey;

        private EmergencyKeyMatch(int id,
                                  boolean missingDbKeyWrapper,
                                  String encryptedDbKeyByKey,
                                  String dbKeySaltByKey) {
            this.id = id;
            this.missingDbKeyWrapper = missingDbKeyWrapper;
            this.encryptedDbKeyByKey = encryptedDbKeyByKey;
            this.dbKeySaltByKey = dbKeySaltByKey;
        }

        public static EmergencyKeyMatch matched(int id,
                                                String encryptedDbKeyByKey,
                                                String dbKeySaltByKey) {
            return new EmergencyKeyMatch(
                    id,
                    false,
                    encryptedDbKeyByKey,
                    dbKeySaltByKey
            );
        }

        public static EmergencyKeyMatch matchedButMissingDbKeyWrapper(int id) {
            return new EmergencyKeyMatch(
                    id,
                    true,
                    null,
                    null
            );
        }
    }
}