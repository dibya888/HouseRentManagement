package com.rent.controller;

import com.rent.util.DBUtil;
import com.rent.util.SecurityUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import com.rent.dao.UserSecurityDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.prefs.Preferences;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;

public class RecoveryPinController {

    @FXML private PasswordField passwordField;
    @FXML private PasswordField recoveryPinField;
    @FXML private PasswordField confirmPinField;

    @FXML
    private void saveRecoveryPin() {

        try {

            if (!com.rent.util.CurrentSession.isLoggedIn()) {
                showError("No active session.");
                return;
            }

            String userId = com.rent.util.CurrentSession.getUserId();
            String username = com.rent.util.CurrentSession.getUsername();

            String password = text(passwordField);
            String pin = text(recoveryPinField);
            String confirmPin = text(confirmPinField);

            if (password.isBlank()) {
                showWarning("Enter your login password.");
                return;
            }

            if (pin.isBlank()) {
                showWarning("Enter recovery PIN.");
                return;
            }

            if (!pin.matches("\\d{4,8}")) {
                showWarning("PIN must be 4–8 digits.");
                return;
            }

            if (!pin.equals(confirmPin)) {
                showWarning("PINs do not match.");
                return;
            }

            // ✅ Verify login password
            var userOpt = com.rent.dao.UserAccountDAO.findById(userId);

            if (userOpt.isEmpty() ||
                    !com.rent.util.SecurityUtil.verifySecret(
                            password,
                            userOpt.get().getPasswordHash(),
                            userOpt.get().getPasswordSalt())) {

                showError("Login password is incorrect.");
                return;
            }

            // ✅ Hash PIN
            String pinSalt = com.rent.util.SecurityUtil.generateSalt();
            String pinHash = com.rent.util.SecurityUtil.hashSecret(pin, pinSalt);

            // ✅ CURRENT DB KEY (critical)
            String dbKey = com.rent.util.CurrentSession.getDatabaseKey();

            // ✅ Encrypt DB key using PIN
            String dbKeySaltByPin = com.rent.util.DbKeyCryptoUtil.generateSalt();
            String encryptedDbKeyByPin =
                    com.rent.util.DbKeyCryptoUtil.encryptDatabaseKey(
                            dbKey,
                            pin,
                            dbKeySaltByPin
                    );

            // ✅ Store in auth.db
            boolean success = com.rent.dao.RecoveryPinDAO.upsertPin(
                    userId,
                    pinHash,
                    pinSalt,
                    encryptedDbKeyByPin,
                    dbKeySaltByPin
            );

            if (success) {
                showInfo("Recovery PIN saved successfully.");
                close();
            } else {
                showError("Failed to save recovery PIN.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to set recovery PIN.");
        }
    }

    private boolean updateRecoveryPin(String username, String hash, String salt) {
        String sql = """
                UPDATE users
                SET recovery_pin_hash = ?,
                    recovery_pin_salt = ?
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

    private String getLoggedInUsername() {
        Preferences prefs =
                Preferences.userNodeForPackage(LoginController.class);

        return prefs.get("loggedInUser", null);
    }

    private String text(PasswordField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) passwordField
                .getScene()
                .getWindow();

        stage.close();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private void showWarning(String message) {
        new Alert(Alert.AlertType.WARNING, message).showAndWait();
    }

    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}