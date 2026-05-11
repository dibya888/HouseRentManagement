package com.rent.controller;

import com.rent.util.DBUtil;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.Scene;
import javafx.scene.Node;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.prefs.Preferences;

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

            messageLabel.setText("Invalid username or password");
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private boolean checkLogin(String username, String password) {

        String sql =
                "SELECT * FROM users WHERE username = ? AND password = ?";

        try (
                Connection conn = DBUtil.connect();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }
}