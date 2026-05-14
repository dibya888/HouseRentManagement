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

    private void clearSavedLogin() {
        Preferences prefs =
                Preferences.userNodeForPackage(LoginController.class);

        prefs.remove("loggedInUser");
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
}