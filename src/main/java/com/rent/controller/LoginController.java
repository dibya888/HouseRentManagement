package com.rent.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.Node;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import com.rent.dao.UserSecurityDAO;
import java.util.prefs.Preferences;
import javafx.scene.image.Image;
import javafx.stage.Modality;


public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void handleLogin(ActionEvent event) {

        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (checkLogin(username, password)) {
            AuditLogDAO.log(username, AuditActions.LOGIN_SUCCESS, "User logged in successfully.");
            try {

                // save login session
                Preferences prefs =
                        Preferences.userNodeForPackage(LoginController.class);

                prefs.put("loggedInUser", username);

                // load dashboard
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/dashboard.fxml")
                );

                Scene scene = new Scene(loader.load(), 1200, 750);

                // load css
                scene.getStylesheets().add(
                        getClass()
                                .getResource("/css/style.css")
                                .toExternalForm()
                );

                // current stage
                Stage stage = (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

                stage.setTitle("Rent Management Dashboard");
                stage.setScene(scene);

                // maximize window
                stage.setMaximized(true);

                stage.show();

            } catch (Exception e) {
                e.printStackTrace();

                messageLabel.setText("Failed to load dashboard");
                messageLabel.setStyle("-fx-text-fill: red;");
            }

        } else {
            AuditLogDAO.log(username, AuditActions.LOGIN_FAILED, "Failed login attempt.");
            messageLabel.setText("Invalid username or password");
            messageLabel.setStyle("-fx-text-fill: red;");

        }
    }

    @FXML
    private void openForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/forgot-password.fxml")
            );

            Scene scene = new Scene(loader.load());

            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/style.css")
                            .toExternalForm()
            );

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Forgot Password");

            stage.getIcons().add(
                    new Image(getClass().getResourceAsStream("/images/app-icon.png"))
            );

            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();

            new Alert(Alert.AlertType.ERROR,
                    "Failed to open password recovery.").showAndWait();
        }
    }

    private boolean checkLogin(String username, String password) {
        return UserSecurityDAO.verifyPassword(username, password);
    }
}