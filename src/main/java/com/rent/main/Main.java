package com.rent.main;

import com.rent.util.AuthBootstrapService;
import com.rent.util.AuthDBUtil;
import com.rent.util.CurrentSession;
import com.rent.util.DBUtil;
import com.rent.util.LegacyAdminMigrationService;
import com.rent.util.UserRentDatabaseService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        /*
         * OLD system still initializes for now.
         * We will remove this after DBUtil is switched to per-user DB.
         */
        DBUtil.init();
        com.rent.dao.UserSecurityDAO.migratePlainPasswordsIfNeeded();

        /*
         * NEW secure auth + user DB system.
         */
        AuthDBUtil.init();
        AuthBootstrapService.ensureDefaultAdminExists();
        UserRentDatabaseService.ensureDefaultAdminRentDatabaseExists();
        LegacyAdminMigrationService.migrateLegacyDataToAdminIfNeeded();

        /*
         * Important:
         * Encrypted user DB requires password unlock.
         * So app must always start at login page.
         */
        CurrentSession.clear();

        Preferences prefs = Preferences.userNodeForPackage(
                com.rent.controller.LoginController.class
        );
        prefs.remove("loggedInUser");
        prefs.putBoolean("saveLogin", false);

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

        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/images/app-icon.png"))
        );

        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}