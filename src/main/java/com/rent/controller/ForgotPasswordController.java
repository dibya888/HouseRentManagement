package com.rent.controller;

import com.rent.util.DBUtil;
import com.rent.util.SecurityUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.rent.dao.UserSecurityDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import com.rent.util.DatabaseResetUtil;
import java.util.prefs.Preferences;

public class ForgotPasswordController {

    @FXML private TextField usernameField;
    @FXML private PasswordField recoveryPinField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void resetPassword() {
        String username = text(usernameField);
        String recoveryPin = text(recoveryPinField);
        String newPassword = text(newPasswordField);
        String confirmPassword = text(confirmPasswordField);

        if (username.isBlank()) {
            showWarning("Please enter username.");
            return;
        }

        if (recoveryPin.isBlank()) {
            showWarning("Please enter recovery PIN.");
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

        RecoveryPinData pinData = getRecoveryPinData(username);

        if (pinData == null) {
            showError("User not found or recovery PIN is not set.");
            return;
        }

        boolean validPin = SecurityUtil.verifySecret(
                recoveryPin,
                pinData.hash,
                pinData.salt
        );

        if (!validPin) {
            showError("Invalid recovery PIN.");
            return;
        }

        if (UserSecurityDAO.updatePassword(username, newPassword)) {
            showInfo("Password reset successfully. You can now login with your new password.");
            close();
        } else {
            showError("Failed to reset password.");
        }
    }

    @FXML
    private void openEmergencyKeyReset() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/emergency-key-reset.fxml")
            );

            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/style.css")
                            .toExternalForm()
            );

            Stage stage = new Stage();
            stage.initOwner(usernameField.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Emergency Key Reset");

            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Failed to open emergency key reset.").showAndWait();
        }
    }

    @FXML
    private void factoryReset() {
        Stage stage = (Stage) usernameField
                .getScene()
                .getWindow();

        boolean confirmed = DatabaseResetUtil.confirmFactoryReset(stage);

        if (!confirmed) {
            showInfo("Factory reset cancelled.");
            return;
        }

        boolean success = DatabaseResetUtil.factoryReset();

        if (!success) {
            return;
        }

        clearSavedLogin();

        showInfo("""
            Factory reset completed.

            Default login:
            Username: admin
            Password: 1234
            """);

        close();
    }

    private RecoveryPinData getRecoveryPinData(String username) {
        String sql = """
                SELECT recovery_pin_hash, recovery_pin_salt
                FROM users
                WHERE username = ?
                """;

        try (Connection conn = DBUtil.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("recovery_pin_hash");
                    String salt = rs.getString("recovery_pin_salt");

                    if (hash == null || hash.isBlank()
                            || salt == null || salt.isBlank()) {
                        return null;
                    }

                    return new RecoveryPinData(hash, salt);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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

    private static class RecoveryPinData {
        String hash;
        String salt;

        RecoveryPinData(String hash, String salt) {
            this.hash = hash;
            this.salt = salt;
        }
    }

    private void clearSavedLogin() {
        Preferences prefs =
                Preferences.userNodeForPackage(LoginController.class);

        prefs.remove("loggedInUser");
    }


}