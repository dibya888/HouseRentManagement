package com.rent.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void handleLogin() {

        String username = usernameField.getText();
        String password = passwordField.getText();

        // Temporary login check
        if (username.equals("admin") && password.equals("1234")) {
            messageLabel.setText("Login Successful!");
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setText("Invalid username or password");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }
}