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

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );

            Node page = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {

        try {

            // clear saved login
            Preferences prefs =
                    Preferences.userNodeForPackage(LoginController.class);

            prefs.remove("loggedInUser");

            // load login page
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );

            Scene scene = new Scene(loader.load(), 900, 600);

            // load css again
            scene.getStylesheets().add(
                    getClass()
                            .getResource("/css/style.css")
                            .toExternalForm()
            );

            // current window
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.setTitle("Rent Management - Login");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}