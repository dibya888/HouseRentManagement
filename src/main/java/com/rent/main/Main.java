package com.rent.main;

import com.rent.util.DBUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // initialize DB
        DBUtil.init();

        // read saved login
        Preferences prefs =
                Preferences.userNodeForPackage(
                        com.rent.controller.LoginController.class
                );

        String loggedUser = prefs.get("loggedInUser", null);

        FXMLLoader loader;

        if (loggedUser != null && !loggedUser.isEmpty()) {

            loader = new FXMLLoader(
                    getClass().getResource("/fxml/dashboard.fxml")
            );

            stage.setTitle("Rent Management - Dashboard");

        } else {

            loader = new FXMLLoader(
                    getClass().getResource("/fxml/login.fxml")
            );

            stage.setTitle("Rent Management - Login");
        }

        Scene scene = new Scene(loader.load(), 900, 600);

        scene.getStylesheets().add(
                getClass()
                        .getResource("/css/style.css")
                        .toExternalForm()
        );

        stage.setScene(scene);

        stage.setResizable(true);
        stage.centerOnScreen();

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}