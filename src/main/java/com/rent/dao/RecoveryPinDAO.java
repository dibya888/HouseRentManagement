package com.rent.dao;

import com.rent.util.AuthDBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RecoveryPinDAO {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static boolean upsertPin(String userId,
                                    String pinHash,
                                    String pinSalt,
                                    String encryptedDbKeyByPin,
                                    String dbKeySaltByPin) {
        String sql = """
            INSERT INTO recovery_pins 
            (user_id, pin_hash, pin_salt, encrypted_db_key_by_pin, db_key_salt_by_pin, updated_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON CONFLICT(user_id) DO UPDATE SET
                pin_hash = excluded.pin_hash,
                pin_salt = excluded.pin_salt,
                encrypted_db_key_by_pin = excluded.encrypted_db_key_by_pin,
                db_key_salt_by_pin = excluded.db_key_salt_by_pin,
                updated_at = excluded.updated_at
        """;


        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, pinHash);
            ps.setString(3, pinSalt);
            ps.setString(4, encryptedDbKeyByPin);
            ps.setString(5, dbKeySaltByPin);
            ps.setString(6, LocalDateTime.now().format(TS));

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static PinData getPinDataByUsername(String username) {
        String sql = """
            SELECT rp.pin_hash, rp.pin_salt,
                   rp.encrypted_db_key_by_pin,
                   rp.db_key_salt_by_pin
            FROM users u
            JOIN recovery_pins rp ON rp.user_id = u.id
            WHERE u.username = ?
        """;

        try (Connection conn = AuthDBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("pin_hash");
                    String salt = rs.getString("pin_salt");

                    if (hash == null || hash.isBlank() || salt == null || salt.isBlank()) {
                        return null;
                    }

                    return new PinData(
                            hash,
                            salt,
                            rs.getString("encrypted_db_key_by_pin"),
                            rs.getString("db_key_salt_by_pin")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static class PinData {
        public final String hash;
        public final String salt;

        public final String encryptedDbKeyByPin;
        public final String dbKeySaltByPin;

        public PinData(String hash,
                       String salt,
                       String encryptedDbKeyByPin,
                       String dbKeySaltByPin) {

            this.hash = hash;
            this.salt = salt;
            this.encryptedDbKeyByPin = encryptedDbKeyByPin;
            this.dbKeySaltByPin = dbKeySaltByPin;
        }
    }
}