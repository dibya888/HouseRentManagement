package com.rent.main;

import com.rent.util.AuthBootstrapService;
import com.rent.util.AuthDBUtil;
import com.rent.util.CurrentSession;
import com.rent.util.UserRentDatabaseService;
import com.rent.util.LegacyAdminMigrationService;

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
         * Auth DB is global and safe to initialize before login.
         */
        AuthDBUtil.init();
        AuthBootstrapService.ensureDefaultAdminExists();

        /*
         * Ensure default Admin encrypted database exists.
         * Needed before legacy migration.
         */
        UserRentDatabaseService.ensureDefaultAdminRentDatabaseExists();

        /*
         * Auto-migrate old single rent.db into Admin's encrypted rent.db.
         * This is critical for existing users upgrading from older versions.
         */
        LegacyAdminMigrationService.migrateLegacyDataToAdminIfNeeded();

        /*
         * Encrypted per-user DB must be unlocked by password login.
         */
        CurrentSession.clear();

        /*
         * Remove old unsafe auto-login behavior.
         */
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