package com.rent.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.rent.util.DBUtil;

import java.util.prefs.Preferences;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        DBUtil.init();

        Preferences prefs =
                Preferences.userNodeForPackage(com.rent.controller.LoginController.class);

        String loggedUser = prefs.get("loggedInUser", null);

        FXMLLoader loader;

        if (loggedUser != null) {
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
                getClass().getResource("/css/style.css").toExternalForm()
        );

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}