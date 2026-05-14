package com.rent.controller;

import com.rent.dao.EmergencyKeyDAO;
import com.rent.util.DBUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.rent.dao.UserSecurityDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

        if (!userExists(username)) {
            showError("User not found.");
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

        boolean keyAccepted = EmergencyKeyDAO.useEmergencyKey(emergencyKey);

        if (!keyAccepted) {
            showError("Invalid or already used emergency key.");
            return;
        }

        if (UserSecurityDAO.updatePassword(username, newPassword)) {
            AuditLogDAO.log(
                    username,
                    AuditActions.EMERGENCY_KEY_USED,
                    "Password reset using emergency recovery key for user: " + username
            );

            showInfo("Password reset successfully. This emergency key is now used.");
            closeWithOwner();
        } else {
            showError("Failed to reset password.");
        }
    }

    private boolean userExists(String username) {
        String sql = """
                SELECT id
                FROM users
                WHERE username = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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