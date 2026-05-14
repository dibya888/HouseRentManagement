package com.rent.controller;

import com.rent.util.DBUtil;
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

public class ChangePasswordController {

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void changePassword() {
        String username = getLoggedInUsername();

        if (username == null || username.isBlank()) {
            showError("Logged-in user not found.");
            return;
        }

        String currentPassword = currentPasswordField.getText() == null
                ? ""
                : currentPasswordField.getText().trim();

        String newPassword = newPasswordField.getText() == null
                ? ""
                : newPasswordField.getText().trim();

        String confirmPassword = confirmPasswordField.getText() == null
                ? ""
                : confirmPasswordField.getText().trim();

        if (currentPassword.isBlank()) {
            showWarning("Please enter current password.");
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

        if (!UserSecurityDAO.verifyPassword(username, currentPassword)) {
            showError("Current password is incorrect.");
            return;
        }

        if (UserSecurityDAO.updatePassword(username, newPassword)) {
            AuditLogDAO.log(
                    AuditActions.PASSWORD_CHANGED,
                    "Password changed for user: " + username
            );

            showInfo("Password changed successfully.");
            close();
        } else {
            showError("Failed to change password.");
        }
    }

    private String getLoggedInUsername() {
        Preferences prefs =
                Preferences.userNodeForPackage(LoginController.class);

        return prefs.get("loggedInUser", null);
    }

    private boolean isCurrentPasswordCorrect(String username, String currentPassword) {
        String sql = """
                SELECT password
                FROM users
                WHERE username = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbPassword = rs.getString("password");
                    return currentPassword.equals(dbPassword);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean updatePassword(String username, String newPassword) {
        String sql = """
                UPDATE users
                SET password = ?
                WHERE username = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newPassword);
            ps.setString(2, username);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) currentPasswordField
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