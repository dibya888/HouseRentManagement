package com.rent.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import com.rent.dao.EmergencyKeyDAO;
import javafx.scene.control.Alert;
import com.rent.dao.AuditLogDAO;
import com.rent.util.AuditActions;
import java.util.prefs.Preferences;

public class DashboardController {
    private static DashboardController instance;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        instance = this;
        loadPage("/fxml/pages/dashboard-view.fxml");
        checkEmergencyKeys();
    }

    public static DashboardController getInstance() {
        return instance;
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
    public void showPastTenants() { loadPage("/fxml/pages/past-tenants-view.fxml"); }

    @FXML
    public void showFlats() {
        loadPage("/fxml/pages/flats-view.fxml");
    }

    @FXML
    public void showRent() {
        loadPage("/fxml/pages/rent-view.fxml");
    }

    @FXML
    public void showRepairs() {
        loadPage("/fxml/pages/repairs-view.fxml");
    }

    @FXML
    public void showReports() {
        loadPage("/fxml/pages/reports-view.fxml");
    }

    @FXML
    public void showAuditLogs() { loadPage("/fxml/pages/audit-logs-view.fxml");}

    public void loadPage(String fxmlPath) {

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
    public void showSettings() {
        loadPage("/fxml/pages/settings-view.fxml");
    }

    @FXML
    public void showProperty() {
        loadPage("/fxml/pages/property-view.fxml");
    }


    @FXML
    public void handleLogout(ActionEvent event) {
        AuditLogDAO.log(AuditActions.LOGOUT, "User logged out.");

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

            stage.setTitle("House Rent Management - Login");
            stage.setScene(scene);
            stage.setMaximized(false);
            stage.setWidth(900);
            stage.setHeight(600);
            stage.centerOnScreen();
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAuditLogs() {
        DashboardController.getInstance().showAuditLogs();
    }

    private void checkEmergencyKeys() {
        int remaining = EmergencyKeyDAO.countUnusedKeys();

        if (remaining == 0) {
            new Alert(Alert.AlertType.WARNING,
                    """
                    You have no unused emergency recovery keys left.
    
                    Please go to:
                    Settings > Recovery Keys
    
                    Generate and save new emergency keys.
                    """).showAndWait();

        } else if (remaining <= 2) {
            new Alert(Alert.AlertType.INFORMATION,
                    """
                    You have only """ + remaining + """
                 emergency recovery key(s) left.

                Please generate new keys soon from:
                Settings > Recovery Keys
                """).showAndWait();
        }
    }
}