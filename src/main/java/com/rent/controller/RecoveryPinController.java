package com.rent.controller;

import com.rent.util.DBUtil;
import com.rent.util.SecurityUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.prefs.Preferences;

public class RecoveryPinController {

    @FXML private PasswordField passwordField;
    @FXML private PasswordField recoveryPinField;
    @FXML private PasswordField confirmPinField;

    @FXML
    private void saveRecoveryPin() {
        String username = getLoggedInUsername();

        if (username == null || username.isBlank()) {
            showError("Logged-in user not found.");
            return;
        }

        String password = text(passwordField);
        String pin = text(recoveryPinField);
        String confirmPin = text(confirmPinField);

        if (password.isBlank()) {
            showWarning("Please enter your login password.");
            return;
        }

        if (pin.isBlank()) {
            showWarning("Please enter recovery PIN.");
            return;
        }

        if (!pin.matches("\\d{4,8}")) {
            showWarning("Recovery PIN must be 4 to 8 digits.");
            return;
        }

        if (!pin.equals(confirmPin)) {
            showWarning("Recovery PIN and confirm PIN do not match.");
            return;
        }

        if (!isPasswordCorrect(username, password)) {
            showError("Login password is incorrect.");
            return;
        }

        String salt = SecurityUtil.generateSalt();
        String hash = SecurityUtil.hashSecret(pin, salt);

        if (updateRecoveryPin(username, hash, salt)) {
            showInfo("Recovery PIN saved successfully.");
            close();
        } else {
            showError("Failed to save recovery PIN.");
        }
    }

    private boolean isPasswordCorrect(String username, String password) {
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
                    return password.equals(dbPassword);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
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