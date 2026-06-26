package com.rent.controller;

import com.rent.dao.RecoveryPinDAO;
import com.rent.dao.UserAccountDAO;
import com.rent.model.UserAccount;
import com.rent.util.DbKeyCryptoUtil;
import com.rent.util.EncryptedDbConnectionFactory;
import com.rent.util.AppPaths;
import com.rent.util.SecurityUtil;
import com.rent.util.DatabaseResetUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;

import java.util.Optional;
import java.util.prefs.Preferences;

public class ForgotPasswordController {

    @FXML private TextField usernameField;
    @FXML private PasswordField recoveryPinField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML
    private void resetPassword() {

        String username = text(usernameField);
        String pin = text(recoveryPinField);
        String newPassword = text(newPasswordField);
        String confirmPassword = text(confirmPasswordField);

        if (username.isBlank()) {
            showWarning("Please enter username.");
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

        // 1) Load user from auth.db
        Optional<UserAccount> optUser = UserAccountDAO.findByUsername(username);
        if (optUser.isEmpty()) {
            showError("User not found.");
            return;
        }

        UserAccount user = optUser.get();

        if (!user.isActive()) {
            showError("User is disabled.");
            return;
        }

        // 2) Get recovery PIN record from auth.db
        RecoveryPinDAO.PinData pinData = RecoveryPinDAO.getPinDataByUsername(username);
        if (pinData == null) {
            showError("Recovery PIN is not set for this user.");
            return;
        }

        // 3) Verify entered PIN against stored PIN hash
        boolean validPin = SecurityUtil.verifySecret(pin, pinData.hash, pinData.salt);
        if (!validPin) {
            showError("Invalid recovery PIN.");
            return;
        }

        // 4) Decrypt the DB key using PIN wrapper (preserves encrypted rent.db)
        String dbKey;
        try {
            dbKey = DbKeyCryptoUtil.decryptDatabaseKey(
                    pinData.encryptedDbKeyByPin,
                    pin,
                    pinData.dbKeySaltByPin
            );
        } catch (Exception e) {
            showError("Failed to unlock database key with recovery PIN.");
            return;
        }

        // 5) Verify we can open the user's encrypted rent.db with this key
        try (var ignored = EncryptedDbConnectionFactory.open(
                AppPaths.getUserRentDbPath(user.getId()),
                dbKey
        )) {
            // OK
        } catch (Exception e) {
            showError("User database could not be opened. Backup/restore may be required.");
            return;
        }

        // 6) Create new password hash/salt
        String newSalt = SecurityUtil.generateSalt();
        String newHash = SecurityUtil.hashSecret(newPassword, newSalt);

        // 7) Re-wrap SAME dbKey using new password
        String newDbKeySalt = DbKeyCryptoUtil.generateSalt();
        String newEncryptedDbKey = DbKeyCryptoUtil.encryptDatabaseKey(
                dbKey,
                newPassword,
                newDbKeySalt
        );

        // 8) Save back to auth.db
        boolean updated = UserAccountDAO.updatePasswordAndDbKey(
                user.getId(),
                newHash,
                newSalt,
                newDbKeySalt,
                newEncryptedDbKey
        );

        if (!updated) {
            showError("Failed to update password.");
            return;
        }

        com.rent.dao.AuthAuditLogDAO.log(
                username,
                "PASSWORD_RESET_WITH_RECOVERY_PIN",
                "Password reset using recovery PIN."
        );

        showInfo("Password reset successfully. You can now login with your new password.");
        close();
    }

    @FXML
    private void openEmergencyKeyReset() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/emergency-key-reset.fxml")
            );
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/css/style.css").toExternalForm()
            );

            Stage stage = new Stage();
            stage.initOwner(usernameField.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle("Emergency Key Reset");
            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );
            stage.setScene(scene);
            stage.setMinWidth(440);
            stage.setMinHeight(420);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Failed to open emergency key reset.").showAndWait();
        }
    }

    @FXML
    private void factoryReset() {
        Stage stage = (Stage) usernameField.getScene().getWindow();

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

    private String text(TextField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    private String text(PasswordField field) {
        return field.getText() == null ? "" : field.getText().trim();
    }

    @FXML
    private void close() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
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

    private void clearSavedLogin() {
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
        prefs.remove("loggedInUser");
        prefs.putBoolean("saveLogin", false);
        prefs.remove("rememberedUsername");
    }
}