package com.rent.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.util.prefs.Preferences;

public class DashboardController {

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        loadPage("/fxml/pages/dashboard-view.fxml");
    }

    @FXML
    public void showDashboard() {
        loadPage("/fxml/pages/dashboard-view.fxml");
    }

    @FXML
    public void showTenants() {
        loadPage("/fxml/pages/tenants-view.fxml");
    }

    @FXML
    public void showRooms() {
        loadPage("/fxml/pages/rooms-view.fxml");
    }

    @FXML
    public void showRent() {
        loadPage("/fxml/pages/dashboard-view.fxml");
    }

    @FXML
    public void showReports() {
        loadPage("/fxml/pages/dashboard-view.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            Node page = FXMLLoader.load(
                    getClass().getResource(fxmlPath)
            );

            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleLogout(ActionEvent event) {
        try {
            // 1. Clear session
            Preferences prefs = Preferences.userNodeForPackage(LoginController.class);
            prefs.remove("loggedInUser");

            // 2. Load login screen
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );

            Scene scene = new Scene(loader.load(), 900, 600);

            // 3. Get current stage
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();

            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}