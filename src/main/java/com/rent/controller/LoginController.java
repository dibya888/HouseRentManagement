package com.rent.controller;

import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import com.rent.util.AuthService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox saveLoginCheckBox;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

        String rememberedUsername = prefs.get("rememberedUsername", "");

        if (!rememberedUsername.isBlank()) {
            usernameField.setText(rememberedUsername);

            if (saveLoginCheckBox != null) {
                saveLoginCheckBox.setSelected(true);
            }
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = usernameField.getText() == null
                ? ""
                : usernameField.getText().trim();

        String password = passwordField.getText() == null
                ? ""
                : passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            messageLabel.setText("Username and password are required.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (AuthService.login(username, password)) {
            AuditLogDAO.log(username, AuditActions.LOGIN_SUCCESS, "User logged in successfully.");

            rememberUsernameIfNeeded(username);

            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/dashboard.fxml")
                );

                Scene scene = new Scene(loader.load(), 1200, 750);

                scene.getStylesheets().add(
                        getClass()
                                .getResource("/css/style.css")
                                .toExternalForm()
                );

                Stage stage = (Stage) ((Node) event.getSource())
                        .getScene()
                        .getWindow();

                stage.setTitle("Rent Management Dashboard");
                stage.setScene(scene);
                stage.setMaximized(true);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                messageLabel.setText("Failed to load dashboard.");
                messageLabel.setStyle("-fx-text-fill: red;");
            }

        } else {
            /*
             * No AuditLogDAO here.
             * Failed login has no active CurrentSession, so no user rent DB is open.
             * Auth-level failed-login logging can be added later in auth.db.
             */
            messageLabel.setText("Invalid username or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void rememberUsernameIfNeeded(String username) {
        Preferences prefs = Preferences.userNodeForPackage(LoginController.class);

        boolean remember = saveLoginCheckBox != null && saveLoginCheckBox.isSelected();

        prefs.remove("loggedInUser");
        prefs.putBoolean("saveLogin", false);

        if (remember) {
            prefs.put("rememberedUsername", username);
        } else {
            prefs.remove("rememberedUsername");
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

            new Alert(
                    Alert.AlertType.ERROR,
                    "Failed to open password recovery."
            ).showAndWait();
        }
    }
}