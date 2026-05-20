package com.rent.controller;

import com.rent.util.DatabaseBackupUtil;
import com.rent.util.DatabaseResetUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.util.prefs.Preferences;

public class SettingsController {

    @FXML
    public void openProperty() {
        DashboardController.getInstance().showProperty();
    }

    @FXML
    private void backupDatabase(ActionEvent event) {
        DatabaseBackupUtil.backupDatabase(
                ((Node) event.getSource()).getScene().getWindow()
        );
    }

    @FXML
    private void restoreDatabase(ActionEvent event) {
        DatabaseBackupUtil.restoreDatabase(
                ((Node) event.getSource()).getScene().getWindow()
        );
    }

    @FXML
    private void factoryReset(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        boolean confirmed = DatabaseResetUtil.confirmFactoryReset(stage);

        if (!confirmed) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Factory reset cancelled.").showAndWait();
            return;
        }

        boolean success = DatabaseResetUtil.factoryReset();

        if (!success) {
            return;
        }

        clearSavedLogin();
        loadLogin(stage);

        new Alert(Alert.AlertType.INFORMATION,
                """
                Factory reset completed.
                
                Default login:
                Username: admin
                Password: 1234
                """).showAndWait();
    }

    @FXML
    private void openChangePassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/change-password.fxml")
            );

            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/style.css")
                            .toExternalForm()
            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Change Password");

            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();

            new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Failed to open Change Password."
            ).showAndWait();
        }
    }

    @FXML
    private void openRecoveryPin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/recovery-pin.fxml")
            );

            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/style.css")
                            .toExternalForm()
            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Recovery PIN");

            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Failed to open Recovery PIN.").showAndWait();
        }
    }

    @FXML
    private void openRecoveryKeys(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/recovery-keys.fxml")
            );

            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/style.css")
                            .toExternalForm()
            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Emergency Recovery Keys");

            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Failed to open Emergency Recovery Keys.").showAndWait();
        }
    }

    @FXML
    private void openAuditLogs() {
        DashboardController.getInstance().showAuditLogs();
    }

    private void clearSavedLogin() {
        Preferences prefs =
                Preferences.userNodeForPackage(LoginController.class);

        prefs.remove("loggedInUser");
        prefs.putBoolean("saveLogin", false);
    }

    private void loadLogin(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );

            Scene scene = new Scene(loader.load(), 900, 600);

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/style.css")
                            .toExternalForm()
            );

            stage.setTitle("Rent Management - Login");
            stage.getIcons().clear();
            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );

            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Factory reset completed, but login page could not be loaded. Please restart the app.")
                    .showAndWait();
        }
    }

    @FXML
    private void openUserManagement(ActionEvent event) {
        try {
            if (!com.rent.util.CurrentSession.isAdmin()) {
                new Alert(Alert.AlertType.WARNING,
                        "Only Admin can manage users.").showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/user-management.fxml")
            );

            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/style.css")
                            .toExternalForm()
            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("User Management");

            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Failed to open User Management.").showAndWait();
        }
    }

    @FXML
    private void openWhatsNew() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("What's New");
        alert.setHeaderText("Version 2.0.0 - Secure Multi-User Release");

        alert.setContentText("""
            What's New:

            • Multi-user support
            • Each user has a separate private rent database
            • DB Security upgraded with encrypted user databases
            • Admin-controlled user creation, enable, disable, and delete
            • Save Login now remembers username only for better security
            """);

        alert.showAndWait();
    }
}