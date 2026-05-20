package com.rent.controller;

import com.rent.dao.EmergencyKeyDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import javafx.stage.Window;

public class EmergencyKeyResetController {

    @FXML private TextField usernameField;
    @FXML private TextField emergencyKeyField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void resetPassword() {

        String username = text(usernameField);
        String emergencyKey = text(emergencyKeyField).toUpperCase();
        String newPassword = text(newPasswordField);
        String confirmPassword = text(confirmPasswordField);

        if (username.isBlank()) {
            showWarning("Please enter username.");
            return;
        }

        if (emergencyKey.isBlank()) {
            showWarning("Please enter emergency key.");
            return;
        }

        if (newPassword.isBlank()) {
            showWarning("Please enter new password.");
            return;
        }

        if (newPassword.length() < 4) {
            showWarning("New password must be at least 4 characters.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showWarning("New password and confirm password do not match.");
            return;
        }

        var userOpt = com.rent.dao.UserAccountDAO.findByUsername(username);

        if (userOpt.isEmpty()) {
            showError("User not found.");
            return;
        }

        var user = userOpt.get();

        if (!user.isActive()) {
            showError("User is disabled.");
            return;
        }

        var match = com.rent.dao.EmergencyKeyDAO.findMatchingUnusedKeyForUser(
                user.getId(),
                emergencyKey
        );

        if (match == null) {
            showError("Invalid or already used emergency key.");
            return;
        }

        if (match.missingDbKeyWrapper) {
            showError("""
                This emergency key was generated before the secure recovery upgrade.

                Please use Recovery PIN or restore from portable backup.
                After login, generate new emergency keys.
                """);
            return;
        }

        String dbKey;

        try {
            dbKey = com.rent.util.DbKeyCryptoUtil.decryptDatabaseKey(
                    match.encryptedDbKeyByKey,
                    emergencyKey,
                    match.dbKeySaltByKey
            );
        } catch (Exception e) {
            showError("Failed to unlock database key using emergency key.");
            return;
        }

        try (var ignored = com.rent.util.EncryptedDbConnectionFactory.open(
                com.rent.util.AppPaths.getUserRentDbPath(user.getId()),
                dbKey
        )) {
            // DB key verified
        } catch (Exception e) {
            showError("User database could not be opened with this emergency key.");
            return;
        }

        String newSalt = com.rent.util.SecurityUtil.generateSalt();
        String newHash = com.rent.util.SecurityUtil.hashSecret(newPassword, newSalt);

        String newDbKeySalt = com.rent.util.DbKeyCryptoUtil.generateSalt();
        String newEncryptedDbKey =
                com.rent.util.DbKeyCryptoUtil.encryptDatabaseKey(
                        dbKey,
                        newPassword,
                        newDbKeySalt
                );

        boolean updated = com.rent.dao.UserAccountDAO.updatePasswordAndDbKey(
                user.getId(),
                newHash,
                newSalt,
                newDbKeySalt,
                newEncryptedDbKey
        );

        if (!updated) {
            showError("Failed to reset password.");
            return;
        }

        boolean markedUsed = com.rent.dao.EmergencyKeyDAO.markKeyUsed(match.id);


        if (!markedUsed) {
            showWarning("Password was reset, but emergency key could not be marked as used.");
        }

        com.rent.dao.AuthAuditLogDAO.log(
                username,
                "PASSWORD_RESET_WITH_EMERGENCY_KEY",
                "Password reset using one-time emergency recovery key."
        );

        showInfo("Password reset successfully. Your data is preserved.");

        closeWithOwner();
    }

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String text(PasswordField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) usernameField
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

    private void closeWithOwner() {
        Stage currentStage = (Stage) usernameField
                .getScene()
                .getWindow();

        Window owner = currentStage.getOwner();

        currentStage.close();

        if (owner instanceof Stage ownerStage) {
            ownerStage.close();
        }
    }
}